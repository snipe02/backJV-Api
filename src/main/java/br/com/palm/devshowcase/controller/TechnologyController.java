package br.com.palm.devshowcase.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.palm.devshowcase.dto.TechnologyResponseDTO;
import br.com.palm.devshowcase.entity.Technology;
import br.com.palm.devshowcase.service.TechnologyService;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ===================================================================
// Controller original de Technology (inalterado)
// ===================================================================
@RestController
@RequestMapping("/api/technologies")
public class TechnologyController {

    @Autowired
    private TechnologyService service;

    @GetMapping
    public ResponseEntity<List<TechnologyResponseDTO>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }
}

// ===================================================================
// Controller de Project com as 3 rotas novas
// ===================================================================
@RestController
@RequestMapping("/api/projects")
class ProjectController {

    @Autowired
    private ProjectService service;

    // GET /api/projects?technology=Java&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<ProjectResponseDTO>> buscarTodos(
            @RequestParam(required = false) String technology,
            Pageable pageable) {
        return ResponseEntity.ok(service.buscarTodos(technology, pageable));
    }

    // POST /api/projects/{id}/feedbacks
    @PostMapping("/{id}/feedbacks")
    public ResponseEntity<ProjectResponseDTO> cadastrarFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequestDTO dto) {
        ProjectResponseDTO response = service.cadastrarFeedback(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/projects/{id}/upvote
    @PutMapping("/{id}/upvote")
    public ResponseEntity<ProjectResponseDTO> upvote(@PathVariable Long id) {
        return ResponseEntity.ok(service.upvote(id));
    }
}

// ===================================================================
// Service com as regras de negócio de Project
// ===================================================================
@Service
class ProjectService {

    @Autowired
    private ProjectRepository repository;

    public Page<ProjectResponseDTO> buscarTodos(String technology, Pageable pageable) {
        Page<Project> page = repository.buscarComFiltro(technology, pageable);
        return page.map(ProjectResponseDTO::new);
    }

    public ProjectResponseDTO cadastrarFeedback(Long projectId, FeedbackRequestDTO dto) {
        Project project = buscarProjetoOuFalhar(projectId);

        Feedback feedback = new Feedback(dto.getNota(), dto.getComentario(), project);
        project.getFeedbacks().add(feedback);
        project.recalcularNotaMedia();

        Project salvo = repository.save(project);
        return new ProjectResponseDTO(salvo);
    }

    public ProjectResponseDTO upvote(Long projectId) {
        Project project = buscarProjetoOuFalhar(projectId);
        project.setCurtidas(project.getCurtidas() + 1);

        Project salvo = repository.save(project);
        return new ProjectResponseDTO(salvo);
    }

    private Project buscarProjetoOuFalhar(Long projectId) {
        return repository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto não encontrado com id " + projectId));
    }
}

// ===================================================================
// Repositório de Project com filtro por tecnologia e paginação
// ===================================================================
interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.technologies t " +
           "WHERE (:technology IS NULL OR LOWER(t.nome) = LOWER(:technology))")
    Page<Project> buscarComFiltro(@Param("technology") String technology, Pageable pageable);
}

// ===================================================================
// Entidade Project
// ===================================================================
@Entity
class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    @ManyToMany
    @JoinTable(
        name = "project_technology",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "technology_id")
    )
    private List<Technology> technologies = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    private Double notaMedia = 0.0;

    private Long curtidas = 0L;

    public Project() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Technology> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<Technology> technologies) {
        this.technologies = technologies;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Double getNotaMedia() {
        return notaMedia;
    }

    public void setNotaMedia(Double notaMedia) {
        this.notaMedia = notaMedia;
    }

    public Long getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(Long curtidas) {
        this.curtidas = curtidas;
    }

    public void recalcularNotaMedia() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            this.notaMedia = 0.0;
            return;
        }
        double soma = feedbacks.stream()
                .mapToInt(Feedback::getNota)
                .sum();
        this.notaMedia = soma / feedbacks.size();
    }
}

// ===================================================================
// Entidade Feedback
// ===================================================================
@Entity
class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nota; // 1 a 5

    private String comentario;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Feedback() {
    }

    public Feedback(Integer nota, String comentario, Project project) {
        this.nota = nota;
        this.comentario = comentario;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}

// ===================================================================
// DTO de resposta de Project
// ===================================================================
class ProjectResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private List<TechnologyResponseDTO> technologies;
    private List<FeedbackResponseDTO> feedbacks;
    private Double notaMedia;
    private Long curtidas;

    public ProjectResponseDTO() {
    }

    public ProjectResponseDTO(Project project) {
        this.id = project.getId();
        this.nome = project.getNome();
        this.descricao = project.getDescricao();
        this.notaMedia = project.getNotaMedia();
        this.curtidas = project.getCurtidas();
        this.technologies = project.getTechnologies().stream()
                .map(TechnologyResponseDTO::new) // ajuste caso o construtor tenha outra assinatura
                .collect(Collectors.toList());
        this.feedbacks = project.getFeedbacks().stream()
                .map(FeedbackResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<TechnologyResponseDTO> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<TechnologyResponseDTO> technologies) {
        this.technologies = technologies;
    }

    public List<FeedbackResponseDTO> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackResponseDTO> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Double getNotaMedia() {
        return notaMedia;
    }

    public void setNotaMedia(Double notaMedia) {
        this.notaMedia = notaMedia;
    }

    public Long getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(Long curtidas) {
        this.curtidas = curtidas;
    }
}

// ===================================================================
// DTO de requisição de Feedback
// ===================================================================
class FeedbackRequestDTO {

    @NotNull(message = "A nota é obrigatória")
    @Min(value = 1, message = "A nota mínima é 1")
    @Max(value = 5, message = "A nota máxima é 5")
    private Integer nota;

    @NotBlank(message = "O comentário é obrigatório")
    private String comentario;

    public FeedbackRequestDTO() {
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}

// ===================================================================
// DTO de resposta de Feedback
// ===================================================================
class FeedbackResponseDTO {

    private Long id;
    private Integer nota;
    private String comentario;

    public FeedbackResponseDTO() {
    }

    public FeedbackResponseDTO(Feedback feedback) {
        this.id = feedback.getId();
        this.nota = feedback.getNota();
        this.comentario = feedback.getComentario();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}

// ===================================================================
// Exceção para recurso não encontrado
// ===================================================================
class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

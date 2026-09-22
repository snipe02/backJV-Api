package br.com.palm.devshowcase.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.palm.devshowcase.dto.ProjectRequestDTO;
import br.com.palm.devshowcase.dto.ProjectResponseDTO;
import br.com.palm.devshowcase.dto.ProfileRequestDTO;
import br.com.palm.devshowcase.dto.ProfileResponseDTO;
import br.com.palm.devshowcase.dto.FeedbackRequestDTO;
import br.com.palm.devshowcase.service.ProjectService;
import br.com.palm.devshowcase.service.ProfileService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> listarProjetos() {
        return ResponseEntity.ok(projectService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> criarProjeto(@RequestBody ProjectRequestDTO dto) {
        return ResponseEntity.ok(projectService.criar(dto));
    }

    @PostMapping("/{id}/feedbacks")
    public ResponseEntity<Void> adicionarFeedback(@PathVariable Long id, @RequestBody FeedbackRequestDTO dto) {
        projectService.adicionarFeedback(id, dto);
        return ResponseEntity.ok().build();
    }
}

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProfileResponseDTO> criarProfile(@RequestBody ProfileRequestDTO dto) {
        return ResponseEntity.ok(profileService.criar(dto));
    }
}

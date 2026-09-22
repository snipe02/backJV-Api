package br.com.palm.devshowcase.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.palm.devshowcase.dto.TechnologyResponseDTO;
import br.com.palm.devshowcase.repository.TechnologyRepository;

@Service
public class TechnologyService {

	@Autowired
	private TechnologyRepository repository;
	
	@Transactional(readOnly = true)
    public List<TechnologyResponseDTO> buscarTodos() {
        return repository.findAll().stream()
                .map(TechnologyResponseDTO::fromEntity)
                .toList();
    }
}
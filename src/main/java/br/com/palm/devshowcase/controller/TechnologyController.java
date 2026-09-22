package br.com.palm.devshowcase.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.palm.devshowcase.dto.TechnologyResponseDTO;
import br.com.palm.devshowcase.service.TechnologyService;

@RestController @RequestMapping("/api/technologies")
public class TechnologyController {

	@Autowired
	private TechnologyService service;
	
	@GetMapping
    public ResponseEntity<List<TechnologyResponseDTO>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }
}

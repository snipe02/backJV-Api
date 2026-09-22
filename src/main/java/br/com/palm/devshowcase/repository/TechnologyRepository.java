package br.com.palm.devshowcase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.palm.devshowcase.model.Technology;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {
	
}
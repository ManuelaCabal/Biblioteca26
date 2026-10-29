package com.biblioteca26.repositorios;

import com.biblioteca26.modelos.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Integer> {
}

package com.biblioteca26.repositorios;

import com.biblioteca26.modelos.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Integer> {
}

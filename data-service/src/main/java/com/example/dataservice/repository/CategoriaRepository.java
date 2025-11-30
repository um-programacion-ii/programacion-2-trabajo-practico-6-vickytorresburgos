package com.example.dataservice.repository;

import com.example.dataservice.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    /**
     * Busca una categoría por su nombre.
     * @param nombre El nombre de la categoría.
     * @return Un Optional que contiene la Categoria si se encuentra.
     */
    Optional<Categoria> findByNombre(String nombre);

}

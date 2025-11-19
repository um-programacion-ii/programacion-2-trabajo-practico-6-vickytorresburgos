package com.example.dataservice.repository;

import com.example.dataservice.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca todos los productos que pertenecen a una categoría específica por su nombre.
     * @param nombreCategoria El nombre de la categoría.
     * @return Una lista de Productos.
     */
    List<Producto> findByCategoriaNombre(String nombreCategoria);
}

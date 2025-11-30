package com.example.dataservice.repository;

import com.example.dataservice.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Busca en el inventario productos donde la cantidad sea menor al stock mínimo.
     * @return Lista de Inventario con stock bajo.
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad < i.stockMinimo")
    List<Inventario> findProductosConStockBajo();

    /**
     * Busca un registro de inventario por el ID del producto asociado.
     * @param productoId El ID del producto.
     * @return Un Optional que contiene el Inventario si se encuentra.
     */
    Optional<Inventario> findByProductoId(Long productoId);
}

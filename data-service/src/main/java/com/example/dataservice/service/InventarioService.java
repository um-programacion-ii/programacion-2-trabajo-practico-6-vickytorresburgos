package com.example.dataservice.service;

import com.example.dataservice.dto.InventarioDTO;
import com.example.dataservice.entity.Inventario;
import com.example.dataservice.exception.ProductoNoEncontradoException;
import com.example.dataservice.mapper.InventarioMapper;
import com.example.dataservice.repository.InventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioService {
    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;

    /**
     * Constructor para la inyección de dependencias.
     * @param inventarioRepository Repositorio JPA para la entidad Inventario.
     * @param inventarioMapper Mapper para convertir entre entidades y DTOs.
     */
    public InventarioService(InventarioRepository inventarioRepository, InventarioMapper inventarioMapper) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
    }

    /**
     * Obtiene todos los registros de inventario de la base de datos.
     * @return Lista de todos los registros de inventario convertidos a DTOs.
     */
    public List<InventarioDTO> obtenerTodo() {
        return inventarioRepository.findAll()
                .stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la cantidad de stock de un producto específico.
     * @param productoId El ID del producto asociado al inventario a actualizar.
     * @param cantidad La nueva cantidad de stock.
     * @return El registro de Inventario actualizado, convertido a DTO.
     * @throws ProductoNoEncontradoException si el registro de inventario asociado al productoId no existe.
     */
    public InventarioDTO actualizarStock(Long productoId, Integer cantidad) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + productoId));
        inventario.setCantidad(cantidad);
        inventario.setFechaActualizacion(LocalDateTime.now());

        Inventario inventarioGuardado = inventarioRepository.save(inventario);

        return inventarioMapper.toDTO(inventarioGuardado);
    }

    /**
     * Obtiene una lista de productos cuyo stock es menor o igual al stock mínimo configurado.
     * @return Lista de registros de inventario con stock bajo, convertidos a DTOs.
     */
    public List<InventarioDTO> obtenerProductosConStockBajo() {
        return inventarioRepository.findProductosConStockBajo()
                .stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList());
    }
}

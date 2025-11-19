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

@Service
@Transactional
public class InventarioService {
    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;

    public InventarioService(InventarioRepository inventarioRepository, InventarioMapper inventarioMapper) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
    }

    public List<Inventario> obtenerTodo() {
        return inventarioRepository.findAll();
    }
    
    public Inventario actualizarStock(Long productoId, Integer cantidad) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + productoId));
        inventario.setCantidad(cantidad);
        inventario.setFechaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(inventario);
    }

    public List<Inventario> obtenerProductosConStockBajo() {
        return inventarioRepository.findProductosConStockBajo();
    }
}

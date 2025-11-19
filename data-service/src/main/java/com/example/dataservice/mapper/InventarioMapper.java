package com.example.dataservice.mapper;

import com.example.dataservice.dto.InventarioDTO;
import com.example.dataservice.entity.Inventario;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    private final ProductoMapper productoMapper;
    public InventarioMapper(ProductoMapper productoMapper) {
        this.productoMapper = productoMapper;
    }

    public InventarioDTO toDTO(Inventario inventario) {
        if (inventario == null) {
            return null;
        }

        InventarioDTO dto = new InventarioDTO();
        dto.setId(inventario.getId());
        dto.setCantidad(inventario.getCantidad());
        dto.setStockMinimo(inventario.getStockMinimo());
        dto.setFechaActualizacion(inventario.getFechaActualizacion());

        if (inventario.getProducto() != null) {
            dto.setProducto(productoMapper.toDTO(inventario.getProducto()));
        }

        return dto;
    }
}
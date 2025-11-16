package com.example.dataservice.mapper;

import com.example.dataservice.dto.ProductoDTO;
import com.example.dataservice.entity.Inventario;
import com.example.dataservice.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoDTO toDTO(Producto producto) {
        if (producto == null) {
            return null;
        }

        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());

        if (producto.getCategoria() != null) {
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        Inventario inventario = producto.getInventario();
        if (inventario != null) {
            dto.setStock(inventario.getCantidad());
            boolean stockBajo = inventario.getStockMinimo() != null &&
                    inventario.getCantidad() < inventario.getStockMinimo();
            dto.setStockBajo(stockBajo);
        } else {
            dto.setStock(0);
            dto.setStockBajo(false);
        }

        return dto;
    }
}
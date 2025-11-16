package com.example.businessservice.service;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.exceptions.InventarioNoEncontradoException;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventarioBusinessService {
    private final DataServiceClient dataServiceClient;

    public InventarioBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    /**
     * Obtiene productos con stock bajo (delegado al data-service).
     */
    public List<InventarioDTO> obtenerProductosConStockBajo() {
        try {
            List<InventarioDTO> inventario = dataServiceClient.obtenerProductosConStockBajo();
            return inventario;
        } catch (FeignException e) {
            log.error("Error al obtener los productos con stock bajo", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Obtiene el registro de inventario para un producto por su id.
     */
    public InventarioDTO obtenerInventarioPorProductoId(Long productoId) {
        if (productoId == null) throw new ValidacionNegocioException("El id del producto es obligatorio");
        try {
            return dataServiceClient.obtenerInventarioPorProductoId(productoId);
        } catch (FeignException.NotFound e) {
            throw new InventarioNoEncontradoException("Inventario no encontrado para producto id: " + productoId);
        } catch (FeignException e) {
            log.error("Error al obtener inventario para producto id={} del microservicio de datos", productoId, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Actualiza la cantidad de inventario de un producto.
     */
    public InventarioDTO actualizarCantidadInventario(Long productoId, Integer nuevaCantidad) {
        if (productoId == null) throw new ValidacionNegocioException("El id del producto es obligatorio");
        if (nuevaCantidad == null || nuevaCantidad < 0) throw new ValidacionNegocioException("La nueva cantidad debe ser >= 0");
        try {
            InventarioDTO payload = new InventarioDTO();
            ProductoDTO producto = new ProductoDTO();
            producto.setId(productoId);
            payload.setProducto(producto);
            payload.setCantidad(nuevaCantidad);
            return dataServiceClient.actualizarCantidadInventario(productoId, payload);
        } catch (FeignException.NotFound e) {
            throw new InventarioNoEncontradoException("Inventario no encontrado para producto id: " + productoId);
        } catch (FeignException e) {
            log.error("Error al actualizar inventario para producto id={} en el microservicio de datos", productoId, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Registra un movimiento de inventario (entrada/salida).
     */
    public InventarioDTO registrarMovimientoInventario(InventarioDTO movimiento) {
        if (movimiento == null
                || movimiento.getProducto() == null
                || movimiento.getProducto().getId() == null) {
            throw new ValidacionNegocioException("Movimiento de inventario inválido: producto.id obligatorio");
        }
        try {
            return dataServiceClient.registrarMovimientoInventario(movimiento);
        } catch (FeignException.NotFound e) {
            throw new InventarioNoEncontradoException("Producto no encontrado para registrar movimiento, id: " + movimiento.getProducto().getId());
        } catch (FeignException e) {
            log.error("Error al registrar movimiento de inventario en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }
}

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

/**
 * Servicio de negocio para la gestión de Inventario.
 * Maneja las operaciones de consulta de stock, actualizaciones manuales y
 * registro de movimientos, validando las reglas de negocio antes de llamar al servicio de datos.
 */
@Service
@Slf4j
public class InventarioBusinessService {
    private final DataServiceClient dataServiceClient;

    /**
     * Inyección de dependencias.
     * @param dataServiceClient Cliente Feign para comunicación con data-service.
     */
    public InventarioBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    /**
     * Obtiene la lista de productos que están por debajo del umbral de stock mínimo.
     *
     * @return Lista de {@link InventarioDTO} con stock crítico.
     * @throws MicroserviceCommunicationException Si falla la comunicación.
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
     * Obtiene el registro de inventario asociado a un producto específico.
     *
     * @param productoId Identificador del producto.
     * @return El {@link InventarioDTO} asociado.
     * @throws ValidacionNegocioException Si el ID es nulo.
     * @throws InventarioNoEncontradoException Si no existe inventario para ese producto.
     * @throws MicroserviceCommunicationException Error técnico.
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
     * Actualiza manualmente la cantidad de stock disponible para un producto.
     *
     * @param productoId Identificador del producto.
     * @param nuevaCantidad Nueva cantidad absoluta de stock (debe ser >= 0).
     * @return El inventario actualizado.
     * @throws ValidacionNegocioException Si el ID es nulo o la cantidad es negativa.
     * @throws InventarioNoEncontradoException Si el producto no existe.
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
     * Registra un movimiento de inventario (entradas o salidas).
     * Se espera que el DTO contenga la referencia al producto y la cantidad a ajustar.
     *
     * @param movimiento Objeto {@link InventarioDTO} con los datos del movimiento.
     * @return El estado del inventario tras aplicar el movimiento.
     * @throws ValidacionNegocioException Si el movimiento no contiene el ID del producto.
     * @throws InventarioNoEncontradoException Si el producto no existe.
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
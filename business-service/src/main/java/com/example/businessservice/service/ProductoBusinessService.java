package com.example.businessservice.service;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import com.example.businessservice.exceptions.ProductoNoEncontradoException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio de negocio encargado de la gestión de Productos.
 * Esta clase encapsula las reglas de negocio, validaciones y la comunicación
 * con el microservicio de datos a través de {@link DataServiceClient}.
 */
@Service
@Slf4j
public class ProductoBusinessService {

    private final DataServiceClient dataServiceClient;

    /**
     * Constructor para la inyección de dependencias.
     *
     * @param dataServiceClient Cliente Feign para comunicación con Data Service.
     */
    public ProductoBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    /**
     * Recupera el listado completo de productos.
     *
     * @return Lista de {@link ProductoDTO} disponibles.
     * @throws MicroserviceCommunicationException Si falla la conexión con el servicio de datos.
     */
    public List<ProductoDTO> obtenerTodosLosProductos() {
        try {
            return dataServiceClient.obtenerTodosLosProductos();
        } catch (FeignException e) {
            log.error("Error al obtener productos del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Busca un producto específico por su identificador.
     *
     * @param id Identificador único del producto.
     * @return El {@link ProductoDTO} encontrado.
     * @throws ProductoNoEncontradoException      Si el servicio de datos retorna 404.
     * @throws MicroserviceCommunicationException Si ocurre un error inesperado en la comunicación.
     */
    public ProductoDTO obtenerProductoPorId(Long id) {
        try {
            return dataServiceClient.obtenerProductoPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al obtener producto del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Crea un nuevo producto aplicando validaciones de negocio.
     *
     * @param request Datos del producto a crear.
     * @return El {@link ProductoDTO} creado y persistido.
     * @throws ValidacionNegocioException         Si los datos del producto no son válidos (precio negativo, nombre vacío, etc.).
     * @throws MicroserviceCommunicationException Si falla la persistencia en el servicio de datos.
     */
    public ProductoDTO crearProducto(ProductoRequest request) {
        validarProducto(request, true);

        try {
            return dataServiceClient.crearProducto(request);
        } catch (FeignException e) {
            log.error("Error al crear producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Método auxiliar para validar la integridad de los datos del producto.
     *
     * @param request DTO a validar.
     * @param crear   Indica si la validación es para creación (requiere nombre) o actualización.
     * @throws ValidacionNegocioException Si alguna regla de validación falla.
     */
    private void validarProducto(ProductoRequest request, boolean crear) {
        if (request == null) throw new ValidacionNegocioException("Request de producto vacío");
        if (crear && (request.getNombre() == null || request.getNombre().trim().isEmpty())) {
            throw new ValidacionNegocioException("El nombre del producto es obligatorio");
        }
        if (request.getPrecio() == null) {
            throw new ValidacionNegocioException("El precio del producto es obligatorio");
        }
        if (request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionNegocioException("El precio debe ser mayor a cero");
        }
        if (request.getStock() != null && request.getStock() < 0) {
            throw new ValidacionNegocioException("El stock no puede ser negativo");
        }
    }

    /**
     * Obtiene los productos asociados a una categoría específica.
     *
     * @param nombre Nombre de la categoría.
     * @return Lista de productos pertenecientes a esa categoría.
     * @throws ValidacionNegocioException         Si el nombre de la categoría es nulo o vacío.
     * @throws ProductoNoEncontradoException      Si la categoría no existe o no tiene productos (404 desde Data Service).
     * @throws MicroserviceCommunicationException Si falla la comunicación.
     */
    public List<ProductoDTO> obtenerProductosPorCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionNegocioException("El nombre de la categoría es obligatorio");
        }

        try {
            return dataServiceClient.obtenerProductosPorCategoria(nombre.trim());
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("No se encontraron productos para la categoría: " + nombre);
        } catch (FeignException e) {
            log.error("Error al obtener productos por categoría '{}' del microservicio de datos", nombre, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Actualiza la información de un producto existente.
     *
     * @param id      Identificador del producto a actualizar.
     * @param request Datos nuevos para el producto.
     * @return El {@link ProductoDTO} actualizado.
     * @throws ValidacionNegocioException         Si el ID es nulo o los datos no son válidos.
     * @throws ProductoNoEncontradoException      Si el producto no existe en el servicio de datos.
     * @throws MicroserviceCommunicationException Si falla la comunicación.
     */
    public ProductoDTO actualizarProducto(Long id, ProductoRequest request) {
        if (id == null) throw new ValidacionNegocioException("El id del producto es obligatorio para actualizar");
        validarProducto(request, false);
        try {
            return dataServiceClient.actualizarProducto(id, request);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al actualizar producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Elimina un producto del sistema.
     *
     * @param id Identificador del producto a eliminar.
     * @throws ValidacionNegocioException         Si el ID es nulo.
     * @throws ProductoNoEncontradoException      Si el producto no existe.
     * @throws MicroserviceCommunicationException Si falla la comunicación.
     */
    public void eliminarProducto(Long id) {
        if (id == null) throw new ValidacionNegocioException("El id del producto es obligatorio para eliminar");
        try {
            dataServiceClient.eliminarProducto(id);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al eliminar producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Filtra los productos existentes basándose en un rango de precios.
     * <p>
     * Nota: Este filtrado se realiza en memoria después de obtener todos los productos.
     * </p>
     *
     * @param minPrecio Precio mínimo (inclusive). Puede ser null.
     * @param maxPrecio Precio máximo (inclusive). Puede ser null.
     * @return Lista de productos que cumplen con el criterio de precio.
     * @throws ValidacionNegocioException Si el precio mínimo es mayor que el máximo.
     */
    public List<ProductoDTO> obtenerProductosFiltradosPorPrecio(BigDecimal minPrecio, BigDecimal maxPrecio) {
        if (minPrecio != null && maxPrecio != null && minPrecio.compareTo(maxPrecio) > 0) {
            throw new ValidacionNegocioException("El precio mínimo no puede ser mayor que el máximo");
        }

        List<ProductoDTO> todos = obtenerTodosLosProductos();

        return todos.stream()
                .filter(Objects::nonNull)
                .filter(p -> {
                    BigDecimal precio = p.getPrecio();
                    if (precio == null) return false;
                    if (minPrecio != null && precio.compareTo(minPrecio) < 0) return false;
                    if (maxPrecio != null && precio.compareTo(maxPrecio) > 0) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }
}
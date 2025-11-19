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

@Service
@Slf4j
public class ProductoBusinessService {

    private final DataServiceClient dataServiceClient;

    public ProductoBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        try {
            return dataServiceClient.obtenerTodosLosProductos();
        } catch (FeignException e) {
            log.error("Error al obtener productos del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

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

    public ProductoDTO crearProducto(ProductoRequest request) {
        validarProducto(request, true);

        try {
            return dataServiceClient.crearProducto(request);
        } catch (FeignException e) {
            log.error("Error al crear producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

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

package com.example.businessservice.service;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.exceptions.CategoriaNoEncontradaException;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class CategoriaBusinessService {
    private final DataServiceClient dataServiceClient;

    public CategoriaBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    /**
     * Obtiene todas las categorías.
     */
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        try {
            return dataServiceClient.obtenerTodasLasCategorias();
        } catch (FeignException e) {
            log.error("Error al obtener categorias del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicacion con el servicio de datos");
        }
    }

    /**
     * Obtiene una categoría por id.
     */
    public CategoriaDTO obtenerCategoriaPorId(Long id) {
        if (id == null) throw new ValidacionNegocioException("El id de la categoría es obligatorio");
        try {
            return dataServiceClient.obtenerCategoriaPorId(id);
        } catch (FeignException.NotFound e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al obtener categoría id={} del microservicio de datos", id, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Crea una nueva categoría.
     */
    public CategoriaDTO crearCategoria(CategoriaDTO request) {
        if (request == null || Objects.requireNonNullElse(request.getNombre(), "").trim().isEmpty()) {
            throw new ValidacionNegocioException("El nombre de la categoría es obligatorio");
        }
        try {
            return dataServiceClient.crearCategoria(request);
        } catch (FeignException.Conflict e) {
            throw new ValidacionNegocioException("Ya existe una categoría con ese nombre");
        } catch (FeignException e) {
            log.error("Error al crear categoría en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Actualiza una categoría existente.
     */
    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO request) {
        if (id == null) throw new ValidacionNegocioException("El id de la categoría es obligatorio para actualizar");
        try {
            return dataServiceClient.actualizarCategoria(id, request);
        } catch (FeignException.NotFound e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id);
        } catch (FeignException.Conflict e) {
            throw new ValidacionNegocioException("Ya existe una categoría con ese nombre");
        } catch (FeignException e) {
            log.error("Error al actualizar categoría id={} en el microservicio de datos", id, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Elimina una categoría por id.
     */
    public void eliminarCategoria(Long id) {
        if (id == null) throw new ValidacionNegocioException("El id de la categoría es obligatorio para eliminar");
        try {
            dataServiceClient.eliminarCategoria(id);
        } catch (FeignException.NotFound e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al eliminar categoría id={} en el microservicio de datos", id, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    /**
     * Obtiene los productos asociados a una categoría (por nombre).
     */
    public List<ProductoDTO> obtenerProductosDeCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.trim().isEmpty()) {
            throw new ValidacionNegocioException("El nombre de la categoría es obligatorio");
        }
        try {
            return dataServiceClient.obtenerProductosPorCategoria(nombreCategoria.trim());
        } catch (FeignException.NotFound e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada o sin productos: " + nombreCategoria);
        } catch (FeignException e) {
            log.error("Error al obtener productos para la categoría '{}' en el microservicio de datos", nombreCategoria, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }

    public Map<String, Object> calcularEstadisticasCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionNegocioException("El nombre de la categoría es obligatorio");
        }

        try {
            List<ProductoDTO> productos = dataServiceClient.obtenerProductosPorCategoria(nombre.trim());

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("categoriaNombre", nombre.trim());

            if (productos == null || productos.isEmpty()) {
                resultado.put("totalProductos", 0L);
                resultado.put("totalStock", 0);
                resultado.put("valorTotalInventario", BigDecimal.ZERO);
                resultado.put("precioPromedio", BigDecimal.ZERO);
                resultado.put("precioMinimo", BigDecimal.ZERO);
                resultado.put("precioMaximo", BigDecimal.ZERO);
                resultado.put("productosConStockBajo", 0L);
                resultado.put("porcentajeProductosConStockBajo", 0.0);
                return resultado;
            }

            long totalProductos = productos.size();

            int totalStock = productos.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(p -> p.getStock() == null ? 0 : p.getStock())
                    .sum();

            BigDecimal valorTotalInventario = productos.stream()
                    .filter(Objects::nonNull)
                    .map(p -> {
                        BigDecimal precio = p.getPrecio() == null ? BigDecimal.ZERO : p.getPrecio();
                        int stock = p.getStock() == null ? 0 : p.getStock();
                        return precio.multiply(BigDecimal.valueOf(stock));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            List<BigDecimal> precios = productos.stream()
                    .filter(Objects::nonNull)
                    .map(ProductoDTO::getPrecio)
                    .filter(Objects::nonNull)
                    .toList();

            BigDecimal precioPromedio = BigDecimal.ZERO;
            BigDecimal precioMinimo = BigDecimal.ZERO;
            BigDecimal precioMaximo = BigDecimal.ZERO;
            if (!precios.isEmpty()) {
                BigDecimal suma = precios.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                precioPromedio = suma.divide(BigDecimal.valueOf(precios.size()), 2, RoundingMode.HALF_UP);
                precioMinimo = precios.stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
                precioMaximo = precios.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            }

            final int umbralDefault = 10;
            long productosConStockBajo = productos.stream()
                    .filter(Objects::nonNull)
                    .filter(p -> {
                        if (p.getStockBajo() != null) return p.getStockBajo();
                        Integer stock = p.getStock();
                        return stock != null && stock <= umbralDefault;
                    })
                    .count();

            double porcentajeStockBajo = productosConStockBajo * 100.0 / (double) totalProductos;

            resultado.put("totalProductos", totalProductos);
            resultado.put("totalStock", totalStock);
            resultado.put("valorTotalInventario", valorTotalInventario);
            resultado.put("precioPromedio", precioPromedio);
            resultado.put("precioMinimo", precioMinimo);
            resultado.put("precioMaximo", precioMaximo);
            resultado.put("productosConStockBajo", productosConStockBajo);
            resultado.put("porcentajeProductosConStockBajo", Math.round(porcentajeStockBajo * 100.0) / 100.0); // 2 decimales

            return resultado;
        } catch (FeignException.NotFound e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada: " + nombre);
        } catch (FeignException e) {
            log.error("Error al calcular estadísticas para la categoría '{}' desde data-service", nombre, e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }
}

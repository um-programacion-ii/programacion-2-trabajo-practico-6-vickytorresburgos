package com.example.businessservice.services;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.exceptions.CategoriaNoEncontradaException;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import com.example.businessservice.service.CategoriaBusinessService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private CategoriaBusinessService categoriaService;

    private FeignException crearFeignNotFound() {
        return new FeignException.NotFound("No encontrado",
                Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, new RequestTemplate()),
                null, null);
    }

    private FeignException crearFeignConflict() {
        return new FeignException.Conflict("Conflicto",
                Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, new RequestTemplate()),
                null, null);
    }


    @Test
    @DisplayName("calcularEstadisticas: Debe calcular métricas correctamente con productos mixtos")
    void calcularEstadisticas_ConProductos_CalculaCorrectamente() {
        // Arrange
        String categoria = "Tecnologia";
        ProductoDTO p1 = new ProductoDTO(); p1.setPrecio(BigDecimal.valueOf(100)); p1.setStock(10); p1.setStockBajo(true);
        ProductoDTO p2 = new ProductoDTO(); p2.setPrecio(BigDecimal.valueOf(200)); p2.setStock(20); p2.setStockBajo(false);

        when(dataServiceClient.obtenerProductosPorCategoria(categoria)).thenReturn(List.of(p1, p2));

        // Act
        Map<String, Object> stats = categoriaService.calcularEstadisticasCategoria(categoria);

        // Assert
        assertEquals(2L, stats.get("totalProductos"));
        assertEquals(30, stats.get("totalStock"));
        // Valor: (100*10) + (200*20) = 1000 + 4000 = 5000
        assertEquals(0, BigDecimal.valueOf(5000).compareTo((BigDecimal) stats.get("valorTotalInventario")));
        assertEquals(0, BigDecimal.valueOf(150).compareTo((BigDecimal) stats.get("precioPromedio")));
        assertEquals(1L, stats.get("productosConStockBajo"));
        assertEquals(50.0, stats.get("porcentajeProductosConStockBajo"));
    }

    @Test
    @DisplayName("calcularEstadisticas: Retorna ceros si no hay productos")
    void calcularEstadisticas_SinProductos_RetornaCeros() {
        when(dataServiceClient.obtenerProductosPorCategoria("Vacia")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = categoriaService.calcularEstadisticasCategoria("Vacia");

        assertEquals(0L, stats.get("totalProductos"));
        assertEquals(BigDecimal.ZERO, stats.get("valorTotalInventario"));
    }


    @Test
    @DisplayName("obtenerCategoriaPorId: Lanza excepción si el ID es nulo")
    void obtenerCategoriaPorId_IdNulo_LanzaValidacion() {
        assertThrows(ValidacionNegocioException.class, () -> categoriaService.obtenerCategoriaPorId(null));
    }

    @Test
    @DisplayName("obtenerCategoriaPorId: Lanza CategoriaNoEncontradaException si Feign devuelve 404")
    void obtenerCategoriaPorId_NoExiste_LanzaExcepcion() {
        when(dataServiceClient.obtenerCategoriaPorId(1L)).thenThrow(crearFeignNotFound());
        assertThrows(CategoriaNoEncontradaException.class, () -> categoriaService.obtenerCategoriaPorId(1L));
    }


    @Test
    @DisplayName("crearCategoria: Lanza Validacion si nombre es conflicto (409)")
    void crearCategoria_Conflicto_LanzaValidacion() {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setNombre("Duplicado");

        when(dataServiceClient.crearCategoria(dto)).thenThrow(crearFeignConflict());

        ValidacionNegocioException ex = assertThrows(ValidacionNegocioException.class, () -> categoriaService.crearCategoria(dto));
        assertEquals("Ya existe una categoría con ese nombre", ex.getMessage());
    }

    @Test
    @DisplayName("crearCategoria: Lanza MicroserviceCommunicationException en error genérico")
    void crearCategoria_ErrorServidor_LanzaExcepcionComunicacion() {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setNombre("Nueva");

        when(dataServiceClient.crearCategoria(dto)).thenThrow(new FeignException.InternalServerError("Error",
                Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, new RequestTemplate()), null, null));

        assertThrows(MicroserviceCommunicationException.class, () -> categoriaService.crearCategoria(dto));
    }
}
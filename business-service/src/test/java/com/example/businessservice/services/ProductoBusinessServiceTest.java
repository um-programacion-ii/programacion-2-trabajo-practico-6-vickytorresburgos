package com.example.businessservice.services;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import com.example.businessservice.exceptions.ProductoNoEncontradoException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import com.example.businessservice.service.ProductoBusinessService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private ProductoBusinessService productoService;


    @Test
    @DisplayName("filtrarPorPrecio: Lanza excepción si min > max")
    void filtrarPorPrecio_RangoInvalido_LanzaExcepcion() {
        assertThrows(ValidacionNegocioException.class,
                () -> productoService.obtenerProductosFiltradosPorPrecio(BigDecimal.valueOf(100), BigDecimal.valueOf(50)));
    }

    @Test
    @DisplayName("filtrarPorPrecio: Filtra correctamente la lista devuelta por el cliente")
    void filtrarPorPrecio_FiltraCorrectamente() {
        // Arrange
        ProductoDTO p1 = new ProductoDTO(); p1.setPrecio(BigDecimal.valueOf(10));
        ProductoDTO p2 = new ProductoDTO(); p2.setPrecio(BigDecimal.valueOf(50));
        ProductoDTO p3 = new ProductoDTO(); p3.setPrecio(BigDecimal.valueOf(100));

        when(dataServiceClient.obtenerTodosLosProductos()).thenReturn(Arrays.asList(p1, p2, p3));

        // Act
        List<ProductoDTO> filtrados = productoService.obtenerProductosFiltradosPorPrecio(BigDecimal.valueOf(40), BigDecimal.valueOf(60));

        // Assert
        assertEquals(1, filtrados.size());
        assertEquals(BigDecimal.valueOf(50), filtrados.get(0).getPrecio());
    }


    @Test
    @DisplayName("crearProducto: Valida precio negativo")
    void crearProducto_PrecioNegativo_LanzaValidacion() {
        ProductoRequest req = new ProductoRequest();
        req.setNombre("Test");
        req.setPrecio(BigDecimal.valueOf(-1));

        assertThrows(ValidacionNegocioException.class, () -> productoService.crearProducto(req));
        verifyNoInteractions(dataServiceClient);
    }

    @Test
    @DisplayName("crearProducto: Valida stock negativo")
    void crearProducto_StockNegativo_LanzaValidacion() {
        ProductoRequest req = new ProductoRequest();
        req.setNombre("Test");
        req.setPrecio(BigDecimal.TEN);
        req.setStock(-5);

        assertThrows(ValidacionNegocioException.class, () -> productoService.crearProducto(req));
    }

    @Test
    @DisplayName("crearProducto: Delegación exitosa")
    void crearProducto_Valido_LlamaCliente() {
        ProductoRequest req = new ProductoRequest();
        req.setNombre("Test");
        req.setPrecio(BigDecimal.TEN);

        ProductoDTO resp = new ProductoDTO();
        when(dataServiceClient.crearProducto(req)).thenReturn(resp);

        ProductoDTO resultado = productoService.crearProducto(req);

        assertNotNull(resultado);
        verify(dataServiceClient).crearProducto(req);
    }


    @Test
    @DisplayName("obtenerTodos: Envuelve FeignException en MicroserviceCommunicationException")
    void obtenerTodos_FallaComunicacion_LanzaCustomException() {
        when(dataServiceClient.obtenerTodosLosProductos()).thenThrow(new FeignException.ServiceUnavailable("Down",
                Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, new RequestTemplate()), null, null));

        assertThrows(MicroserviceCommunicationException.class, () -> productoService.obtenerTodosLosProductos());
    }
}
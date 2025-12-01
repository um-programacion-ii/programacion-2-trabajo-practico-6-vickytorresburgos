package com.example.businessservice.client;

import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
class DataServiceClientTest {

    @Autowired
    private DataServiceClient dataServiceClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("data.service.url", () -> "http://localhost:${wiremock.server.port}");
    }


    @Test
    @DisplayName("obtenerTodosLosProductos: Deserializa correctamente la lista JSON")
    void obtenerTodosLosProductos_Ok() {
        // Arrange
        stubFor(WireMock.get(WireMock.urlEqualTo("/data/productos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            [
                                {"id": 1, "nombre": "Laptop", "precio": 1500.00, "categoriaNombre": "Tech", "stock": 5, "stockBajo": true},
                                {"id": 2, "nombre": "Mouse", "precio": 20.00, "categoriaNombre": "Tech", "stock": 50, "stockBajo": false}
                            ]
                        """)));

        // Act
        List<ProductoDTO> productos = dataServiceClient.obtenerTodosLosProductos();

        // Assert
        assertNotNull(productos);
        assertEquals(2, productos.size());
        assertEquals("Laptop", productos.get(0).getNombre());
        assertTrue(productos.get(0).getStockBajo());

        // Verificación de protocolo HTTP
        verify(getRequestedFor(urlEqualTo("/data/productos")));
    }

    @Test
    @DisplayName("crearProducto: Serializa Request y Deserializa Response")
    void crearProducto_Ok() {
        // Arrange
        ProductoRequest request = new ProductoRequest("Teclado", "Mecánico", BigDecimal.valueOf(100), "Accesorios", 10);

        stubFor(WireMock.post(WireMock.urlEqualTo("/data/productos"))
                .withRequestBody(matchingJsonPath("$.nombre", equalTo("Teclado"))) // Valida JSON enviado
                .withRequestBody(matchingJsonPath("$.precio", equalTo("100")))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\": 10, \"nombre\": \"Teclado\"}")));

        // Act
        ProductoDTO response = dataServiceClient.crearProducto(request);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("actualizarCantidadInventario: Verifica la URL y payload correctos")
    void actualizarCantidadInventario_Ok() {
        // Arrange
        Long prodId = 1L;
        InventarioDTO payload = new InventarioDTO();
        payload.setCantidad(50);

        stubFor(WireMock.put(WireMock.urlEqualTo("/data/inventario/1/stock")) // Verifica ruta corregida
                .withRequestBody(matchingJsonPath("$.cantidad", equalTo("50")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\": 1, \"cantidad\": 50}")));

        // Act
        InventarioDTO result = dataServiceClient.actualizarCantidadInventario(prodId, payload);

        // Assert
        assertEquals(50, result.getCantidad());
    }

    @Test
    @DisplayName("obtenerProductoPorId: Lanza FeignException.NotFound en 404")
    void obtenerProductoPorId_404() {
        // Arrange
        stubFor(WireMock.get(WireMock.urlEqualTo("/data/productos/999"))
                .willReturn(aResponse().withStatus(404)));

        // Act & Assert
        assertThrows(FeignException.NotFound.class, () ->
                dataServiceClient.obtenerProductoPorId(999L)
        );
    }

    @Test
    @DisplayName("crearCategoria: Lanza FeignException.Conflict en 409")
    void crearCategoria_409() {
        // Arrange
        CategoriaDTO request = new CategoriaDTO(null, "Existente", "Desc");
        stubFor(WireMock.post(WireMock.urlEqualTo("/data/categorias"))
                .willReturn(aResponse().withStatus(409)));

        // Act & Assert
        assertThrows(FeignException.Conflict.class, () ->
                dataServiceClient.crearCategoria(request)
        );
    }

    @Test
    @DisplayName("obtenerProductosConStockBajo: Lanza ServiceUnavailable en 503")
    void obtenerStockBajo_503() {
        // Arrange
        stubFor(WireMock.get(WireMock.urlEqualTo("/data/inventario/stock-bajo"))
                .willReturn(aResponse().withStatus(503)));

        // Act & Assert
        assertThrows(FeignException.ServiceUnavailable.class, () ->
                dataServiceClient.obtenerProductosConStockBajo()
        );
    }
}
package com.example.businessservice.controller;

import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
class BusinessControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("POST /api/productos: Integra Business -> Feign -> WireMock (Exito)")
    void crearProducto_FlujoCompleto() {
        // Arrange
        stubFor(WireMock.post(WireMock.urlEqualTo("/data/productos"))
                .withRequestBody(WireMock.containing("Notebook Gamer"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "id": 100,
                                "nombre": "Notebook Gamer",
                                "precio": 1500.00,
                                "categoriaNombre": "Computacion",
                                "stock": 10,
                                "stockBajo": false
                            }
                        """)));

        ProductoRequest request = new ProductoRequest("Notebook Gamer", "Alta gama", BigDecimal.valueOf(1500), "Computacion", 10);

        // Act
        ResponseEntity<ProductoDTO> response = restTemplate.postForEntity("/api/productos", request, ProductoDTO.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getId());
        assertEquals("Notebook Gamer", response.getBody().getNombre());

        verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/data/productos")));
    }

    @Test
    @DisplayName("GET /api/productos/{id}: Manejo de 404 del Data Service")
    void obtenerProducto_NoExiste_Retorna404() {
        // Arrange
        Long id = 999L;
        stubFor(WireMock.get(WireMock.urlEqualTo("/data/productos/" + id))
                .willReturn(aResponse().withStatus(404)));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity("/api/productos/" + id, String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/productos: Manejo de Error 500 del Data Service (Resiliencia)")
    void obtenerProductos_DataServiceCaido_RetornaErrorServidor() {
        // Arrange
        stubFor(WireMock.get(WireMock.urlEqualTo("/data/productos"))
                .willReturn(aResponse().withStatus(500)));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity("/api/productos", String.class);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
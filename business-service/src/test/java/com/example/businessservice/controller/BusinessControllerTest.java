package com.example.businessservice.controller;

import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import com.example.businessservice.service.CategoriaBusinessService;
import com.example.businessservice.service.InventarioBusinessService;
import com.example.businessservice.service.ProductoBusinessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusinessController.class)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoBusinessService productoBusinessService;

    @MockBean
    private CategoriaBusinessService categoriaBusinessService;

    @MockBean
    private InventarioBusinessService inventarioBusinessService;

    // --- Tests de Productos ---

    @Test
    @DisplayName("GET /api/productos: Retorna lista de productos")
    void obtenerTodosLosProductos_RetornaLista() throws Exception {
        ProductoDTO p1 = new ProductoDTO(1L, "Laptop", "Desc", BigDecimal.valueOf(1000), "Tech", 10, false);
        when(productoBusinessService.obtenerTodosLosProductos()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Laptop")));
    }

    @Test
    @DisplayName("POST /api/productos: Crea producto y retorna 201")
    void crearProducto_RetornaCreated() throws Exception {
        ProductoRequest request = new ProductoRequest("Mouse", "Optico", BigDecimal.valueOf(20), "Tech", 50);
        ProductoDTO response = new ProductoDTO(1L, "Mouse", "Optico", BigDecimal.valueOf(20), "Tech", 50, false);

        when(productoBusinessService.crearProducto(any(ProductoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/productos/filtros: Pasa parámetros de precio correctamente")
    void obtenerProductosFiltrados_PasaParams() throws Exception {
        BigDecimal min = BigDecimal.valueOf(100);
        BigDecimal max = BigDecimal.valueOf(500);

        when(productoBusinessService.obtenerProductosFiltradosPorPrecio(min, max))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/productos/filtros")
                        .param("minPrice", "100")
                        .param("maxPrice", "500"))
                .andExpect(status().isOk());

        verify(productoBusinessService).obtenerProductosFiltradosPorPrecio(min, max);
    }

    @Test
    @DisplayName("GET /api/categorias/{nombre}/estadisticas: Retorna Map de estadísticas")
    void obtenerEstadisticas_RetornaMap() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", 10);

        when(categoriaBusinessService.calcularEstadisticasCategoria("Tech")).thenReturn(stats);

        mockMvc.perform(get("/api/categorias/Tech/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos", is(10)));
    }

    @Test
    @DisplayName("PUT /api/categorias/{id}: Actualiza y retorna OK")
    void actualizarCategoria_RetornaOk() throws Exception {
        Long id = 1L;
        CategoriaDTO request = new CategoriaDTO(null, "Tech Updated", "Desc");
        CategoriaDTO response = new CategoriaDTO(id, "Tech Updated", "Desc");

        when(categoriaBusinessService.actualizarCategoria(eq(id), any(CategoriaDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/categorias/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Tech Updated")));
    }

    @Test
    @DisplayName("PUT /api/reportes/{productoId}: Parsea correctamente el Map de cantidad")
    void actualizarCantidadInventario_ParseaMapCorrectamente() throws Exception {
        Long prodId = 1L;
        Integer nuevaCantidad = 50;

        Map<String, Object> payload = new HashMap<>();
        payload.put("cantidad", nuevaCantidad);

        InventarioDTO response = new InventarioDTO();
        response.setCantidad(nuevaCantidad);

        when(inventarioBusinessService.actualizarCantidadInventario(prodId, nuevaCantidad)).thenReturn(response);

        mockMvc.perform(put("/api/reportes/{productoId}", prodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad", is(50)));

        verify(inventarioBusinessService).actualizarCantidadInventario(prodId, nuevaCantidad);
    }

    @Test
    @DisplayName("POST /api/reportes/movimientos: Registra movimiento")
    void registrarMovimiento_RetornaCreated() throws Exception {
        // Arrange
        InventarioDTO request = new InventarioDTO();
        request.setCantidad(10);

        ProductoDTO productoSimulado = new ProductoDTO();
        productoSimulado.setId(1L);
        request.setProducto(productoSimulado);

        when(inventarioBusinessService.registrarMovimientoInventario(any(InventarioDTO.class)))
                .thenReturn(request);

        // Act & Assert
        mockMvc.perform(post("/api/reportes/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Ahora debería pasar
    }
}
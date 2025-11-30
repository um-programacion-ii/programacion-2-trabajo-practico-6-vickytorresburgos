package com.example.dataservice.controller;

import com.example.dataservice.dto.InventarioDTO;
import com.example.dataservice.dto.ProductoDTO;
import com.example.dataservice.dto.ProductoRequest;
import com.example.dataservice.service.CategoriaService;
import com.example.dataservice.service.InventarioService;
import com.example.dataservice.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataController.class)
class DataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private InventarioService inventarioService;

    @MockBean
    private CategoriaService categoriaService;


    @Test
    @DisplayName("GET /data/productos: Retorna 200 y lista de productos")
    void obtenerTodosLosProductos_RetornaLista() throws Exception {
        // Arrange
        ProductoDTO p1 = new ProductoDTO(1L, "Laptop", "Gamer", BigDecimal.valueOf(1000), "Tecno", 10, false);
        ProductoDTO p2 = new ProductoDTO(2L, "Mouse", "Optico", BigDecimal.valueOf(20), "Tecno", 50, false);
        List<ProductoDTO> lista = Arrays.asList(p1, p2);

        when(productoService.obtenerTodos()).thenReturn(lista);

        // Act & Assert
        mockMvc.perform(get("/data/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Laptop")))
                .andExpect(jsonPath("$[0].stock", is(10))) // Verificación extra
                .andExpect(jsonPath("$[1].nombre", is("Mouse")));
    }

    @Test
    @DisplayName("GET /data/productos/{id}: Retorna 200 y el producto")
    void obtenerProductoPorId_RetornaProducto() throws Exception {
        // Arrange
        Long id = 1L;
        ProductoDTO p1 = new ProductoDTO(id, "Laptop", "Gamer", BigDecimal.valueOf(1000), "Tecno", 5, true); // stockBajo = true

        when(productoService.buscarPorId(id)).thenReturn(p1);

        // Act & Assert
        mockMvc.perform(get("/data/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Laptop")))
                .andExpect(jsonPath("$.stockBajo", is(true)));
    }

    @Test
    @DisplayName("POST /data/productos: Retorna 201 Created")
    void crearProducto_RetornaCreated() throws Exception {
        // Arrange
        ProductoRequest request = new ProductoRequest("Teclado", "Mecánico", BigDecimal.valueOf(50), "Tecno", 10);
        // El DTO de respuesta simula lo que devolvería el servicio tras guardar
        ProductoDTO respuesta = new ProductoDTO(1L, "Teclado", "Mecánico", BigDecimal.valueOf(50), "Tecno", 10, false);

        when(productoService.crearProducto(any(ProductoRequest.class))).thenReturn(respuesta);

        // Act & Assert
        mockMvc.perform(post("/data/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Teclado")))
                .andExpect(jsonPath("$.stock", is(10)));
    }

    @Test
    @DisplayName("PUT /data/productos/{id}: Retorna 200 y producto actualizado")
    void actualizarProducto_RetornaActualizado() throws Exception {
        // Arrange
        Long id = 1L;
        ProductoRequest request = new ProductoRequest("Teclado v2", "RGB", BigDecimal.valueOf(60), "Tecno", 10);
        ProductoDTO respuesta = new ProductoDTO(id, "Teclado v2", "RGB", BigDecimal.valueOf(60), "Tecno", 10, false);

        when(productoService.actualizarProducto(eq(id), any(ProductoRequest.class))).thenReturn(respuesta);

        // Act & Assert
        mockMvc.perform(put("/data/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Teclado v2")));
    }

    @Test
    @DisplayName("DELETE /data/productos/{id}: Retorna 204 No Content")
    void eliminarProducto_RetornaNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(productoService).eliminarProducto(id);

        // Act & Assert
        mockMvc.perform(delete("/data/productos/{id}", id))
                .andExpect(status().isNoContent());

        verify(productoService).eliminarProducto(id);
    }

    @Test
    @DisplayName("GET /data/productos/categoria/{nombre}: Retorna lista filtrada")
    void obtenerProductosPorCategoria_RetornaLista() throws Exception {
        // Arrange
        String categoria = "Tecno";
        when(productoService.buscarPorCategoria(categoria)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/data/productos/categoria/{nombre}", categoria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("GET /data/inventario/stock-bajo: Retorna alertas de stock")
    void obtenerProductosConStockBajo_RetornaLista() throws Exception {
        // Arrange
        InventarioDTO inv = new InventarioDTO();
        inv.setCantidad(2);
        when(inventarioService.obtenerProductosConStockBajo()).thenReturn(List.of(inv));

        // Act & Assert
        mockMvc.perform(get("/data/inventario/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cantidad", is(2)));
    }

    @Test
    @DisplayName("GET /data/inventario: Retorna todo el inventario")
    void obtenerTodoElInventario_RetornaLista() throws Exception {
        when(inventarioService.obtenerTodo()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/data/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /data/inventario/{id}/stock: Actualiza stock simple")
    void actualizarStock_RetornaDTO() throws Exception {
        // Arrange
        Long id = 1L;
        Integer nuevoStock = 50;
        InventarioDTO dto = new InventarioDTO();
        dto.setId(id);
        dto.setCantidad(nuevoStock);
        when(inventarioService.actualizarStock(any(), any())).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(put("/data/inventario/{id}/stock", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoStock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad", is(50)));
    }
}
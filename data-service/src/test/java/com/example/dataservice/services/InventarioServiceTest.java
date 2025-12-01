package com.example.dataservice.services;

import com.example.dataservice.dto.InventarioDTO;
import com.example.dataservice.entity.Inventario;
import com.example.dataservice.exception.ProductoNoEncontradoException;
import com.example.dataservice.mapper.InventarioMapper;
import com.example.dataservice.repository.InventarioRepository;
import com.example.dataservice.service.InventarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private InventarioMapper inventarioMapper;

    @InjectMocks
    private InventarioService inventarioService;


    @Test
    @DisplayName("obtenerTodo: Debería retornar lista de DTOs cuando hay datos")
    void obtenerTodo_ConDatos_RetornaLista() {
        // Arrange
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        InventarioDTO dto = new InventarioDTO();

        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));
        when(inventarioMapper.toDTO(inventario)).thenReturn(dto);

        // Act
        List<InventarioDTO> resultado = inventarioService.obtenerTodo();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(inventarioRepository).findAll();
        verify(inventarioMapper).toDTO(inventario);
    }

    @Test
    @DisplayName("obtenerTodo: Debería retornar lista vacía si no hay registros")
    void obtenerTodo_SinDatos_RetornaVacio() {
        // Arrange
        when(inventarioRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<InventarioDTO> resultado = inventarioService.obtenerTodo();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(inventarioMapper, never()).toDTO(any());
    }


    @Test
    @DisplayName("actualizarStock: Debería actualizar cantidad y fecha cuando el producto existe")
    void actualizarStock_ProductoExiste_ActualizaCorrectamente() {
        // Arrange
        Long productoId = 100L;
        Integer nuevaCantidad = 50;

        Inventario inventarioExistente = new Inventario();
        inventarioExistente.setId(1L);
        inventarioExistente.setCantidad(10); // Valor anterior

        Inventario inventarioGuardado = new Inventario();
        inventarioGuardado.setId(1L);
        inventarioGuardado.setCantidad(nuevaCantidad);

        InventarioDTO dtoEsperado = new InventarioDTO();
        dtoEsperado.setCantidad(nuevaCantidad);

        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventarioExistente));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioGuardado);
        when(inventarioMapper.toDTO(inventarioGuardado)).thenReturn(dtoEsperado);

        // Act
        InventarioDTO resultado = inventarioService.actualizarStock(productoId, nuevaCantidad);

        // Assert
        assertNotNull(resultado);
        assertEquals(nuevaCantidad, resultado.getCantidad());

        assertEquals(nuevaCantidad, inventarioExistente.getCantidad());
        assertNotNull(inventarioExistente.getFechaActualizacion(), "La fecha de actualización debe asignarse");

        verify(inventarioRepository).save(inventarioExistente);
    }

    @Test
    @DisplayName("actualizarStock: Debería lanzar excepción si el producto no existe")
    void actualizarStock_ProductoNoExiste_LanzaExcepcion() {
        // Arrange
        Long productoId = 999L;
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductoNoEncontradoException excepcion = assertThrows(ProductoNoEncontradoException.class, () ->
                inventarioService.actualizarStock(productoId, 10)
        );

        assertTrue(excepcion.getMessage().contains("Producto no encontrado"));
        verify(inventarioRepository, never()).save(any());
    }


    @Test
    @DisplayName("obtenerProductosConStockBajo: Debería retornar items filtrados")
    void obtenerProductosConStockBajo_ExistenItems_RetornaLista() {
        // Arrange
        Inventario invBajo = new Inventario();
        InventarioDTO dtoBajo = new InventarioDTO();

        when(inventarioRepository.findProductosConStockBajo()).thenReturn(List.of(invBajo));
        when(inventarioMapper.toDTO(invBajo)).thenReturn(dtoBajo);

        // Act
        List<InventarioDTO> resultado = inventarioService.obtenerProductosConStockBajo();

        // Assert
        assertEquals(1, resultado.size());
        verify(inventarioRepository).findProductosConStockBajo();
    }

    @Test
    @DisplayName("obtenerProductosConStockBajo: Debería retornar vacío si no hay alertas")
    void obtenerProductosConStockBajo_SinItems_RetornaVacio() {
        // Arrange
        when(inventarioRepository.findProductosConStockBajo()).thenReturn(Collections.emptyList());

        // Act
        List<InventarioDTO> resultado = inventarioService.obtenerProductosConStockBajo();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(inventarioMapper, never()).toDTO(any());
    }
}

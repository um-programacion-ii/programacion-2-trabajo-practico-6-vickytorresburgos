package com.example.dataservice.service;

import com.example.dataservice.dto.ProductoDTO;
import com.example.dataservice.dto.ProductoRequest;
import com.example.dataservice.entity.Categoria;
import com.example.dataservice.entity.Inventario;
import com.example.dataservice.entity.Producto;
import com.example.dataservice.exception.CategoriaNoEncontradaException;
import com.example.dataservice.exception.ProductoNoEncontradoException;
import com.example.dataservice.exception.ValidacionNegocioException;
import com.example.dataservice.mapper.ProductoMapper;
import com.example.dataservice.repository.CategoriaRepository;
import com.example.dataservice.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;


    @Test
    @DisplayName("obtenerTodos: Retorna lista de productos mapeados")
    void obtenerTodos_ExistenDatos_RetornaLista() {
        // Arrange
        Producto prod = new Producto();
        prod.setId(1L);
        ProductoDTO dto = new ProductoDTO();

        when(productoRepository.findAll()).thenReturn(List.of(prod));
        when(productoMapper.toDTO(prod)).thenReturn(dto);

        // Act
        List<ProductoDTO> resultado = productoService.obtenerTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(productoRepository).findAll();
    }


    @Test
    @DisplayName("buscarPorId: Retorna DTO si existe")
    void buscarPorId_IdExiste_RetornaDTO() {
        // Arrange
        Long id = 1L;
        Producto prod = new Producto();
        ProductoDTO dto = new ProductoDTO();

        when(productoRepository.findById(id)).thenReturn(Optional.of(prod));
        when(productoMapper.toDTO(prod)).thenReturn(dto);

        // Act
        ProductoDTO resultado = productoService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository).findById(id);
    }

    @Test
    @DisplayName("buscarPorId: Lanza excepción si no existe")
    void buscarPorId_IdNoExiste_LanzaExcepcion() {
        // Arrange
        Long id = 99L;
        when(productoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoNoEncontradoException.class, () -> productoService.buscarPorId(id));
    }


    @Test
    @DisplayName("buscarPorCategoria: Retorna productos filtrados")
    void buscarPorCategoria_CategoriaExiste_RetornaLista() {
        // Arrange
        String nombreCat = "Electrónica";
        Producto prod = new Producto();
        ProductoDTO dto = new ProductoDTO();

        when(productoRepository.findByCategoriaNombre(nombreCat)).thenReturn(List.of(prod));
        when(productoMapper.toDTO(prod)).thenReturn(dto);

        // Act
        List<ProductoDTO> result = productoService.buscarPorCategoria(nombreCat);

        // Assert
        assertEquals(1, result.size());
        verify(productoRepository).findByCategoriaNombre(nombreCat);
    }


    @Test
    @DisplayName("crearProducto: Guarda producto e inventario con datos válidos")
    void crearProducto_DatosValidos_GuardaCorrectamente() {
        // Arrange
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Mouse");
        request.setCategoriaNombre("Periféricos");
        request.setStock(50);
        request.setPrecio(BigDecimal.valueOf(100));

        Categoria categoriaMock = new Categoria();
        Producto productoGuardado = new Producto();
        ProductoDTO dtoEsperado = new ProductoDTO();

        when(categoriaRepository.findByNombre(request.getCategoriaNombre())).thenReturn(Optional.of(categoriaMock));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);
        when(productoMapper.toDTO(productoGuardado)).thenReturn(dtoEsperado);

        // Act
        ProductoDTO resultado = productoService.crearProducto(request);

        // Assert
        assertNotNull(resultado);
        verify(categoriaRepository).findByNombre("Periféricos");
        verify(productoRepository).save(any(Producto.class)); // Verifica que se llamó a guardar
    }

    @Test
    @DisplayName("crearProducto: Lanza excepción si el stock es negativo")
    void crearProducto_StockNegativo_LanzaValidacionException() {
        // Arrange
        ProductoRequest request = new ProductoRequest();
        request.setStock(-5);

        // Act & Assert
        ValidacionNegocioException ex = assertThrows(ValidacionNegocioException.class, () ->
                productoService.crearProducto(request)
        );

        assertEquals("El stock no puede ser negativo", ex.getMessage());
        verifyNoInteractions(productoRepository); // Asegura que nada se guardó
    }

    @Test
    @DisplayName("crearProducto: Lanza excepción si la categoría no existe")
    void crearProducto_CategoriaInexistente_LanzaCategoriaNoEncontradaException() {
        // Arrange
        ProductoRequest request = new ProductoRequest();
        request.setStock(10);
        request.setCategoriaNombre("Fantasma");

        when(categoriaRepository.findByNombre("Fantasma")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoriaNoEncontradaException.class, () ->
                productoService.crearProducto(request)
        );
        verify(productoRepository, never()).save(any());
    }


    @Test
    @DisplayName("actualizarProducto: Actualiza datos y stock si todo es válido")
    void actualizarProducto_Valido_ActualizaExitosamente() {
        // Arrange
        Long id = 1L;
        ProductoRequest request = new ProductoRequest();
        request.setStock(20);
        request.setCategoriaNombre("NuevaCat");

        Inventario inventarioExistente = new Inventario(); // Necesario para evitar NullPointerException
        Producto productoExistente = new Producto();
        productoExistente.setInventario(inventarioExistente);

        Categoria nuevaCategoria = new Categoria();
        ProductoDTO dto = new ProductoDTO();

        when(productoRepository.findById(id)).thenReturn(Optional.of(productoExistente));
        when(categoriaRepository.findByNombre("NuevaCat")).thenReturn(Optional.of(nuevaCategoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoExistente);
        when(productoMapper.toDTO(productoExistente)).thenReturn(dto);

        // Act
        ProductoDTO resultado = productoService.actualizarProducto(id, request);

        // Assert
        assertNotNull(resultado);
        // Verificamos que se actualizó el inventario dentro del producto
        assertEquals(20, inventarioExistente.getCantidad());
        verify(productoRepository).save(productoExistente);
    }

    @Test
    @DisplayName("actualizarProducto: Lanza excepción si el producto no existe")
    void actualizarProducto_ProductoNoExiste_LanzaException() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoNoEncontradoException.class, () ->
                productoService.actualizarProducto(1L, new ProductoRequest())
        );
    }


    @Test
    @DisplayName("eliminarProducto: Elimina si existe")
    void eliminarProducto_Existe_Elimina() {
        // Arrange
        Long id = 1L;
        when(productoRepository.existsById(id)).thenReturn(true);

        // Act
        productoService.eliminarProducto(id);

        // Assert
        verify(productoRepository).deleteById(id);
    }

    @Test
    @DisplayName("eliminarProducto: Lanza excepción si no existe")
    void eliminarProducto_NoExiste_LanzaException() {
        // Arrange
        Long id = 1L;
        when(productoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductoNoEncontradoException.class, () ->
                productoService.eliminarProducto(id)
        );
        verify(productoRepository, never()).deleteById(any());
    }
}
package com.example.dataservice.services;

import com.example.dataservice.dto.CategoriaDTO;
import com.example.dataservice.dto.CategoriaRequest;
import com.example.dataservice.entity.Categoria;
import com.example.dataservice.entity.Producto;
import com.example.dataservice.exception.CategoriaNoEncontradaException;
import com.example.dataservice.exception.ValidacionNegocioException;
import com.example.dataservice.mapper.CategoriaMapper;
import com.example.dataservice.repository.CategoriaRepository;
import com.example.dataservice.repository.ProductoRepository;
import com.example.dataservice.service.CategoriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;


    @Test
    @DisplayName("obtenerTodas: Debería retornar lista de CategoriaDTO cuando existen datos")
    void obtenerTodas_CuandoExistenDatos_RetornaLista() {
        // Arrange
        Categoria cat1 = new Categoria(1L, "Electrónica", "Desc 1", null);
        Categoria cat2 = new Categoria(2L, "Hogar", "Desc 2", null);
        List<Categoria> categorias = Arrays.asList(cat1, cat2);

        CategoriaDTO dto1 = new CategoriaDTO(1L, "Electrónica", "Desc 1");
        CategoriaDTO dto2 = new CategoriaDTO(2L, "Hogar", "Desc 2");

        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(categoriaMapper.toDTO(cat1)).thenReturn(dto1);
        when(categoriaMapper.toDTO(cat2)).thenReturn(dto2);

        // Act
        List<CategoriaDTO> resultado = categoriaService.obtenerTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Electrónica", resultado.get(0).getNombre());
        verify(categoriaRepository).findAll();
    }


    @Test
    @DisplayName("buscarPorId: Debería retornar DTO cuando el ID existe")
    void buscarPorId_CuandoIdExiste_RetornaDTO() {
        // Arrange
        Long id = 1L;
        Categoria categoria = new Categoria(id, "Tecnología", "Descripción", null);
        CategoriaDTO dtoEsperado = new CategoriaDTO(id, "Tecnología", "Descripción");

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDTO(categoria)).thenReturn(dtoEsperado);

        // Act
        CategoriaDTO resultado = categoriaService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(dtoEsperado.getNombre(), resultado.getNombre());
    }

    @Test
    @DisplayName("buscarPorId: Debería lanzar excepción cuando el ID no existe")
    void buscarPorId_CuandoIdNoExiste_LanzaExcepcion() {
        // Arrange
        Long id = 99L;
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoriaNoEncontradaException.class, () -> {
            categoriaService.buscarPorId(id);
        });

        verify(categoriaMapper, never()).toDTO(any());
    }


    @Test
    @DisplayName("crearCategoria: Debería guardar y retornar DTO cuando el nombre es único")
    void crearCategoria_CuandoNombreEsUnico_GuardaCorrectamente() {
        // Arrange
        CategoriaRequest request = new CategoriaRequest("Deportes", "Artículos deportivos");
        Categoria categoriaGuardada = new Categoria(1L, "Deportes", "Artículos deportivos", null);
        CategoriaDTO dtoEsperado = new CategoriaDTO(1L, "Deportes", "Artículos deportivos");

        when(categoriaRepository.findByNombre(request.getNombre())).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaGuardada);
        when(categoriaMapper.toDTO(categoriaGuardada)).thenReturn(dtoEsperado);

        // Act
        CategoriaDTO resultado = categoriaService.crearCategoria(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Deportes", resultado.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("crearCategoria: Debería lanzar excepción cuando el nombre ya existe")
    void crearCategoria_CuandoNombreDuplicado_LanzaExcepcion() {
        // Arrange
        CategoriaRequest request = new CategoriaRequest("Deportes", "Artículos deportivos");

        when(categoriaRepository.findByNombre(request.getNombre()))
                .thenReturn(Optional.of(new Categoria())); // Simula que ya existe

        // Act & Assert
        ValidacionNegocioException excepcion = assertThrows(ValidacionNegocioException.class, () -> {
            categoriaService.crearCategoria(request);
        });

        assertEquals("Ya existe una categoría con el nombre: Deportes", excepcion.getMessage());
        verify(categoriaRepository, never()).save(any());
    }


    @Test
    @DisplayName("actualizarCategoria: Debería actualizar cuando los datos son válidos")
    void actualizarCategoria_DatosValidos_ActualizaCorrectamente() {
        // Arrange
        Long id = 1L;
        CategoriaRequest request = new CategoriaRequest("Nuevo Nombre", "Nueva Desc");
        Categoria categoriaExistente = new Categoria(id, "Viejo Nombre", "Vieja Desc", null);
        Categoria categoriaActualizada = new Categoria(id, "Nuevo Nombre", "Nueva Desc", null);
        CategoriaDTO dtoEsperado = new CategoriaDTO(id, "Nuevo Nombre", "Nueva Desc");

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoriaExistente));
        when(categoriaRepository.findByNombre(request.getNombre())).thenReturn(Optional.empty()); // Nombre libre
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaActualizada);
        when(categoriaMapper.toDTO(categoriaActualizada)).thenReturn(dtoEsperado);

        // Act
        CategoriaDTO resultado = categoriaService.actualizarCategoria(id, request);

        // Assert
        assertEquals("Nuevo Nombre", resultado.getNombre());
        verify(categoriaRepository).save(categoriaExistente);
    }

    @Test
    @DisplayName("actualizarCategoria: Debería lanzar excepción si el nombre nuevo ya lo usa otra categoría")
    void actualizarCategoria_NombreEnUsoPorOtro_LanzaExcepcion() {
        // Arrange
        Long id = 1L;
        CategoriaRequest request = new CategoriaRequest("Nombre Duplicado", "Desc");
        Categoria categoriaAActualizar = new Categoria(id, "Original", "Desc", null);
        Categoria otraCategoria = new Categoria(2L, "Nombre Duplicado", "Otra Desc", null); // ID diferente

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoriaAActualizar));
        when(categoriaRepository.findByNombre(request.getNombre())).thenReturn(Optional.of(otraCategoria));

        // Act & Assert
        assertThrows(ValidacionNegocioException.class, () -> {
            categoriaService.actualizarCategoria(id, request);
        });

        verify(categoriaRepository, never()).save(any());
    }


    @Test
    @DisplayName("borrarCategoria: Debería eliminar si no tiene productos asociados")
    void borrarCategoria_SinProductos_EliminaCorrectamente() {
        // Arrange
        Long id = 1L;
        Categoria categoria = new Categoria(id, "Vacia", "Desc", null);

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaNombre(categoria.getNombre())).thenReturn(Collections.emptyList());

        // Act
        categoriaService.borrarCategoria(id);

        // Assert
        verify(categoriaRepository).deleteById(id);
    }

    @Test
    @DisplayName("borrarCategoria: Debería lanzar excepción si tiene productos asociados")
    void borrarCategoria_ConProductos_LanzaExcepcion() {
        // Arrange
        Long id = 1L;
        Categoria categoria = new Categoria(id, "Ocupada", "Desc", null);
        List<Producto> productosAsociados = List.of(new Producto());

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaNombre(categoria.getNombre())).thenReturn(productosAsociados);

        // Act & Assert
        ValidacionNegocioException excepcion = assertThrows(ValidacionNegocioException.class, () -> {
            categoriaService.borrarCategoria(id);
        });

        assertTrue(excepcion.getMessage().contains("Existen productos asociados"));
        verify(categoriaRepository, never()).deleteById(any());
    }
}
package com.example.businessservice.services;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.exceptions.InventarioNoEncontradoException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import com.example.businessservice.service.InventarioBusinessService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private InventarioBusinessService inventarioService;

    private FeignException crearFeignNotFound() {
        return new FeignException.NotFound("No encontrado",
                Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, new RequestTemplate()), null, null);
    }

    @Test
    @DisplayName("actualizarCantidadInventario: Lanza excepción si cantidad es negativa")
    void actualizarCantidad_Negativa_LanzaValidacion() {
        assertThrows(ValidacionNegocioException.class,
                () -> inventarioService.actualizarCantidadInventario(1L, -5));
    }

    @Test
    @DisplayName("actualizarCantidadInventario: Llama al cliente con payload correcto")
    void actualizarCantidad_Valido_LlamaCliente() {
        // Arrange
        Long prodId = 1L;
        Integer cantidad = 50;
        InventarioDTO mockResponse = new InventarioDTO();
        mockResponse.setCantidad(cantidad);

        when(dataServiceClient.actualizarCantidadInventario(eq(prodId), any(InventarioDTO.class)))
                .thenReturn(mockResponse);

        // Act
        InventarioDTO result = inventarioService.actualizarCantidadInventario(prodId, cantidad);

        // Assert
        assertEquals(cantidad, result.getCantidad());
        verify(dataServiceClient).actualizarCantidadInventario(eq(prodId), any(InventarioDTO.class));
    }

    @Test
    @DisplayName("registrarMovimientoInventario: Lanza validación si producto o id es nulo")
    void registrarMovimiento_Invalido_LanzaExcepcion() {
        InventarioDTO mov = new InventarioDTO(); // Sin producto
        assertThrows(ValidacionNegocioException.class,
                () -> inventarioService.registrarMovimientoInventario(mov));
    }

    @Test
    @DisplayName("obtenerInventarioPorProductoId: Mapea NotFound de Feign a excepción de negocio")
    void obtenerInventario_NoExiste_LanzaExcepcionNegocio() {
        when(dataServiceClient.obtenerInventarioPorProductoId(99L)).thenThrow(crearFeignNotFound());

        assertThrows(InventarioNoEncontradoException.class,
                () -> inventarioService.obtenerInventarioPorProductoId(99L));
    }
}
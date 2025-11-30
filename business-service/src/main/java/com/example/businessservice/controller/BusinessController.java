package com.example.businessservice.controller;

import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import com.example.businessservice.service.CategoriaBusinessService;
import com.example.businessservice.service.InventarioBusinessService;
import com.example.businessservice.service.ProductoBusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BusinessController {
    private final ProductoBusinessService productoBusinessService;
    private final CategoriaBusinessService categoriaBusinessService;
    private final InventarioBusinessService inventarioBusinessService;

    public BusinessController(ProductoBusinessService productoBusinessService,
                              CategoriaBusinessService categoriaBusinessService,
                              InventarioBusinessService inventarioBusinessService) {
        this.productoBusinessService = productoBusinessService;
        this.categoriaBusinessService = categoriaBusinessService;
        this.inventarioBusinessService = inventarioBusinessService;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        return ResponseEntity.ok(productoBusinessService.obtenerTodosLosProductos());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoBusinessService.obtenerProductoPorId(id));
    }

    @PostMapping("productos")
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoRequest request) {
        return new ResponseEntity<>(productoBusinessService.crearProducto(request), HttpStatus.CREATED);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable("id") Long id,
                                                          @Valid @RequestBody ProductoRequest request) {
        ProductoDTO actualizado = productoBusinessService.actualizarProducto(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable("id") Long id) {
        productoBusinessService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/productos/categoria/{nombre}")
    public List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable String nombre) {
        return productoBusinessService.obtenerProductosPorCategoria(nombre);
    }

    @GetMapping("/productos/filtros")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosFiltradosPorPrecio(
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(productoBusinessService.obtenerProductosFiltradosPorPrecio(minPrice, maxPrice));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> obtenerTodasLasCategorias() {
        return ResponseEntity.ok(categoriaBusinessService.obtenerTodasLasCategorias());
    }

    @GetMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> obtenerCategoriaPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoriaBusinessService.obtenerCategoriaPorId(id));
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CategoriaDTO request) {
        CategoriaDTO creado = categoriaBusinessService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable("id") Long id,
                                                            @Valid @RequestBody CategoriaDTO request) {
        CategoriaDTO actualizado = categoriaBusinessService.actualizarCategoria(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable("id") Long id) {
        categoriaBusinessService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorias/{nombre}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCategoria(@PathVariable("nombre") String nombre) {
        Map<String, Object> stats = categoriaBusinessService.calcularEstadisticasCategoria(nombre);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reportes/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> obtenerProductosConStockBajo() {
        return ResponseEntity.ok(inventarioBusinessService.obtenerProductosConStockBajo());
    }

    @GetMapping("/reportes/producto/{productoId}")
    public ResponseEntity<InventarioDTO> obtenerInventarioPorProducto(@PathVariable("productoId") Long productoId) {
        return ResponseEntity.ok(inventarioBusinessService.obtenerInventarioPorProductoId(productoId));
    }

    @PutMapping("/reportes/{productoId}")
    public ResponseEntity<InventarioDTO> actualizarCantidadInventario(
            @PathVariable("productoId") Long productoId,
            @RequestBody Map<String, Object> payload) {
        Integer nuevaCantidad = null;
        if (payload != null && payload.get("cantidad") != null) {
            Object val = payload.get("cantidad");
            if (val instanceof Number) {
                nuevaCantidad = ((Number) val).intValue();
            } else {
                try {
                    nuevaCantidad = Integer.valueOf(val.toString());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        InventarioDTO actualizado = inventarioBusinessService.actualizarCantidadInventario(productoId, nuevaCantidad);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/reportes/movimientos")
    public ResponseEntity<InventarioDTO> registrarMovimientoInventario(@Valid @RequestBody InventarioDTO movimiento) {
        InventarioDTO registrado = inventarioBusinessService.registrarMovimientoInventario(movimiento);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
    }
}

package com.example.businessservice.client;

import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@FeignClient(name = "data-service", url = "${data.service.url}")
public interface DataServiceClient {

    @GetMapping("/data/productos")
    List<ProductoDTO> obtenerTodosLosProductos();

    @GetMapping("/data/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable Long id);

    @PostMapping("/data/productos")
    ProductoDTO crearProducto(@RequestBody ProductoRequest request);

    @PutMapping("/data/productos/{id}")
    ProductoDTO actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request);

    @DeleteMapping("/data/productos/{id}")
    void eliminarProducto(@PathVariable Long id);

    @GetMapping("/data/productos/categoria/{nombre}")
    List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable String nombre);

    @GetMapping("/data/productos/filtros")
    List<ProductoDTO> obtenerProductosFiltrados();

    @GetMapping("/data/categorias")
    List<CategoriaDTO> obtenerTodasLasCategorias();

    @GetMapping("/data/categorias/{id}")
    CategoriaDTO obtenerCategoriaPorId(@PathVariable("id") Long id);

    @PostMapping("/data/categorias")
    CategoriaDTO crearCategoria(@RequestBody CategoriaDTO request);

    @PutMapping("/data/categorias/{id}")
    CategoriaDTO actualizarCategoria(@PathVariable("id") Long id, @RequestBody CategoriaDTO request);

    @DeleteMapping("/data/categorias/{id}")
    void eliminarCategoria(@PathVariable("id") Long id);

    @GetMapping("/data/categorias/{nombre}/estadisticas")
    Map<String, Object> obtenerEstadisticasCategoria(@PathVariable("nombre") String nombre);

    @GetMapping("/data/inventario")
    List<InventarioDTO> obtenerProductosConStockBajo();

    @GetMapping("/data/inventario/producto/{productoId}")
    InventarioDTO obtenerInventarioPorProductoId(@PathVariable("productoId") Long productoId);

    @PutMapping("/data/inventario/{productoId}")
    InventarioDTO actualizarCantidadInventario(@PathVariable("productoId") Long productoId,
                                               @RequestBody InventarioDTO payload);

    @PostMapping("/data/inventario/movimientos")
    InventarioDTO registrarMovimientoInventario(@RequestBody InventarioDTO movimiento);
}

package com.example.dataservice.controller;

import com.example.dataservice.dto.ProductoDTO;
import com.example.dataservice.dto.ProductoRequest;
import com.example.dataservice.entity.Inventario;
import com.example.dataservice.entity.Producto;
import com.example.dataservice.service.CategoriaService;
import com.example.dataservice.service.InventarioService;
import com.example.dataservice.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la capa de datos del sistema de microservicios.
 */
@RestController
@RequestMapping("/data")
@Validated
public class DataController {
    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final InventarioService inventarioService;

    /**
     * Constructor para inyección de dependencias.
     * @param productoService
     * @param categoriaService
     * @param inventarioService
     */

    public DataController(ProductoService productoService, CategoriaService categoriaService, InventarioService inventarioService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.inventarioService = inventarioService;
    }

    /**
     * Obtiene todos los productos almacenados en la base de datos.
     * @return Lista de DTO de Producto
     */
    @GetMapping("/productos")
    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoService.obtenerTodos();
    }

    /**
     * Busca y retorna un producto por su id.
     * @param id Identificador del producto
     * @return DTO de producto
     */
    @GetMapping("/productos/{id}")
    public ProductoDTO obtenerProductoPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }


    /**
     * Crea un nuevo DTO de producto
     * @return
     */
    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDTO crearProducto(@RequestBody ProductoRequest producto) {
        return productoService.crearProducto(producto);
    }


    /**
     * Actualiza un producto existente
     * @param id Identificador del producto
     * @param producto datos actualizados del producto
     * @return
     */
    @PutMapping("/productos/{id}")
    public ProductoDTO actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest producto) {
        return productoService.actualizarProducto(id, producto);
    }

    /**
     * Elimina un producto
     * @param id Identificador del producto a eliminar
     */
    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
    }


    /**
     * Busca producto por categoria
     * @param nombre Nombre de la categoria
     * @return Lista de productos de la categoria
     */
    @GetMapping("/productos/categoria/{nombre}")
    public List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable String nombre) {
        return productoService.buscarPorCategoria(nombre);
    }


    /**
     * Obtiene el inventario de productos con stock bajo según criterios establecidos
     * @return Lista de DTO de inventario con stock bajo
     */
    @GetMapping("/inventario/stock-bajo")
    public List<Inventario> obtenerProductosConStockBajo() {
        return inventarioService.obtenerProductosConStockBajo();
    }

    /**
     * Obtiene el registro completo del inventario
     * @return Lista completa de Inventario DTO
     */
    @GetMapping("/inventario")
    public List<Inventario> obtenerTodoElInventario() {
        return inventarioService.obtenerTodo();
    }
}

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

/**
 * Controlador REST que expone la lógica de negocio del sistema.
 */
@RestController
@RequestMapping("/api")
public class BusinessController {
    private final ProductoBusinessService productoBusinessService;
    private final CategoriaBusinessService categoriaBusinessService;
    private final InventarioBusinessService inventarioBusinessService;

    /**
     * Constructor para la inyección de dependencias de los servicios de negocio.
     *
     * @param productoBusinessService   Servicio para gestión de productos.
     * @param categoriaBusinessService  Servicio para gestión de categorías.
     * @param inventarioBusinessService Servicio para gestión de inventario y reportes.
     */
    public BusinessController(ProductoBusinessService productoBusinessService,
                              CategoriaBusinessService categoriaBusinessService,
                              InventarioBusinessService inventarioBusinessService) {
        this.productoBusinessService = productoBusinessService;
        this.categoriaBusinessService = categoriaBusinessService;
        this.inventarioBusinessService = inventarioBusinessService;
    }

    /**
     * Obtiene el catálogo completo de productos.
     *
     * @return ResponseEntity con la lista de {@link ProductoDTO} y estado 200 OK.
     */
    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        return ResponseEntity.ok(productoBusinessService.obtenerTodosLosProductos());
    }

    /**
     * Busca un producto específico por su ID.
     *
     * @param id Identificador único del producto.
     * @return ResponseEntity con el {@link ProductoDTO} encontrado y estado 200 OK.
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoBusinessService.obtenerProductoPorId(id));
    }

    /**
     * Crea un nuevo producto en el sistema.
     *
     * @param request Objeto {@link ProductoRequest} con los datos del nuevo producto.
     * @return ResponseEntity con el producto creado y estado 201 Created.
     */
    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoRequest request) {
        return new ResponseEntity<>(productoBusinessService.crearProducto(request), HttpStatus.CREATED);
    }

    /**
     * Actualiza la información de un producto existente.
     *
     * @param id      Identificador del producto a modificar.
     * @param request Objeto {@link ProductoRequest} con los datos actualizados.
     * @return ResponseEntity con el producto actualizado y estado 200 OK.
     */
    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable("id") Long id,
                                                          @Valid @RequestBody ProductoRequest request) {
        ProductoDTO actualizado = productoBusinessService.actualizarProducto(id, request);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina un producto del sistema.
     *
     * @param id Identificador del producto a eliminar.
     * @return ResponseEntity sin contenido y estado 204 No Content.
     */
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable("id") Long id) {
        productoBusinessService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Filtra productos pertenecientes a una categoría específica.
     *
     * @param nombre Nombre de la categoría.
     * @return Lista de {@link ProductoDTO} asociados a la categoría.
     */
    @GetMapping("/productos/categoria/{nombre}")
    public List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable String nombre) {
        return productoBusinessService.obtenerProductosPorCategoria(nombre);
    }

    /**
     * Realiza una búsqueda avanzada de productos filtrando por rango de precios.
     *
     * @param minPrice Precio mínimo (opcional).
     * @param maxPrice Precio máximo (opcional).
     * @return ResponseEntity con la lista de productos filtrada y estado 200 OK.
     */
    @GetMapping("/productos/filtros")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosFiltradosPorPrecio(
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(productoBusinessService.obtenerProductosFiltradosPorPrecio(minPrice, maxPrice));
    }

    /**
     * Obtiene todas las categorías disponibles.
     *
     * @return ResponseEntity con la lista de {@link CategoriaDTO} y estado 200 OK.
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> obtenerTodasLasCategorias() {
        return ResponseEntity.ok(categoriaBusinessService.obtenerTodasLasCategorias());
    }

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id Identificador de la categoría.
     * @return ResponseEntity con el {@link CategoriaDTO} y estado 200 OK.
     */
    @GetMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> obtenerCategoriaPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoriaBusinessService.obtenerCategoriaPorId(id));
    }

    /**
     * Crea una nueva categoría.
     *
     * @param request Objeto {@link CategoriaDTO} con los datos de la categoría.
     * @return ResponseEntity con la categoría creada y estado 201 Created.
     */
    @PostMapping("/categorias")
    public ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CategoriaDTO request) {
        CategoriaDTO creado = categoriaBusinessService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Actualiza una categoría existente.
     *
     * @param id      Identificador de la categoría.
     * @param request Objeto {@link CategoriaDTO} con los nuevos datos.
     * @return ResponseEntity con la categoría actualizada y estado 200 OK.
     */
    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable("id") Long id,
                                                            @Valid @RequestBody CategoriaDTO request) {
        CategoriaDTO actualizado = categoriaBusinessService.actualizarCategoria(id, request);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina una categoría del sistema.
     *
     * @param id Identificador de la categoría.
     * @return ResponseEntity sin contenido y estado 204 No Content.
     */
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable("id") Long id) {
        categoriaBusinessService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Genera estadísticas de negocio para una categoría específica (ej. total productos, valor inventario).
     *
     * @param nombre Nombre de la categoría.
     * @return ResponseEntity con un Mapa de estadísticas y estado 200 OK.
     */
    @GetMapping("/categorias/{nombre}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCategoria(@PathVariable("nombre") String nombre) {
        Map<String, Object> stats = categoriaBusinessService.calcularEstadisticasCategoria(nombre);
        return ResponseEntity.ok(stats);
    }

    /**
     * Reporte de productos que se encuentran por debajo del stock mínimo.
     *
     * @return ResponseEntity con lista de {@link InventarioDTO} críticos y estado 200 OK.
     */
    @GetMapping("/reportes/stock-bajo")
    public ResponseEntity<List<InventarioDTO>> obtenerProductosConStockBajo() {
        return ResponseEntity.ok(inventarioBusinessService.obtenerProductosConStockBajo());
    }

    /**
     * Consulta el estado del inventario para un producto específico.
     *
     * @param productoId Identificador del producto.
     * @return ResponseEntity con el {@link InventarioDTO} y estado 200 OK.
     */
    @GetMapping("/reportes/producto/{productoId}")
    public ResponseEntity<InventarioDTO> obtenerInventarioPorProducto(@PathVariable("productoId") Long productoId) {
        return ResponseEntity.ok(inventarioBusinessService.obtenerInventarioPorProductoId(productoId));
    }

    /**
     * Actualiza manualmente la cantidad de stock para un producto.
     *
     * @param productoId Identificador del producto.
     * @param payload    Mapa JSON conteniendo la clave "cantidad" con el nuevo valor entero.
     * @return ResponseEntity con el inventario actualizado y estado 200 OK.
     */
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

    /**
     * Registra un movimiento de inventario (entrada/salida) y actualiza el stock.
     *
     * @param movimiento Objeto {@link InventarioDTO} con los detalles del movimiento.
     * @return ResponseEntity con el estado del inventario resultante y estado 201 Created.
     */
    @PostMapping("/reportes/movimientos")
    public ResponseEntity<InventarioDTO> registrarMovimientoInventario(@Valid @RequestBody InventarioDTO movimiento) {
        InventarioDTO registrado = inventarioBusinessService.registrarMovimientoInventario(movimiento);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
    }
}
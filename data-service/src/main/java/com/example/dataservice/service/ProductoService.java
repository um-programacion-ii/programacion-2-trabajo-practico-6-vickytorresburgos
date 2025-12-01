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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaRepository categoriaRepository;

    /**
     * Constructor para la inyección de dependencias.
     * @param productoRepository Repositorio JPA para la entidad Producto.
     * @param productoMapper Mapper para convertir entre entidades y DTOs.
     * @param categoriaRepository Repositorio JPA para buscar la categoría asociada.
     */
    public ProductoService(ProductoRepository productoRepository,
                           ProductoMapper productoMapper, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Obtiene todos los productos almacenados en la base de datos.
     * @return Lista de todos los productos convertidos a DTOs.
     */
    public List<ProductoDTO> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un producto por su identificador único.
     * @param id Identificador del producto buscado.
     * @return Producto encontrado convertido a DTO.
     * @throws ProductoNoEncontradoException si el producto no existe.
     */
    public ProductoDTO buscarPorId(Long id) {
        return productoRepository.findById(id)
                .map(productoMapper::toDTO)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));
    }

    /**
     * Busca productos asociados a una categoría específica por su nombre.
     * @param nombreCategoria Nombre de la categoría a buscar.
     * @return Lista de productos que pertenecen a la categoría, convertidos a DTOs.
     */
    public List<ProductoDTO> buscarPorCategoria(String nombreCategoria) {
        return productoRepository.findByCategoriaNombre(nombreCategoria)
                .stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo producto junto con su registro de inventario asociado.
     * @param request DTO que contiene los datos del producto y el stock inicial.
     * @return Producto creado y guardado, convertido a DTO.
     * @throws ValidacionNegocioException si el stock es negativo.
     * @throws CategoriaNoEncontradaException si la categoría especificada no existe.
     */
    public ProductoDTO crearProducto(ProductoRequest request) {
        if (request.getStock() < 0) {
            throw new ValidacionNegocioException("El stock no puede ser negativo");
        }
        Categoria categoria = categoriaRepository.findByNombre(request.getCategoriaNombre())
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se encontro la categoria con el nombre: " + request.getCategoriaNombre()));

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(categoria);

        Inventario inventario = new Inventario();
        inventario.setCantidad(request.getStock());
        inventario.setStockMinimo(10);
        inventario.setFechaActualizacion(LocalDateTime.now());
        inventario.setProducto(producto);

        producto.setInventario(inventario);

        Producto productoGuardado = productoRepository.save(producto);

        return productoMapper.toDTO(productoGuardado);
    }

    /**
     * Actualiza un producto existente identificado por su ID.
     * @param id Identificador del producto a actualizar.
     * @param request DTO con los nuevos datos.
     * @return Producto actualizado, convertido a DTO.
     * @throws ProductoNoEncontradoException si el producto ID no existe.
     * @throws ValidacionNegocioException si el nuevo stock es negativo.
     * @throws CategoriaNoEncontradaException si la nueva categoría no existe.
     */
    public ProductoDTO actualizarProducto(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));

        if (request.getStock() < 0) {
            throw new ValidacionNegocioException("El stock no puede ser negativo");
        }

        Categoria categoria = categoriaRepository.findByNombre(request.getCategoriaNombre())
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se encontro la categoria con el nombre: " + request.getCategoriaNombre()));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(categoria);

        Inventario inventario = producto.getInventario();
        inventario.setCantidad(request.getStock());
        inventario.setFechaActualizacion(LocalDateTime.now());

        Producto productoActualizado = productoRepository.save(producto);

        return productoMapper.toDTO(productoActualizado);
    }

    /**
     * Elimina un producto por su identificador único.
     * @param id Identificador del producto a eliminar.
     * @throws ProductoNoEncontradoException si el producto no existe.
     */
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
}
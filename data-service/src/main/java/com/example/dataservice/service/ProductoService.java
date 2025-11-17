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

    public ProductoService(ProductoRepository productoRepository,
                           ProductoMapper productoMapper, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProductoDTO> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductoDTO buscarPorId(Long id) {
        return productoRepository.findById(id)
                .map(productoMapper::toDTO)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + id));
    }

    public List<ProductoDTO> buscarPorCategoria(String nombreCategoria) {
        return productoRepository.findByCategoriaNombre(nombreCategoria)
                .stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

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

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
}
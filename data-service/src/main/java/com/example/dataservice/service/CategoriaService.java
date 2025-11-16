package com.example.dataservice.service;

import com.example.dataservice.dto.CategoriaDTO;
import com.example.dataservice.dto.CategoriaRequest;
import com.example.dataservice.entity.Categoria;
import com.example.dataservice.exception.CategoriaNoEncontradaException;
import com.example.dataservice.exception.ValidacionNegocioException;
import com.example.dataservice.mapper.CategoriaMapper;
import com.example.dataservice.repository.CategoriaRepository;
import com.example.dataservice.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
        this.productoRepository = productoRepository;
    }

    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toDTO)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id));
    }

    public CategoriaDTO crearCategoria(CategoriaRequest request) {
        if (categoriaRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new ValidacionNegocioException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoriaGuardada);
    }

    /**
     * Actualiza una categoría existente identificada por su ID.
     */
    public CategoriaDTO actualizarCategoria(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id));
        categoriaRepository.findByNombre(request.getNombre()).ifPresent(categoriaExistente -> {
            if (!categoriaExistente.getId().equals(id)) {
                throw new ValidacionNegocioException("El nombre '" + request.getNombre() + "' ya está en uso por otra categoría");
            }
        });

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoriaActualizada);
    }

    /**
     * Elimina una categoría por ID.
     * Verifica que la categoría no tenga productos asociados antes de borrar.
     */
    public void borrarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id));
        if (!productoRepository.findByCategoriaNombre(categoria.getNombre()).isEmpty()) {
            throw new ValidacionNegocioException("No se puede eliminar la categoría ID " + id + ". Existen productos asociados a ella.");
        }
        categoriaRepository.deleteById(id);
    }

}
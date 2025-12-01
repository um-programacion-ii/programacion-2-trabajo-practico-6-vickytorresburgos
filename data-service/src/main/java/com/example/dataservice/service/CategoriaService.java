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

    /**
     * Constructor para la inyección de dependencias.
     * @param categoriaRepository Repositorio JPA para la entidad Categoria.
     * @param categoriaMapper Mapper para convertir entre entidades y DTOs.
     * @param productoRepository Repositorio JPA para verificar la existencia de productos asociados.
     */
    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene todas las categorias almacenadas en la base de datos
     * @return lista de todos los registros de categoria y los convierte a DTO
     */
    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca una categoría por su identificador único.
     * @param id Identificador de la categoría buscada.
     * @return Categoría encontrada convertida a DTO.
     * @throws CategoriaNoEncontradaException si la categoría no existe.
     */
    public CategoriaDTO buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toDTO)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada con ID: " + id));
    }

    /**
     * Crea una categoría nueva.
     * Realiza una validación de negocio para asegurar que el nombre no exista previamente.
     * @param request DTO que contiene los datos para la nueva categoría (nombre y descripción).
     * @return Categoría creada y guardada, convertida a DTO.
     * @throws ValidacionNegocioException si ya existe una categoría con el mismo nombre.
     */
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
     * Realiza validaciones de existencia (por ID) y unicidad (por nombre) antes de actualizar.
     * @param id Identificador de la categoría a actualizar.
     * @param request DTO con los nuevos datos (nombre y descripción).
     * @return Categoría actualizada, convertida a DTO.
     * @throws CategoriaNoEncontradaException si la categoría ID no existe.
     * @throws ValidacionNegocioException si el nuevo nombre ya está en uso por otra categoría.
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
     * Elimina una categoría por su identificador único.
     * Implementa una restricción de integridad de negocio.
     * @param id Identificador de la categoría a eliminar.
     * @throws CategoriaNoEncontradaException si la categoría ID no existe.
     * @throws ValidacionNegocioException si existen productos asociados a la categoría (restricción de FK).
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
package com.example.dataservice.controllers;

import com.example.dataservice.dto.ProductoRequest;
import com.example.dataservice.entity.Categoria;
import com.example.dataservice.entity.Inventario;
import com.example.dataservice.entity.Producto;
import com.example.dataservice.repository.CategoriaRepository;
import com.example.dataservice.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3307/microservices_db?allowPublicKeyRetrieval=true&useSSL=false",
        "spring.datasource.username=microservices_user",
        "spring.datasource.password=microservices_pass",
        "spring.jpa.hibernate.ddl-auto=create-drop", // CRÍTICO: Esto recrea las tablas limpias para cada ejecución de test
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect"
})
class DataControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /data/productos: Persistencia exitosa en MySQL Docker (Local)")
    void crearProducto_Exito() {
        // Arrange
        Categoria cat = new Categoria();
        cat.setNombre("Tech");
        cat.setDescripcion("Tecnología");
        categoriaRepository.save(cat);

        ProductoRequest request = new ProductoRequest();
        request.setNombre("Docker Test");
        request.setDescripcion("Desc");
        request.setPrecio(BigDecimal.valueOf(100));
        request.setCategoriaNombre("Tech");
        request.setStock(10);

        // Act
        ResponseEntity<Producto> response = restTemplate.postForEntity("/data/productos", request, Producto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /data/productos/{id}: Retorna 404 si no existe")
    void obtenerProducto_NoExiste() {
        ResponseEntity<String> response = restTemplate.getForEntity("/data/productos/99999", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    @DisplayName("POST /data/categorias: Validación de Unicidad (Unique Constraint)")
    void crearCategoria_NombreDuplicado_Falla() {
        // Arrange
        Categoria cat1 = new Categoria(1L, "Unica", "Desc", null);
        restTemplate.postForEntity("/data/categorias", cat1, Categoria.class);

        // Act
        Categoria cat2 = new Categoria(null, "Unica", "Otra Desc", null);
        ResponseEntity<String> response = restTemplate.postForEntity("/data/categorias", cat2, String.class);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    @DisplayName("PUT /data/productos/{id}: Actualización de Stock y Precio")
    void actualizarProducto_Exito() {
        // Arrange
        Categoria cat = new Categoria(null, "UpdateTest", "Desc", null);
        categoriaRepository.save(cat);

        Producto original = new Producto(null, "Original", "Desc", BigDecimal.valueOf(100), cat, null);
        Inventario inv = new Inventario(null, original, 10, 5, null);
        original.setInventario(inv);
        productoRepository.save(original); // Guardamos directamente en repo para preparar el escenario

        Long id = original.getId();

        // Act
        ProductoRequest requestUpdate = new ProductoRequest();
        requestUpdate.setNombre("Modificado");
        requestUpdate.setPrecio(BigDecimal.valueOf(200));
        requestUpdate.setCategoriaNombre("UpdateTest");
        requestUpdate.setStock(50); // Cambio de stock

        restTemplate.put("/data/productos/" + id, requestUpdate);

        // Assert
        Producto actualizado = productoRepository.findById(id).orElseThrow();
        assertEquals("Modificado", actualizado.getNombre());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(actualizado.getPrecio()));
        assertEquals(50, actualizado.getInventario().getCantidad());
    }

    @Test
    @DisplayName("DELETE /data/productos/{id}: Eliminación física")
    void eliminarProducto_Exito() {
        // Arrange
        Categoria cat = new Categoria(null, "DeleteTest", "Desc", null);
        categoriaRepository.save(cat);
        Producto p = productoRepository.save(new Producto(null, "Borrar", "Desc", BigDecimal.TEN, cat, null));

        // Act
        restTemplate.delete("/data/productos/" + p.getId());

        // Assert
        assertFalse(productoRepository.existsById(p.getId()));
    }

    @Test
    @DisplayName("GET /data/productos/categoria/{nombre}: Filtro correcto")
    void buscarPorCategoria_RetornaSoloCorrespondientes() {
        // Arrange
        Categoria c1 = categoriaRepository.save(new Categoria(null, "Cat A", "Desc", null));
        Categoria c2 = categoriaRepository.save(new Categoria(null, "Cat B", "Desc", null));

        productoRepository.save(new Producto(null, "Prod A", "Desc", BigDecimal.TEN, c1, null));
        productoRepository.save(new Producto(null, "Prod B", "Desc", BigDecimal.TEN, c2, null));

        // Act
        ResponseEntity<Producto[]> response = restTemplate.getForEntity("/data/productos/categoria/Cat A", Producto[].class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
        assertEquals("Prod A", response.getBody()[0].getNombre());
    }
}
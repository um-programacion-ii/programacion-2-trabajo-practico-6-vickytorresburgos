package com.example.businessservice.controller;

import com.example.businessservice.exceptions.CategoriaNoEncontradaException;
import com.example.businessservice.exceptions.InventarioNoEncontradoException;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import com.example.businessservice.exceptions.ProductoNoEncontradoException;
import com.example.businessservice.exceptions.ValidacionNegocioException;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el microservicio de negocio.
 * Utiliza {@link ControllerAdvice} para interceptar excepciones lanzadas por cualquier controlador
 * y transformarlas en respuestas JSON estandarizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Construye el cuerpo de la respuesta de error estandarizada.
     *
     * @param request La petición HTTP que originó el error.
     * @param status  El estado HTTP a devolver.
     * @param error   Un título corto del error.
     * @param message El mensaje detallado de la excepción.
     * @return Un mapa con los detalles del error.
     */
    private Map<String, Object> buildBody(HttpServletRequest request, HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return body;
    }

    /**
     * Maneja excepciones de reglas de negocio.
     *
     * @param ex      La excepción capturada.
     * @param request La petición actual.
     * @return ResponseEntity con estado 400 (Bad Request).
     */
    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleValidacionNegocio(ValidacionNegocioException ex, HttpServletRequest request) {
        Map<String, Object> body = buildBody(request, HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Maneja excepciones de recursos no encontrados.
     *
     * @param ex      La excepción capturada.
     * @param request La petición actual.
     * @return ResponseEntity con estado 404 (Not Found).
     */
    @ExceptionHandler({ProductoNoEncontradoException.class, CategoriaNoEncontradaException.class, InventarioNoEncontradoException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = buildBody(request, HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Maneja errores personalizados de comunicación entre microservicios.
     * Se lanza cuando el cliente Feign falla o el servicio de datos no responde correctamente.
     *
     * @param ex      La excepción capturada.
     * @param request La petición actual.
     * @return ResponseEntity con estado 502 (Bad Gateway).
     */
    @ExceptionHandler(MicroserviceCommunicationException.class)
    public ResponseEntity<Map<String, Object>> handleMicroserviceCommunication(MicroserviceCommunicationException ex, HttpServletRequest request) {
        Map<String, Object> body = buildBody(request, HttpStatus.BAD_GATEWAY, "Bad Gateway", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    /**
     * Maneja excepciones genéricas lanzadas por el cliente Feign.
     * Actúa como un fallback para errores de Feign no capturados específicamente.
     *
     * @param ex      La excepción de Feign.
     * @param request La petición actual.
     * @return ResponseEntity con estado 502 (Bad Gateway).
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeign(FeignException ex, HttpServletRequest request) {
        String message = "Error de comunicación con el servicio de datos";
        if (ex.getMessage() != null) {
            message += ": " + ex.getMessage();
        }
        Map<String, Object> body = buildBody(request, HttpStatus.BAD_GATEWAY, "Bad Gateway", message);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    /**
     * Maneja errores de validación de argumentos.
     * Se dispara cuando falla la validación de objetos anotados con {@code @Valid} o {@code @Validated}.
     * Incluye un mapa detallado de 'campo' -> 'error'.
     *
     * @param ex      La excepción con los resultados de la validación.
     * @param request La petición actual.
     * @return ResponseEntity con estado 400 (Bad Request) y lista de errores de campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + "; " + b));

        Map<String, Object> body = buildBody(request, HttpStatus.BAD_REQUEST, "Validation Failed", "Validation error on request");
        body.put("validationErrors", validationErrors);

        return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Manejador por defecto para cualquier excepción no controlada.
     *
     * @param ex      La excepción inesperada.
     * @param request La petición actual.
     * @return ResponseEntity con estado 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = buildBody(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
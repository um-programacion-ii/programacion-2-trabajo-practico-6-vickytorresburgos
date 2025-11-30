package com.example.businessservice.dto;

import jakarta.validation.Valid;
import lombok.*;
import jakarta.validation.constraints.*;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InventarioDTO {
    private Long id;

    @NotNull(message = "El producto es obligatorio en el inventario")
    @Valid
    private ProductoDTO producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @Min(value = 0, message = "stockMinimo no puede ser negativo")
    private Integer stockMinimo;
    private LocalDateTime fechaActualizacion;
}
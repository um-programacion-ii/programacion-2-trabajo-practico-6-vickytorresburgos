package com.example.businessservice.exceptions;

public class InventarioNoEncontradoException extends RuntimeException {
    public InventarioNoEncontradoException(String message) {
        super(message);
    }
}

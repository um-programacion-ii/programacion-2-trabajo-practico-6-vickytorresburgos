package com.example.businessservice.exceptions;

public class MicroserviceCommunicationException extends RuntimeException {
    public MicroserviceCommunicationException(String message) {
        super(message);
    }
}

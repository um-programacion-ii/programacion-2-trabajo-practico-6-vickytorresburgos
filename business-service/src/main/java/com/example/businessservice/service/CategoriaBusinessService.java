package com.example.businessservice.service;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoriaBusinessService {
    private final DataServiceClient dataServiceClient;

    public CategoriaBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        try {
            return dataServiceClient.obtenerTodasLasCategorias();
        } catch (FeignException e) {
            log.error("Error al obtener categorias del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicacion con el servicio de datos");
        }
    }
}

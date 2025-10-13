package com.example.businessservice.service;

import com.example.businessservice.client.DataServiceClient;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.exceptions.MicroserviceCommunicationException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventarioBusinessService {
    private final DataServiceClient dataServiceClient;

    public InventarioBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<InventarioDTO> obtenerProductosConStockBajo() {
        try {
            List<InventarioDTO> inventario = dataServiceClient.obtenerProductosConStockBajo();
            return inventario;
        } catch (FeignException e) {
            log.error("Error al obtener los productos con stock bajo", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos");
        }
    }
}

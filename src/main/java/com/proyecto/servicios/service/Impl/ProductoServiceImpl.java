package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.ProductoClient;
import com.proyecto.servicios.model.ProductoResponse;
import com.proyecto.servicios.service.ProductoService;
import lombok.extern.slf4j.Slf4j; // <--- Importante
import org.springframework.stereotype.Service;

@Slf4j 
@Service
public class ProductoServiceImpl implements ProductoService {


    private final ProductoClient productoClient;

    public ProductoServiceImpl(ProductoClient productoClient) {
        this.productoClient = productoClient;
    }

    @Override
    public ProductoResponse consultarProductos() {
        log.info("Inicio del proceso de negocio: consultarProductos()");
        try {
            ProductoResponse response = productoClient.obtenerListaProductos();
            log.info("Fin del proceso de negocio: consultarProductos()");
            return response;
        } catch (Exception e) {
            log.error("Excepción capturada en la capa de servicio al consultar productos: {}", e.getMessage());
            throw e;
        }
    }
}
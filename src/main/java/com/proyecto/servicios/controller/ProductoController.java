package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ProductoResponse;
import com.proyecto.servicios.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<ProductoResponse> getProductos() {
        ProductoResponse response = productoService.consultarProductos();
        return ResponseEntity.ok(response);
    }
}

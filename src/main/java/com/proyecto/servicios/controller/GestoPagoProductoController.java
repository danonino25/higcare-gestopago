package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.GestoPagoProductoDto;
import com.proyecto.servicios.service.GestoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gestopago")
public class GestoPagoProductoController {

    private static final Logger log = LoggerFactory.getLogger(GestoPagoProductoController.class);
    private final GestoPagoService gestoPagoService;

    public GestoPagoProductoController(GestoPagoService gestoPagoService) {
        this.gestoPagoService = gestoPagoService;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<GestoPagoProductoDto>> consultarProductos() {
        log.info("Peticion REST recibida: GET /api/gestopago/productos");
        List<GestoPagoProductoDto> productos = gestoPagoService.obtenerCatalogoProductos();
        log.info("Respuesta enviada con {} productos", productos.size());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/productos/externo")
    public ResponseEntity<List<GestoPagoProductoDto>> consumirProductosDirectos() {
        log.info("Peticion REST recibida: GET /api/gestopago/productos/externo");
        List<GestoPagoProductoDto> productos = gestoPagoService.consumirProductosExternos();
        log.info("Respuesta enviada directamente del servicio externo con {} productos", productos.size());
        return ResponseEntity.ok(productos);
    }
}
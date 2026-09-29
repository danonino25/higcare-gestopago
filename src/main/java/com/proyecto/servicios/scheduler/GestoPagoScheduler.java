package com.proyecto.servicios.scheduler;

import com.proyecto.servicios.service.GestoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GestoPagoScheduler {

    private static final Logger log = LoggerFactory.getLogger(GestoPagoScheduler.class);
    private final GestoPagoService gestoPagoService;

    public GestoPagoScheduler(GestoPagoService gestoPagoService) {
        this.gestoPagoService = gestoPagoService;
    }

    // Regla de negocio: frecuencia configurable vía application.properties
    // (gestopago.productos.cron)
    @Scheduled(cron = "${gestopago.productos.cron}")
    public void ejecutarSincronizacionDiaria() {
        log.info("Ejecutando tarea programada para la actualizacion diaria del catalogo GestoPago");
        try {
            gestoPagoService.sincronizarCatalogoConApi();
        } catch (Exception e) {
            log.error("Error durante la ejecucion de la tarea programada de GestoPago", e);
        }
    }
}
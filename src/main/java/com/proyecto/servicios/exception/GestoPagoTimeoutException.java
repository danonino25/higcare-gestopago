package com.proyecto.servicios.exception;

public class GestoPagoTimeoutException extends GestoPagoIntegrationException {

    public GestoPagoTimeoutException(String mensaje, String path) {
        super("ERR_GESTOPAGO_TIMEOUT", mensaje, path, 504, null);
    }

    public GestoPagoTimeoutException(String mensaje, String path, Throwable cause) {
        super("ERR_GESTOPAGO_TIMEOUT", mensaje, path, 504, cause);
    }
}

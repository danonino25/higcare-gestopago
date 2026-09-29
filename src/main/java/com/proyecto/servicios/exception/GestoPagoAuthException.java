package com.proyecto.servicios.exception;

public class GestoPagoAuthException extends GestoPagoIntegrationException {

    public GestoPagoAuthException(String mensaje, String path) {
        super("ERR_GESTOPAGO_AUTH", mensaje, path, 401, null);
    }

    public GestoPagoAuthException(String mensaje, String path, Throwable cause) {
        super("ERR_GESTOPAGO_AUTH", mensaje, path, 401, cause);
    }
}

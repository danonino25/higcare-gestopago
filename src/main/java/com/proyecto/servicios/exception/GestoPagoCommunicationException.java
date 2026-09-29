package com.proyecto.servicios.exception;

public class GestoPagoCommunicationException extends GestoPagoIntegrationException {

    public GestoPagoCommunicationException(String mensaje, String path) {
        super("ERR_GESTOPAGO_COMMUNICATION", mensaje, path, 503, null);
    }

    public GestoPagoCommunicationException(String mensaje, String path, Throwable cause) {
        super("ERR_GESTOPAGO_COMMUNICATION", mensaje, path, 503, cause);
    }
}

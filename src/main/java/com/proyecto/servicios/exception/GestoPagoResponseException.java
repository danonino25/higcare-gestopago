package com.proyecto.servicios.exception;

public class GestoPagoResponseException extends GestoPagoIntegrationException {

    public GestoPagoResponseException(String mensaje, String path, int httpStatus) {
        super("ERR_GESTOPAGO_RESPONSE", mensaje, path, httpStatus, null);
    }

    public GestoPagoResponseException(String mensaje, String path, int httpStatus, Throwable cause) {
        super("ERR_GESTOPAGO_RESPONSE", mensaje, path, httpStatus, cause);
    }
}

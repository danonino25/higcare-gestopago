package com.proyecto.servicios.exception;

public class GestoPagoIntegrationException extends RuntimeException {
    private final String codigoError;
    private final String path;
    private final Integer httpStatus;

    public GestoPagoIntegrationException(String codigoError, String mensaje, String path) {
        super(mensaje);
        this.codigoError = codigoError;
        this.path = path;
        this.httpStatus = 500;
    }

    public GestoPagoIntegrationException(String codigoError, String mensaje, String path, Throwable cause) {
        super(mensaje, cause);
        this.codigoError = codigoError;
        this.path = path;
        this.httpStatus = 500;
    }

    public GestoPagoIntegrationException(String codigoError, String mensaje, String path, Integer httpStatus, Throwable cause) {
        super(mensaje, cause);
        this.codigoError = codigoError;
        this.path = path;
        this.httpStatus = httpStatus;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public String getPath() {
        return path;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}

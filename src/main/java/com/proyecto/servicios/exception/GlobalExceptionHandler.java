package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GestoPagoAuthException.class)
    public ResponseEntity<GenericResponse> handleAuthException(GestoPagoAuthException ex) {
        log.error("Capturada excepcion de autenticacion GestoPago: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(401);
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(GestoPagoTimeoutException.class)
    public ResponseEntity<GenericResponse> handleTimeoutException(GestoPagoTimeoutException ex) {
        log.error("Capturada excepcion de timeout GestoPago: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(504);
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(GestoPagoCommunicationException.class)
    public ResponseEntity<GenericResponse> handleCommunicationException(GestoPagoCommunicationException ex) {
        log.error("Capturada excepcion de comunicacion GestoPago: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(503);
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(GestoPagoResponseException.class)
    public ResponseEntity<GenericResponse> handleResponseException(GestoPagoResponseException ex) {
        log.error("Capturada respuesta no exitosa de GestoPago: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.getHttpStatus() != null ? ex.getHttpStatus() : 502);
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(GestoPagoIntegrationException.class)
    public ResponseEntity<GenericResponse> handleIntegrationException(GestoPagoIntegrationException ex) {
        log.error("Capturada excepcion general de integracion GestoPago: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.getHttpStatus() != null ? ex.getHttpStatus() : 500);
        response.setMensaje(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado en la aplicacion: {}", ex.getMessage(), ex);
        GenericResponse response = new GenericResponse();
        response.setCodigo(500);
        response.setMensaje("Ha ocurrido un error inesperado al procesar la solicitud");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

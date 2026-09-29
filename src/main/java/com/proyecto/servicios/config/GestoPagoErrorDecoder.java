package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoResponseException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GestoPagoErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(GestoPagoErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String path = response.request() != null ? response.request().url() : "/sistema/service/getProductList.do";
        String sanitizedPath = sanitizeUrl(path);

        log.error("Error al consumir servicio externo GestoPago. Metodo: {}, Path: {}, HTTP Status: {}",
                methodKey, sanitizedPath, status);

        if (status == 401 || status == 403) {
            return new GestoPagoAuthException(
                    "Error de autenticación con el servicio externo GestoPago (HTTP " + status + ")", sanitizedPath);
        }

        if (status == 408 || status == 504) {
            return new GestoPagoTimeoutException(
                    "Timeout agotado en el servicio externo GestoPago (HTTP " + status + ")", sanitizedPath);
        }

        if (status >= 400 && status < 500) {
            return new GestoPagoResponseException(
                    "Respuesta no exitosa del cliente externo GestoPago (HTTP " + status + ")", sanitizedPath, status);
        }

        if (status >= 500) {
            return new GestoPagoResponseException(
                    "Error interno en el servidor remoto de GestoPago (HTTP " + status + ")", sanitizedPath, status);
        }

        return new GestoPagoIntegrationException("ERR_GESTOPAGO_API",
                "Error inesperado al consumir GestoPago (HTTP " + status + ")", sanitizedPath, status, null);
    }

    private String sanitizeUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("(?i)(password|token|secret|key)=[^&]*", "$1=***");
    }
}

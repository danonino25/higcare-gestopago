package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoResponseException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class GestoPagoErrorDecoderTest {

    private GestoPagoErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new GestoPagoErrorDecoder();
    }

    private Response buildResponse(int status, String url) {
        Request request = Request.create(Request.HttpMethod.GET, url, Collections.emptyMap(), null, new RequestTemplate());
        return Response.builder()
                .status(status)
                .reason("Reason")
                .request(request)
                .headers(Collections.emptyMap())
                .body("{}", StandardCharsets.UTF_8)
                .build();
    }

    @Test
    @DisplayName("Decodificar HTTP 401 como GestoPagoAuthException")
    void decode_Http401_DevuelveAuthException() {
        Response response = buildResponse(401, "https://api.gestopago.com/sistema/service/getProductList.do");

        Exception result = errorDecoder.decode("getProductList", response);

        assertInstanceOf(GestoPagoAuthException.class, result);
        GestoPagoAuthException authEx = (GestoPagoAuthException) result;
        assertEquals("ERR_GESTOPAGO_AUTH", authEx.getCodigoError());
        assertEquals(401, authEx.getHttpStatus());
    }

    @Test
    @DisplayName("Decodificar HTTP 403 como GestoPagoAuthException")
    void decode_Http403_DevuelveAuthException() {
        Response response = buildResponse(403, "https://api.gestopago.com/sistema/service/getProductList.do");

        Exception result = errorDecoder.decode("getProductList", response);

        assertInstanceOf(GestoPagoAuthException.class, result);
        assertEquals(401, ((GestoPagoAuthException) result).getHttpStatus());
    }

    @Test
    @DisplayName("Decodificar HTTP 504 Gateway Timeout como GestoPagoTimeoutException")
    void decode_Http504_DevuelveTimeoutException() {
        Response response = buildResponse(504, "https://api.gestopago.com/sistema/service/getProductList.do");

        Exception result = errorDecoder.decode("getProductList", response);

        assertInstanceOf(GestoPagoTimeoutException.class, result);
        GestoPagoTimeoutException timeoutEx = (GestoPagoTimeoutException) result;
        assertEquals("ERR_GESTOPAGO_TIMEOUT", timeoutEx.getCodigoError());
        assertEquals(504, timeoutEx.getHttpStatus());
    }

    @Test
    @DisplayName("Decodificar HTTP 404 como GestoPagoResponseException")
    void decode_Http404_DevuelveResponseException() {
        Response response = buildResponse(404, "https://api.gestopago.com/sistema/service/getProductList.do");

        Exception result = errorDecoder.decode("getProductList", response);

        assertInstanceOf(GestoPagoResponseException.class, result);
        GestoPagoResponseException respEx = (GestoPagoResponseException) result;
        assertEquals("ERR_GESTOPAGO_RESPONSE", respEx.getCodigoError());
        assertEquals(404, respEx.getHttpStatus());
    }

    @Test
    @DisplayName("Decodificar HTTP 500 como GestoPagoResponseException")
    void decode_Http500_DevuelveResponseException() {
        Response response = buildResponse(500, "https://api.gestopago.com/sistema/service/getProductList.do");

        Exception result = errorDecoder.decode("getProductList", response);

        assertInstanceOf(GestoPagoResponseException.class, result);
        GestoPagoResponseException respEx = (GestoPagoResponseException) result;
        assertEquals("ERR_GESTOPAGO_RESPONSE", respEx.getCodigoError());
        assertEquals(500, respEx.getHttpStatus());
    }

    @Test
    @DisplayName("Sanitizar parámetros sensibles en la URL")
    void decode_SanitizarUrlConToken() {
        Response response = buildResponse(401, "https://api.gestopago.com/test?token=SECRET123&password=SecretPassword99");

        Exception result = errorDecoder.decode("getProductList", response);

        GestoPagoIntegrationException ex = (GestoPagoIntegrationException) result;
        assertFalse(ex.getPath().contains("SECRET123"));
        assertFalse(ex.getPath().contains("SecretPassword99"));
    }
}

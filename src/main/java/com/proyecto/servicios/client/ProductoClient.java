package com.proyecto.servicios.client;

import com.proyecto.servicios.model.ProductoResponse;
import lombok.extern.slf4j.Slf4j; // <--- Importante
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j 
@Component
public class ProductoClient {

    private final RestTemplate restTemplate;
    private final String serviceUrl;
    private final String bearerToken;

    public ProductoClient(RestTemplate restTemplate,
                          @Value("${external.service.producto.url}") String serviceUrl,
                          @Value("${external.service.producto.token}") String bearerToken) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
        this.bearerToken = bearerToken;
    }

    public ProductoResponse obtenerListaProductos() {
        String endpoint = serviceUrl + "/sistema/service/getProductList.do";
        log.info("Iniciando llamada al servicio externo de productos en endpoint: {}", endpoint); // Usa 'log' en minúscula

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductoResponse> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    requestEntity,
                    ProductoResponse.class
            );

            log.info("Llamada al servicio externo completada con estado HTTP: {}", response.getStatusCode());
            return response.getBody();

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.error("Error de autenticación/autorización al consumir el servicio externo. Estado HTTP: {}", e.getStatusCode());
            throw new RuntimeException("Error de autenticación con el servicio externo de productos.", e);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Respuesta no exitosa recibida del servicio externo. Estado HTTP: {}", e.getStatusCode());
            throw new RuntimeException("Error en la respuesta del servicio externo de productos.", e);

        } catch (ResourceAccessException e) {
            log.error("Error de comunicación o timeout al intentar contactar al servicio externo.");
            throw new RuntimeException("Timeout o falla de red al consumir el servicio externo.", e);

        } catch (Exception e) {
            log.error("Error inesperado en la integración con el servicio externo.");
            throw new RuntimeException("Error de integración no controlado.", e);
        }
    }
}
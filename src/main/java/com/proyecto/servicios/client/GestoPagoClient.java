package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gestoPagoClient", url = "${gestopago.service.url}", configuration = GestoPagoClientConfig.class)
public interface GestoPagoClient {

    @GetMapping(value = "${gestopago.service.endpoint}", produces = { MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_XML_VALUE, MediaType.ALL_VALUE })
    String getProductList(@RequestHeader(HttpHeaders.AUTHORIZATION) String token);
}
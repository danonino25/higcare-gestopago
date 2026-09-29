package com.proyecto.servicios.service.impl;

import com.proyecto.servicios.client.GestoPagoClient;
import com.proyecto.servicios.client.GestoPagoXmlParser;
import com.proyecto.servicios.entity.gestopago.GestoPagoProductoEntity;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoDto;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.service.GestoPagoService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class GestoPagoServiceImpl implements GestoPagoService {

    private static final Logger log = LoggerFactory.getLogger(GestoPagoServiceImpl.class);
    private static final String ENDPOINT_PRODUCT_LIST = "/sistema/service/getProductList.do";

    private final GestoPagoClient gestoPagoClient;
    private final GestoPagoXmlParser xmlParser;
    private final GestoPagoProductoRepository productoRepository;
    private final GestoPagoTokenService gestoPagoTokenService;

    @Value("${gestopago.service.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.service.codigo-dispositivo}")
    private String codigoDispositivo;

    public GestoPagoServiceImpl(GestoPagoClient gestoPagoClient,
            GestoPagoXmlParser xmlParser,
            GestoPagoProductoRepository productoRepository,
            GestoPagoTokenService gestoPagoTokenService) {
        this.gestoPagoClient = gestoPagoClient;
        this.xmlParser = xmlParser;
        this.productoRepository = productoRepository;
        this.gestoPagoTokenService = gestoPagoTokenService;
    }

    @Override
    public List<GestoPagoProductoDto> consumirProductosExternos() {
        log.info("Iniciando llamada al servicio externo GestoPago: GET {}", ENDPOINT_PRODUCT_LIST);

        String bearerHeader = obtenerBearerHeader();

        try {
            String xmlResponse = gestoPagoClient.getProductList(bearerHeader);
            List<GestoPagoProductoDto> productos = xmlParser.parseProductListXml(xmlResponse);

            log.info("Finalizada llamada al servicio externo GestoPago con exito. Total productos recibidos: {}",
                    productos.size());
            return productos;

        } catch (GestoPagoAuthException e) {
            log.error("Error de autenticacion al invocar servicio externo GestoPago: {}", e.getMessage());
            throw e;
        } catch (GestoPagoTimeoutException e) {
            log.error("Timeout al invocar servicio externo GestoPago: {}", e.getMessage());
            throw e;
        } catch (GestoPagoCommunicationException e) {
            log.error("Error de comunicacion al invocar servicio externo GestoPago: {}", e.getMessage());
            throw e;
        } catch (GestoPagoResponseException e) {
            log.error("Respuesta no exitosa de servicio externo GestoPago [HTTP {}]: {}", e.getHttpStatus(),
                    e.getMessage());
            throw e;
        } catch (RetryableException e) {
            if (isTimeoutException(e)) {
                log.error("Timeout de conexion/lectura agotado al invocar GestoPago: {}", e.getMessage());
                throw new GestoPagoTimeoutException("Tiempo de espera agotado al conectar con el servicio GestoPago",
                        ENDPOINT_PRODUCT_LIST, e);
            }
            log.error("Fallo de comunicacion de red al conectar con GestoPago: {}", e.getMessage());
            throw new GestoPagoCommunicationException("Fallo de comunicacion con el servicio remoto GestoPago",
                    ENDPOINT_PRODUCT_LIST, e);
        } catch (feign.FeignException e) {
            log.error("Error de invocacion Feign con GestoPago [HTTP {}]: {}", e.status(), e.getMessage());
            if (e.status() == 401 || e.status() == 403) {
                throw new GestoPagoAuthException(
                        "Error de autenticacion con el servicio externo GestoPago (HTTP " + e.status() + ")",
                        ENDPOINT_PRODUCT_LIST, e);
            } else if (e.status() == 408 || e.status() == 504) {
                throw new GestoPagoTimeoutException(
                        "Timeout agotado en el servicio externo GestoPago (HTTP " + e.status() + ")",
                        ENDPOINT_PRODUCT_LIST, e);
            } else if (e.status() >= 400) {
                throw new GestoPagoResponseException("Respuesta no exitosa de GestoPago (HTTP " + e.status() + ")",
                        ENDPOINT_PRODUCT_LIST, e.status(), e);
            } else {
                throw new GestoPagoCommunicationException("Error de comunicacion con el servicio GestoPago",
                        ENDPOINT_PRODUCT_LIST, e);
            }
        } catch (GestoPagoIntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en la integracion con GestoPago: {}", e.getMessage(), e);
            throw new GestoPagoIntegrationException(
                    "ERR_GESTOPAGO_UNEXPECTED",
                    "Error inesperado al consumir el servicio externo de GestoPago",
                    ENDPOINT_PRODUCT_LIST,
                    500,
                    e);
        }
    }

    @Override
    public List<GestoPagoProductoDto> obtenerCatalogoProductos() {
        log.info("Iniciando consulta de catalogo de productos de GestoPago");

        try {
            List<GestoPagoProductoEntity> entidades = productoRepository.findAll();
            if (!entidades.isEmpty()) {
                log.info("Catalogo obtenido desde la base de datos local. Total registros: {}", entidades.size());
                return entidades.stream().map(this::mapToDto).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el catalogo de la base de datos local. Procediendo al servicio externo: {}",
                    e.getMessage());
        }

        log.info("BD local vacia o no disponible. Consultando directamente API externa de GestoPago...");
        List<GestoPagoProductoDto> productosExternos = consumirProductosExternos();
        guardarProductosEnBd(productosExternos);
        return productosExternos;
    }

    @Override
    public void sincronizarCatalogoConApi() {
        log.info("Iniciando sincronizacion programada con la API de GestoPago...");
        List<GestoPagoProductoDto> productosExternos = consumirProductosExternos();
        guardarProductosEnBd(productosExternos);
    }

    private void guardarProductosEnBd(List<GestoPagoProductoDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        try {
            List<GestoPagoProductoEntity> entidades = dtoList.stream()
                    .map(dto -> new GestoPagoProductoEntity(
                            dto.getIdProducto(),
                            dto.getNombre(),
                            dto.getCategoria(),
                            dto.getPrecio(),
                            dto.getUrlImagen()))
                    .collect(Collectors.toList());
            productoRepository.saveAll(entidades);
            log.info("Catalogo sincronizado y persistido exitosamente en BD local. Total registros: {}",
                    entidades.size());
        } catch (Exception e) {
            log.warn("No se pudieron persistir los productos en la BD local: {}", e.getMessage());
        }
    }

    private String obtenerBearerHeader() {
        Optional<GestoPagoToken> tokenActivo = gestoPagoTokenService.obtenerTokenActivo(idDistribuidor,
                codigoDispositivo);

        String token = tokenActivo
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .map(GestoPagoToken::getToken)
                .filter(t -> t != null && !t.isBlank())
                .orElse(null);

        if (token == null) {
            log.info("Token no encontrado en BD. Ejecutando proceso de autenticacion con GestoPago...");
            try {
                gestoPagoTokenService.renovarToken();
                tokenActivo = gestoPagoTokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo);
                token = tokenActivo
                        .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                        .map(GestoPagoToken::getToken)
                        .filter(t -> t != null && !t.isBlank())
                        .orElse(null);
            } catch (Exception e) {
                log.error("Fallo la renovacion automatica de token: {}", e.getMessage());
            }
        }

        if (token == null) {
            log.error("No existe un token activo de GestoPago para idDistribuidor={} codigoDispositivo={}",
                    idDistribuidor, codigoDispositivo);
            throw new GestoPagoAuthException(
                    "El token de GestoPago no esta configurado o no se pudo generar",
                    ENDPOINT_PRODUCT_LIST);
        }

        String trimmed = token.trim();
        return trimmed.startsWith("Bearer ") ? trimmed : "Bearer " + trimmed;
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private GestoPagoProductoDto mapToDto(GestoPagoProductoEntity entity) {
        return new GestoPagoProductoDto(
                entity.getIdProducto(),
                entity.getNombre(),
                entity.getCategoria(),
                entity.getPrecio(),
                entity.getUrlImagen());
    }
}
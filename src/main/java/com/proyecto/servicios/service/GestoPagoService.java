package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.GestoPagoProductoDto;
import java.util.List;

public interface GestoPagoService {

    /**
     * Consume directamente el servicio externo GET /sistema/service/getProductList.do
     * con autenticación Bearer Token y manejo integral de excepciones.
     *
     * @return Lista de productos obtenidos de la API externa
     */
    List<GestoPagoProductoDto> consumirProductosExternos();

    /**
     * Obtiene el catálogo de productos consultando la BD local con fallback a la API externa.
     *
     * @return Lista de productos
     */
    List<GestoPagoProductoDto> obtenerCatalogoProductos();

    /**
     * Sincroniza el catálogo de productos desde la API externa y lo persiste en base de datos.
     */
    void sincronizarCatalogoConApi();
}
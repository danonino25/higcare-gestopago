package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GestoPagoProductoDto implements Serializable {

    @JsonProperty("idProducto")
    private String idProducto;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("precio")
    private BigDecimal precio;

    @JsonProperty("urlImagen")
    private String urlImagen;

    public GestoPagoProductoDto() {
    }

    public GestoPagoProductoDto(String idProducto, String nombre, String categoria, BigDecimal precio,
            String urlImagen) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.urlImagen = urlImagen;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }
}
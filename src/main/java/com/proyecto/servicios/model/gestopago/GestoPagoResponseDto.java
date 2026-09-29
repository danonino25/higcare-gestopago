package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GestoPagoResponseDto implements Serializable {

    @JsonProperty("codigo")
    private Integer codigo;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("productos")
    private List<GestoPagoProductoDto> productos;

    public GestoPagoResponseDto() {
    }

    public GestoPagoResponseDto(Integer codigo, String mensaje, List<GestoPagoProductoDto> productos) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.productos = productos;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<GestoPagoProductoDto> getProductos() {
        return productos;
    }

    public void setProductos(List<GestoPagoProductoDto> productos) {
        this.productos = productos;
    }
}
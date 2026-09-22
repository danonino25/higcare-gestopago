package com.proyecto.servicios.model;

import java.io.Serializable;
import java.util.List;

public class ProductoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean exito;
    private String mensaje;
    private List<ProductoDto> productos;

    public ProductoResponse() {}

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<ProductoDto> getProductos() { return productos; }
    public void setProductos(List<ProductoDto> productos) { this.productos = productos; }

    public static class ProductoDto implements Serializable {
        private String id;
        private String nombre;
        private Double precio;

        public ProductoDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
    }
}
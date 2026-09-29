package com.proyecto.servicios.client;

import com.proyecto.servicios.model.gestopago.GestoPagoProductoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class GestoPagoXmlParser {

    private static final Logger log = LoggerFactory.getLogger(GestoPagoXmlParser.class);

    public List<GestoPagoProductoDto> parseProductListXml(String xml) {
        List<GestoPagoProductoDto> productos = new ArrayList<>();
        if (xml == null || xml.isBlank()) {
            return productos;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            // 1. Obtener todos los nodos de etiqueta <producto> o <item>
            NodeList nodeList = doc.getElementsByTagName("producto");
            if (nodeList.getLength() == 0) {
                nodeList = doc.getElementsByTagName("item");
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                // 2. Extraer atributos directamente del nodo XML
                String idProducto = getAttributeOrChild(element, "idProducto");
                String nombre = getAttributeOrChild(element, "producto");
                if (nombre == null || nombre.isBlank()) {
                    nombre = getAttributeOrChild(element, "nombre");
                }

                String categoria = getAttributeOrChild(element, "servicio");
                if (categoria == null || categoria.isBlank()) {
                    categoria = getAttributeOrChild(element, "categoria");
                }

                String precioStr = getAttributeOrChild(element, "precio");
                String urlImagen = getAttributeOrChild(element, "urlImagen");

                BigDecimal precio = null;
                if (precioStr != null && !precioStr.isBlank()) {
                    try {
                        precio = new BigDecimal(precioStr.trim());
                    } catch (Exception ignored) {
                    }
                }

                GestoPagoProductoDto dto = new GestoPagoProductoDto(idProducto, nombre, categoria, precio, urlImagen);
                productos.add(dto);
            }

        } catch (Exception e) {
            log.error("Error al parsear el XML de GestoPago: {}", e.getMessage(), e);
        }

        log.info("Total de productos extraídos del XML: {}", productos.size());
        return productos;
    }

    private String getAttributeOrChild(Element element, String attributeName) {
        // Primero busca en los atributos de la etiqueta (ej. <producto
        // idProducto="123">)
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }

        // Si no está como atributo, busca en minúsculas
        if (element.hasAttribute(attributeName.toLowerCase())) {
            return element.getAttribute(attributeName.toLowerCase());
        }

        // Si no está en atributos, busca como etiqueta hija (ej.
        // <idProducto>123</idProducto>)
        NodeList children = element.getElementsByTagName(attributeName);
        if (children != null && children.getLength() > 0) {
            return children.item(0).getTextContent();
        }

        return null;
    }
}
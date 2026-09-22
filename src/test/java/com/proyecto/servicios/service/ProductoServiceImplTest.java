package com.proyecto.servicios.service;

import com.proyecto.servicios.client.ProductoClient;
import com.proyecto.servicios.model.ProductoResponse;
import com.proyecto.servicios.service.Impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoClient productoClient;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private ProductoResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new ProductoResponse();
        mockResponse.setExito(true);
        mockResponse.setMensaje("OK");
    }

    @Test
    void consultarProductos_Exitoso() {
        when(productoClient.obtenerListaProductos()).thenReturn(mockResponse);

        ProductoResponse resultado = productoService.consultarProductos();

        assertNotNull(resultado);
        assertTrue(resultado.isExito());
        assertEquals("OK", resultado.getMensaje());
        verify(productoClient, times(1)).obtenerListaProductos();
    }

    @Test
    void consultarProductos_ErrorAutenticacion() {
        when(productoClient.obtenerListaProductos())
                .thenThrow(new RuntimeException("Error de autenticación con el servicio externo de productos."));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.consultarProductos();
        });

        assertTrue(exception.getMessage().contains("autenticación"));
        verify(productoClient, times(1)).obtenerListaProductos();
    }

    @Test
    void consultarProductos_TimeoutOErrorRed() {
        when(productoClient.obtenerListaProductos())
                .thenThrow(new RuntimeException("Timeout o falla de red al consumir el servicio externo."));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.consultarProductos();
        });

        assertTrue(exception.getMessage().contains("Timeout"));
        verify(productoClient, times(1)).obtenerListaProductos();
    }
}
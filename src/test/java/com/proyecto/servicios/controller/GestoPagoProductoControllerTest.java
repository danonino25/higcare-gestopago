package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoCommunicationException;
import com.proyecto.servicios.exception.GestoPagoResponseException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoDto;
import com.proyecto.servicios.service.GestoPagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestoPagoService gestoPagoService;

    @InjectMocks
    private GestoPagoProductoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/gestopago/productos retorna lista de productos con HTTP 200")
    void consultarProductos_Exito_200() throws Exception {
        GestoPagoProductoDto dto = new GestoPagoProductoDto("PROD1", "Agua", "Servicios", new BigDecimal("150.00"), "http://img.png");
        when(gestoPagoService.obtenerCatalogoProductos()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/gestopago/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value("PROD1"))
                .andExpect(jsonPath("$[0].nombre").value("Agua"))
                .andExpect(jsonPath("$[0].precio").value(150.00));
    }

    @Test
    @DisplayName("GET /api/gestopago/productos maneja GestoPagoAuthException retornando HTTP 401")
    void consultarProductos_ErrorAutenticacion_401() throws Exception {
        when(gestoPagoService.obtenerCatalogoProductos())
                .thenThrow(new GestoPagoAuthException("Token no autorizado", "/sistema/service/getProductList.do"));

        mockMvc.perform(get("/api/gestopago/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401))
                .andExpect(jsonPath("$.mensaje").value("Token no autorizado"));
    }

    @Test
    @DisplayName("GET /api/gestopago/productos maneja GestoPagoTimeoutException retornando HTTP 504")
    void consultarProductos_ErrorTimeout_504() throws Exception {
        when(gestoPagoService.obtenerCatalogoProductos())
                .thenThrow(new GestoPagoTimeoutException("Timeout agotado", "/sistema/service/getProductList.do"));

        mockMvc.perform(get("/api/gestopago/productos"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.codigo").value(504))
                .andExpect(jsonPath("$.mensaje").value("Timeout agotado"));
    }

    @Test
    @DisplayName("GET /api/gestopago/productos maneja GestoPagoCommunicationException retornando HTTP 503")
    void consultarProductos_ErrorComunicacion_503() throws Exception {
        when(gestoPagoService.obtenerCatalogoProductos())
                .thenThrow(new GestoPagoCommunicationException("Fallo de red", "/sistema/service/getProductList.do"));

        mockMvc.perform(get("/api/gestopago/productos"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value(503))
                .andExpect(jsonPath("$.mensaje").value("Fallo de red"));
    }

    @Test
    @DisplayName("GET /api/gestopago/productos maneja GestoPagoResponseException retornando HTTP 502")
    void consultarProductos_RespuestaNoExitosa_502() throws Exception {
        when(gestoPagoService.obtenerCatalogoProductos())
                .thenThrow(new GestoPagoResponseException("Error 500 remoto", "/sistema/service/getProductList.do", 500));

        mockMvc.perform(get("/api/gestopago/productos"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value(500))
                .andExpect(jsonPath("$.mensaje").value("Error 500 remoto"));
    }
}

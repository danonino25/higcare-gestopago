package com.proyecto.servicios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoProductoEntity;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.GestoPagoAuthException;
import com.proyecto.servicios.exception.GestoPagoCommunicationException;
import com.proyecto.servicios.exception.GestoPagoResponseException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.model.gestopago.GestoPagoProductoDto;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.service.impl.GestoPagoServiceImpl;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestoPagoServiceImplTest {

        private static final String TEST_TOKEN = "test_bearer_token_xyz123";
        private static final Integer ID_DISTRIBUIDOR = 83;
        private static final String CODIGO_DISPOSITIVO = "GPS83-TPV-17";

        @Mock
        private GestoPagoClient gestoPagoClient;

        @Mock
        private GestoPagoProductoRepository productoRepository;

        @Mock
        private GestoPagoTokenService gestoPagoTokenService;

        @InjectMocks
        private GestoPagoServiceImpl gestoPagoService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(
                                gestoPagoService,
                                "idDistribuidor",
                                ID_DISTRIBUIDOR);

                ReflectionTestUtils.setField(
                                gestoPagoService,
                                "codigoDispositivo",
                                CODIGO_DISPOSITIVO);
        }

        private GestoPagoToken tokenActivo(String token) {
                GestoPagoToken entity = new GestoPagoToken();
                entity.setIdDistribuidor(ID_DISTRIBUIDOR);
                entity.setCodigoDispositivo(CODIGO_DISPOSITIVO);
                entity.setToken(token);
                entity.setActivo(true);
                return entity;
        }

        /**
         * Convierte una lista de productos a JSON.
         *
         * El método getProductList() del cliente devuelve String,
         * por lo que los mocks deben regresar JSON y no List<GestoPagoProductoDto>.
         */
        private String convertirAJson(List<GestoPagoProductoDto> productos)
                        throws JsonProcessingException {
                return objectMapper.writeValueAsString(productos);
        }

        // =========================================================================
        // 1. Escenarios de Éxito
        // =========================================================================

        @Test
        @DisplayName("Consumir API externa exitosamente con Bearer Token obtenido de GestoPagoTokenService")
        void consumirProductosExternos_Exito() throws Exception {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(Optional.of(tokenActivo(TEST_TOKEN)));

                GestoPagoProductoDto dto = new GestoPagoProductoDto(
                                "PROD01",
                                "Recarga Telcel",
                                "Telefonia",
                                new BigDecimal("100.00"),
                                "http://img.com/telcel.png");

                String respuestaJson = convertirAJson(List.of(dto));

                when(gestoPagoClient.getProductList(
                                "Bearer " + TEST_TOKEN)).thenReturn(respuestaJson);

                // Act
                List<GestoPagoProductoDto> resultado = gestoPagoService.consumirProductosExternos();

                // Assert
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals("PROD01", resultado.get(0).getIdProducto());
                assertEquals("Recarga Telcel", resultado.get(0).getNombre());

                verify(
                                gestoPagoClient,
                                times(1)).getProductList("Bearer " + TEST_TOKEN);

                verify(
                                gestoPagoTokenService,
                                times(1)).obtenerTokenActivo(
                                                ID_DISTRIBUIDOR,
                                                CODIGO_DISPOSITIVO);
        }

        @Test
        @DisplayName("Consumir API externa cuando el token guardado ya tiene prefijo Bearer")
        void consumirProductosExternos_TokenYaTienePrefijoBearer() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo("Bearer ya_tiene_prefijo")));

                when(gestoPagoClient.getProductList(
                                "Bearer ya_tiene_prefijo")).thenReturn("[]");

                // Act
                List<GestoPagoProductoDto> resultado = gestoPagoService.consumirProductosExternos();

                // Assert
                assertNotNull(resultado);

                verify(
                                gestoPagoClient,
                                times(1)).getProductList("Bearer ya_tiene_prefijo");
        }

        @Test
        @DisplayName("Obtener catálogo desde BD local cuando existen registros")
        void obtenerCatalogoProductos_ExitoDesdeBD() {

                // Arrange
                GestoPagoProductoEntity entity = new GestoPagoProductoEntity(
                                "PROD_BD",
                                "Pago de Agua",
                                "Servicios",
                                new BigDecimal("150.00"),
                                "http://img.png");

                when(productoRepository.findAll())
                                .thenReturn(List.of(entity));

                // Act
                List<GestoPagoProductoDto> resultado = gestoPagoService.obtenerCatalogoProductos();

                // Assert
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals("PROD_BD", resultado.get(0).getIdProducto());

                verify(
                                gestoPagoClient,
                                never()).getProductList(anyString());

                verify(
                                gestoPagoTokenService,
                                never()).obtenerTokenActivo(anyInt(), anyString());
        }

        @Test
        @DisplayName("Fallback a API externa cuando la BD local está vacía")
        void obtenerCatalogoProductos_FallbackApiExito_CuandoBDEstaVacia()
                        throws Exception {

                // Arrange
                when(productoRepository.findAll())
                                .thenReturn(Collections.emptyList());

                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                GestoPagoProductoDto dto = new GestoPagoProductoDto(
                                "PROD_EXT",
                                "Pago de Luz",
                                "Servicios",
                                new BigDecimal("320.00"),
                                "http://img2.png");

                String respuestaJson = convertirAJson(List.of(dto));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenReturn(respuestaJson);

                // Act
                List<GestoPagoProductoDto> resultado = gestoPagoService.obtenerCatalogoProductos();

                // Assert
                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals("PROD_EXT", resultado.get(0).getIdProducto());

                verify(
                                productoRepository,
                                times(1)).saveAll(any());
        }

        @Test
        @DisplayName("Sincronizar catálogo con API externa y persistir en BD")
        void sincronizarCatalogoConApi_Exito() throws Exception {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                GestoPagoProductoDto dto = new GestoPagoProductoDto(
                                "SYNC1",
                                "Gas Natural",
                                "Servicios",
                                new BigDecimal("500.00"),
                                "http://img.png");

                String respuestaJson = convertirAJson(List.of(dto));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenReturn(respuestaJson);

                // Act
                gestoPagoService.sincronizarCatalogoConApi();

                // Assert
                verify(
                                gestoPagoClient,
                                times(1)).getProductList(anyString());

                verify(
                                productoRepository,
                                times(1)).saveAll(any());
        }

        // =========================================================================
        // 2. Escenarios de Error: Autenticación
        // =========================================================================

        @Test
        @DisplayName("Lanzar GestoPagoAuthException cuando no existe token activo (Optional vacío)")
        void consumirProductosExternos_ErrorAutenticacion_SinTokenActivo() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(Optional.empty());

                // Act & Assert
                GestoPagoAuthException exception = assertThrows(
                                GestoPagoAuthException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_AUTH",
                                exception.getCodigoError());

                assertEquals(
                                401,
                                exception.getHttpStatus());

                verify(
                                gestoPagoClient,
                                never()).getProductList(anyString());
        }

        @Test
        @DisplayName("Lanzar GestoPagoAuthException cuando el token almacenado está inactivo")
        void consumirProductosExternos_ErrorAutenticacion_TokenInactivo() {

                // Arrange
                GestoPagoToken inactivo = tokenActivo(TEST_TOKEN);
                inactivo.setActivo(false);

                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(Optional.of(inactivo));

                // Act & Assert
                assertThrows(
                                GestoPagoAuthException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                verify(
                                gestoPagoClient,
                                never()).getProductList(anyString());
        }

        @Test
        @DisplayName("Lanzar GestoPagoAuthException cuando el token almacenado está vacío")
        void consumirProductosExternos_ErrorAutenticacion_TokenVacio() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo("   ")));

                // Act & Assert
                GestoPagoAuthException exception = assertThrows(
                                GestoPagoAuthException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_AUTH",
                                exception.getCodigoError());

                verify(
                                gestoPagoClient,
                                never()).getProductList(anyString());
        }

        @Test
        @DisplayName("Lanzar GestoPagoAuthException cuando la API externa responde 401 Unauthorized")
        void consumirProductosExternos_ErrorAutenticacion_Respuesta401Api() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(
                                                new GestoPagoAuthException(
                                                                "Token no autorizado o expirado (HTTP 401)",
                                                                "/sistema/service/getProductList.do"));

                // Act & Assert
                GestoPagoAuthException ex = assertThrows(
                                GestoPagoAuthException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_AUTH",
                                ex.getCodigoError());

                assertEquals(
                                401,
                                ex.getHttpStatus());
        }

        // =========================================================================
        // 3. Escenarios de Error: Timeouts
        // =========================================================================

        @Test
        @DisplayName("Lanzar GestoPagoTimeoutException cuando Feign lanza RetryableException por SocketTimeout")
        void consumirProductosExternos_ErrorTimeout_SocketTimeout() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                SocketTimeoutException socketTimeout = new SocketTimeoutException("Read timed out");

                Request request = Request.create(
                                Request.HttpMethod.GET,
                                "/sistema/service/getProductList.do",
                                Collections.emptyMap(),
                                null,
                                new RequestTemplate());

                RetryableException retryableException = new RetryableException(
                                504,
                                "Read timed out",
                                Request.HttpMethod.GET,
                                socketTimeout,
                                (Long) null,
                                request);

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(retryableException);

                // Act & Assert
                GestoPagoTimeoutException ex = assertThrows(
                                GestoPagoTimeoutException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_TIMEOUT",
                                ex.getCodigoError());

                assertEquals(
                                504,
                                ex.getHttpStatus());
        }

        @Test
        @DisplayName("Lanzar GestoPagoTimeoutException cuando el ErrorDecoder devuelve GestoPagoTimeoutException")
        void consumirProductosExternos_ErrorTimeout_DirectException() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(
                                                new GestoPagoTimeoutException(
                                                                "Gateway Timeout 504",
                                                                "/sistema/service/getProductList.do"));

                // Act & Assert
                GestoPagoTimeoutException ex = assertThrows(
                                GestoPagoTimeoutException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_TIMEOUT",
                                ex.getCodigoError());
        }

        // =========================================================================
        // 4. Escenarios de Error: Comunicación
        // =========================================================================

        @Test
        @DisplayName("Lanzar GestoPagoCommunicationException ante fallo de conexión de red")
        void consumirProductosExternos_ErrorComunicacion_ConnectException() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                ConnectException connectException = new ConnectException("Connection refused: connect");

                Request request = Request.create(
                                Request.HttpMethod.GET,
                                "/sistema/service/getProductList.do",
                                Collections.emptyMap(),
                                null,
                                new RequestTemplate());

                RetryableException retryableException = new RetryableException(
                                503,
                                "Connection refused",
                                Request.HttpMethod.GET,
                                connectException,
                                (Long) null,
                                request);

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(retryableException);

                // Act & Assert
                GestoPagoCommunicationException ex = assertThrows(
                                GestoPagoCommunicationException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_COMMUNICATION",
                                ex.getCodigoError());

                assertEquals(
                                503,
                                ex.getHttpStatus());
        }

        @Test
        @DisplayName("Lanzar GestoPagoCommunicationException ante fallo de red o host inaccesible")
        void consumirProductosExternos_ErrorComunicacion_DirectException() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(
                                                new GestoPagoCommunicationException(
                                                                "Host inalcanzable",
                                                                "/sistema/service/getProductList.do"));

                // Act & Assert
                GestoPagoCommunicationException ex = assertThrows(
                                GestoPagoCommunicationException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_COMMUNICATION",
                                ex.getCodigoError());

                assertEquals(
                                503,
                                ex.getHttpStatus());
        }

        // =========================================================================
        // 5. Escenarios de Error: Respuestas no exitosas (HTTP 4xx / 5xx)
        // =========================================================================

        @Test
        @DisplayName("Lanzar GestoPagoResponseException cuando la API responde con HTTP 500")
        void consumirProductosExternos_RespuestaNoExitosa_Http500() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(
                                                new GestoPagoResponseException(
                                                                "Error interno en GestoPago",
                                                                "/sistema/service/getProductList.do",
                                                                500));

                // Act & Assert
                GestoPagoResponseException ex = assertThrows(
                                GestoPagoResponseException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_RESPONSE",
                                ex.getCodigoError());

                assertEquals(
                                500,
                                ex.getHttpStatus());
        }

        @Test
        @DisplayName("Lanzar GestoPagoResponseException cuando la API responde con HTTP 404")
        void consumirProductosExternos_RespuestaNoExitosa_Http404() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(tokenActivo(TEST_TOKEN)));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(
                                                new GestoPagoResponseException(
                                                                "Recurso no encontrado",
                                                                "/sistema/service/getProductList.do",
                                                                404));

                // Act & Assert
                GestoPagoResponseException ex = assertThrows(
                                GestoPagoResponseException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertEquals(
                                "ERR_GESTOPAGO_RESPONSE",
                                ex.getCodigoError());

                assertEquals(
                                404,
                                ex.getHttpStatus());
        }

        // =========================================================================
        // 6. Seguridad: No exponer tokens sensibles
        // =========================================================================

        @Test
        @DisplayName("Garantizar que no se expone el Bearer token en el mensaje de error de excepción")
        void consumirProductosExternos_Seguridad_NoExponerTokenSensible() {

                // Arrange
                when(gestoPagoTokenService.obtenerTokenActivo(
                                ID_DISTRIBUIDOR,
                                CODIGO_DISPOSITIVO)).thenReturn(
                                                Optional.of(
                                                                tokenActivo("SUPER_SECRET_TOKEN_DO_NOT_LEAK")));

                when(gestoPagoClient.getProductList(anyString()))
                                .thenThrow(
                                                new GestoPagoAuthException(
                                                                "Credenciales no válidas",
                                                                "/sistema/service/getProductList.do"));

                // Act & Assert
                GestoPagoAuthException ex = assertThrows(
                                GestoPagoAuthException.class,
                                () -> gestoPagoService.consumirProductosExternos());

                assertFalse(
                                ex.getMessage().contains(
                                                "SUPER_SECRET_TOKEN_DO_NOT_LEAK"),
                                "El mensaje de error no debe exponer el token sensible");
        }
}
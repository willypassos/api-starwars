package br.com.swapi.handler;

import br.com.swapi.client.FleetHandler;
import br.com.swapi.enums.HttpStatus;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.FleetRecordRequestBody;
import br.com.swapi.service.IFleetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.*;

public class FleetHandlerTest {

    @Mock
    private IFleetService fleetService;

    @Mock
    private HttpExchange httpExchange;

    @InjectMocks
    private FleetHandler fleetHandler;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializa os mocks do Mockito
        MockitoAnnotations.openMocks(this);
        fleetHandler = new FleetHandler(fleetService);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testHandlePostFleet_Success() throws Exception {
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet/Fleet1"));
        when(httpExchange.getRequestMethod()).thenReturn("POST");
        // Criar um mock para FleetRecordRequestBody
        FleetRecordRequestBody requestBody = new FleetRecordRequestBody(
                "Fleet1", List.of(1, 2, 3), 10);

        // Simular a leitura do corpo da requisição
        when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(
                objectMapper.writeValueAsBytes(requestBody)));

        // Criar um mock de FleetRecord (resposta esperada)
        var mockCrewRecordFleet = br.com.swapi.mock.Mock.getMockCrewRecordFleet();
        var mockStarshipInternalRecordFleet = br.com.swapi.mock.Mock.getMockStarshipRecord();
        FleetRecord mockFleetRecord = new FleetRecord("Fleet1", mockStarshipInternalRecordFleet, mockCrewRecordFleet);

        // Simular o retorno do método postFleet do serviço, utilizando any() para evitar a comparação exata
        when(fleetService.postFleet(any(FleetRecordRequestBody.class))).thenReturn(mockFleetRecord);

        // Mockar o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executar o método handlePostFleet
        fleetHandler.handlePostFleet(httpExchange);

        // Verificar se o método postFleet foi chamado corretamente
        verify(fleetService, times(1)).postFleet(any(FleetRecordRequestBody.class));

        // Verificar a resposta enviada
        String expectedJsonResponse = objectMapper.writeValueAsString(mockFleetRecord);
        String actualResponse = new String(outputStream.toByteArray());
        assertEquals(expectedJsonResponse, actualResponse);

        // Verificar se o código de status correto foi enviado
        verify(httpExchange).sendResponseHeaders(HttpStatus.CREATED.getCode(), expectedJsonResponse.length());
    }

    @Test
    public void testHandlePostFleet_Error() throws Exception {
        // Criar um mock para FleetRecordRequestBody
        FleetRecordRequestBody requestBody = new FleetRecordRequestBody("Fleet1", List.of(1, 2, 3), 10);

        // Simular a leitura do corpo da requisição
        when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(objectMapper.writeValueAsBytes(requestBody)));

        // Simular uma exceção lançada pelo método postFleet do serviço
        when(fleetService.postFleet(any(FleetRecordRequestBody.class))).thenThrow(new RuntimeException("Service Error"));

        // Mockar o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executar o método handlePostFleet
        fleetHandler.handlePostFleet(httpExchange);

        // Verificar se o método postFleet foi chamado corretamente
        verify(fleetService, times(1)).postFleet(any(FleetRecordRequestBody.class));

        // Verificar a resposta de erro enviada
        String expectedErrorResponse = "{\"error\": \"Service Error\"}";
        String actualResponse = new String(outputStream.toByteArray());
        assertEquals(expectedErrorResponse, actualResponse);

        // Verificar se o código de status correto foi enviado
        verify(httpExchange).sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), expectedErrorResponse.length());
    }


    @Test
    public void testHandleUpdateFleet_Success() throws Exception {
        // Simular a URI e o método PUT
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet/Fleet1"));
        when(httpExchange.getRequestMethod()).thenReturn("PUT");

        // Simular o corpo da requisição
        List<Integer> crewIds = List.of(1, 2, 3);
        when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(
                objectMapper.writeValueAsBytes(crewIds)));

        // Criar um mock de FleetRecord (resposta esperada)
        var mockCrewRecordFleet = br.com.swapi.mock.Mock.getMockCrewRecordFleet();
        var mockStarshipInternalRecordFleet = br.com.swapi.mock.Mock.getMockStarshipRecord();
        var updatedFleet = new FleetRecord("Fleet1", mockStarshipInternalRecordFleet, mockCrewRecordFleet);

        // Simular o retorno do serviço de updateFleet
        when(fleetService.updateFleet("Fleet1", crewIds)).thenReturn(updatedFleet);

        // Mockar o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executar o método que está sendo testado
        fleetHandler.handle(httpExchange);

        // Verificar se o método updateFleet foi chamado corretamente
        verify(fleetService, times(1)).updateFleet("Fleet1", crewIds);

        String expectedJsonResponse = objectMapper.writeValueAsString(updatedFleet);
        String actualResponse = new String(outputStream.toByteArray());
        assertEquals(expectedJsonResponse, actualResponse);

        // Verificar se o código de status correto foi enviado
        verify(httpExchange).sendResponseHeaders(HttpStatus.CREATED.getCode(), expectedJsonResponse.length());
    }

    @Test
    public void testHandleUpdateFleet_Error() throws Exception {
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet/Fleet1"));
        when(httpExchange.getRequestMethod()).thenReturn("PUT");

        // Simulando o corpo da requisição com a lista de crewIds
        List<Integer> crewIds = List.of(1, 2, 3);
        when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(objectMapper.writeValueAsBytes(crewIds)));

        when(fleetService.updateFleet("Fleet1", crewIds)).thenThrow(new RuntimeException("Service Error"));
        // Mockando o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        fleetHandler.handle(httpExchange);

        // Verifica se o fleetService.updateFleet foi chamado corretamente
        verify(fleetService, times(1)).updateFleet("Fleet1", crewIds);


        // Captura e verifica a resposta de erro gerada
        String jsonResponse = new String(outputStream.toByteArray());
        String expectedError = "{\"error\": \"Erro ao atualizar a frota: Service Error\"}";
        assertEquals(expectedError, jsonResponse);
    }

    @Test
    public void testHandleDeleteFleet_Success() throws Exception {
        // Simulando a URI e o método de requisição (DELETE)
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet/Fleet1"));
        when(httpExchange.getRequestMethod()).thenReturn("DELETE");

        // Mock do comportamento de exclusão no service
        doNothing().when(fleetService).deleteFleet("Fleet1");

        // Mockando o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executa o método handle que chama internamente o método handleDeleteFleet
        fleetHandler.handle(httpExchange);

        // Verifica se o fleetService.deleteFleet foi chamado corretamente
        verify(fleetService, times(1)).deleteFleet("Fleet1");

        // Verifica a resposta enviada
        String expectedResponse = "Frota deletada com sucesso: ";
        String actualResponse = new String(outputStream.toByteArray());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testHandleDeleteFleet_Error() throws Exception {
        // Simulando a URI e o método de requisição (DELETE)
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet/Fleet1"));
        when(httpExchange.getRequestMethod()).thenReturn("DELETE");

        // Simulando uma exceção quando o método deleteFleet é chamado
        doThrow(new RuntimeException("Erro ao deletar frota")).when(fleetService).deleteFleet("Fleet1");

        // Mockando o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executa o método handleDeleteFleet
        fleetHandler.handleDeleteFleet(httpExchange);

        // Verifica se o fleetService.deleteFleet foi chamado com o nome correto
        verify(fleetService, times(1)).deleteFleet("Fleet1");

        // Captura e verifica a resposta de erro gerada
        String expectedError = "{\"error\": \"Erro ao deletar frota: Erro ao deletar frota\"}";
        String actualErrorResponse = new String(outputStream.toByteArray());
        assertEquals(expectedError, actualErrorResponse);
    }

    @Test
    public void testHandleGetFleet_Success() throws Exception {
        // Utilizando os mocks da classe Mock
        var mockCrewRecordFleet = br.com.swapi.mock.Mock.getMockCrewRecordFleet();
        var mockStarshipInternalRecordFleet = br.com.swapi.mock.Mock.getMockStarshipRecord();

        // Simulando a URI e o método de requisição (GET)
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet?page=1"));
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        // Mockando a resposta do FleetService
        FleetRecord mockFleetRecord = new FleetRecord("Fleet1", mockStarshipInternalRecordFleet, mockCrewRecordFleet);
        List<FleetRecord> mockFleets = Collections.singletonList(mockFleetRecord);
        when(fleetService.getFleet(1, null)).thenReturn(mockFleets);

        // Mockando o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executa o método handleGetFleet
        fleetHandler.handleGetFleet(httpExchange);

        // Verifica se o FleetService foi chamado corretamente
        verify(fleetService, times(1)).getFleet(1, null);

        // Captura e compara a resposta JSON gerada com a esperada
        String jsonResponse = new String(outputStream.toByteArray());
        String expectedResponse = objectMapper.writeValueAsString(mockFleets);
        assertEquals(expectedResponse, jsonResponse);
    }

    @Test
    public void testHandleGetFleet_Error() throws Exception {
        // Simulando a URI e o método de requisição (GET)
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/fleet?page=1"));
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        // Simulando uma exceção quando o método getFleet é chamado
        when(fleetService.getFleet(1, null)).thenThrow(new RuntimeException("Service Error"));

        // Mockando o OutputStream e Headers para capturar a resposta
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Headers headers = new Headers();
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Executa o método handleGetFleet, que deve capturar a exceção
        fleetHandler.handleGetFleet(httpExchange);

        // Captura e verifica a resposta de erro gerada
        String jsonResponse = new String(outputStream.toByteArray());
        String expectedError = "{\"error\": \"Erro ao buscar frotas: Service Error\"}";
        assertEquals(expectedError, jsonResponse);
    }

    @Test
    public void testParseQueryParams_NullQuery() throws Exception {
        // Usando reflexão para acessar o método privado
        Method method = FleetHandler.class.getDeclaredMethod("parseQueryParams", String.class);
        method.setAccessible(true);

        // Teste com query nula
        Map<String, String> result = (Map<String, String>) method.invoke(fleetHandler, (String) null);

        // Verificar que o mapa retornado está vazio
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseQueryParams_EmptyQuery() throws Exception {
        // Usando reflexão para acessar o método privado
        Method method = FleetHandler.class.getDeclaredMethod("parseQueryParams", String.class);
        method.setAccessible(true);

        // Teste com query vazia
        Map<String, String> result = (Map<String, String>) method.invoke(fleetHandler, "");

        // Verificar que o mapa retornado está vazio
        assertTrue(result.isEmpty());
    }

}
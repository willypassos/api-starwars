package br.com.swapi.handler;

import br.com.swapi.client.CrewHandler;
import br.com.swapi.exception.GenericExceptionDTO;
import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.service.SWAPIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CrewHandlerTest {

    @Mock
    private SWAPIClient swapiClient;

    @Mock
    private HttpExchange httpExchange;

    @InjectMocks
    private CrewHandler crewHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleGetCrewByPage_Success() throws Exception {
        // Mockando os parâmetros de query e o SWAPIClient
        String query = "page=1&name=Luke";
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/crew?" + query));
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        // Mockando headers
        Headers headers = new Headers();
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Mockando o retorno do SWAPIClient
        List<CrewRecordFleet> mockCrewList = List.of(new CrewRecordFleet("Luke", "172", "77", "male", 1, true));
        when(swapiClient.getCrew(anyInt(), anyString())).thenReturn(mockCrewList);

        // Simulando o envio da resposta
        OutputStream os = new ByteArrayOutputStream();
        when(httpExchange.getResponseBody()).thenReturn(os);

        // Ação: chama o método handle que deve disparar o handleGetCrewByPage
        crewHandler.handle(httpExchange);

        // Verificações
        String expectedResponse = new ObjectMapper().writeValueAsString(mockCrewList);
        verify(httpExchange, times(1)).sendResponseHeaders(200, expectedResponse.length());
        assertEquals(expectedResponse, os.toString());
    }

    @Test
    void testHandleGetCrewByPage_NotFound() throws Exception {
        // Simulando um método não suportado
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/unknown"));
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        // Mockando headers
        Headers headers = new Headers();
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // Simulando o envio da resposta de erro
        OutputStream os = new ByteArrayOutputStream();
        when(httpExchange.getResponseBody()).thenReturn(os);

        // Ação: chama o método handle, que deve retornar erro de Not Found
        crewHandler.handle(httpExchange);

        // Verificações
        GenericExceptionDTO expectedError = new GenericExceptionDTO("404", "Not Found");
        String expectedResponse = new ObjectMapper().writeValueAsString(expectedError);
        verify(httpExchange, times(1)).sendResponseHeaders(404, expectedResponse.length());
        assertEquals(expectedResponse, os.toString());
    }


}

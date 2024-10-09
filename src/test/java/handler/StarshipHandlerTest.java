package handler;

import br.com.swapi.client.StarshipHandler;
import br.com.swapi.enums.HttpStatus;
import br.com.swapi.exception.GenericExceptionDTO;
import br.com.swapi.model.StarshipInternalRecordFleet;
import br.com.swapi.service.SWAPIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
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

class StarshipHandlerTest {

    @Mock
    private SWAPIClient swapiClient;

    @Mock
    private HttpExchange httpExchange;

    @InjectMocks
    private StarshipHandler starshipHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        starshipHandler = new StarshipHandler(swapiClient);  // Cria uma nova instância do handler com o mock do SWAPIClient
    }

    @Test
    void testHandleGetStarshipByPage_Success() throws Exception {
        String query = "page=1&name=X-Wing";
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/starship?" + query));
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        Headers headers = new Headers();
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        List<StarshipInternalRecordFleet> mockStarships = List.of(mock.Mock.getMockStarshipRecord());
        when(swapiClient.getStarships(anyInt(), anyString())).thenReturn(mockStarships);

        OutputStream os = new ByteArrayOutputStream();
        when(httpExchange.getResponseBody()).thenReturn(os);

        starshipHandler.handle(httpExchange);

        String expectedResponse = new ObjectMapper().writeValueAsString(mockStarships);
        long expectedLength = expectedResponse.getBytes().length;

        verify(httpExchange, times(1)).sendResponseHeaders(HttpStatus.OK.getCode(), expectedLength);
        assertEquals(expectedResponse, os.toString());
    }

    @Test
    void testHandleGetStarshipByPage_NotFound() throws Exception {
        String query = "page=1&name=Unknown";
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/starship?" + query));
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        // Mockando headers
        Headers headers = new Headers();
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        when(swapiClient.getStarships(anyInt(), anyString())).thenThrow(new IOException("pagina nao existe"));

        // Simulando o envio da resposta de erro
        OutputStream os = new ByteArrayOutputStream();
        when(httpExchange.getResponseBody()).thenReturn(os);

        starshipHandler.handle(httpExchange);

        GenericExceptionDTO expectedError = new GenericExceptionDTO("404", "pagina nao existe na api externa: pagina nao existe");
        String expectedResponse = new ObjectMapper().writeValueAsString(expectedError);
        long expectedLength = expectedResponse.getBytes().length;

        verify(httpExchange, times(1)).sendResponseHeaders(HttpStatus.NOT_FOUND.getCode(), expectedLength);
        assertEquals(expectedResponse, os.toString());
    }

    @Test
    void testHandle_InvalidMethod() throws Exception {
        when(httpExchange.getRequestURI()).thenReturn(new URI("/starwars/v1/starship"));
        when(httpExchange.getRequestMethod()).thenReturn("POST");

        Headers headers = new Headers();
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        OutputStream os = new ByteArrayOutputStream();
        when(httpExchange.getResponseBody()).thenReturn(os);

        starshipHandler.handle(httpExchange);

        GenericExceptionDTO expectedError = new GenericExceptionDTO("404", "Not Found");
        String expectedResponse = new ObjectMapper().writeValueAsString(expectedError);
        long expectedLength = expectedResponse.getBytes().length;

        verify(httpExchange, times(1)).sendResponseHeaders(HttpStatus.NOT_FOUND.getCode(), expectedLength);
        assertEquals(expectedResponse, os.toString());
    }
}

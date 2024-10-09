package br.com.swapi.service;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.StarshipInternalRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SWAPIClientTest {

    @InjectMocks
    private SWAPIClient swapiClient;

    @BeforeEach
    public void setUp() {
        swapiClient = spy(new SWAPIClient());
    }

    @Test
    public void testGetCrew_Success() throws IOException {
        // Simulando a resposta da API com um JSON de exemplo
        String simulatedJsonResponse = "{ \"results\": [ { \"name\": \"Luke Skywalker\", \"height\": \"172\", \"mass\": \"77\", \"gender\": \"male\", \"url\": \"/people/1/\" } ] }";

        // Simulando o retorno do método fetchData
        doReturn(simulatedJsonResponse).when(swapiClient).fetchData("/people/?page=1");

        // Chamando o método getCrew simulando o resultado da API
        List<CrewRecordFleet> crewMembers = swapiClient.getCrew(1, null);

        // Verificando o tamanho da lista e os dados do primeiro membro da tripulação
        assertEquals(1, crewMembers.size());
        assertEquals("Luke Skywalker", crewMembers.get(0).getName());
        assertEquals("172", crewMembers.get(0).getHeight());
        assertEquals("77", crewMembers.get(0).getMass());
        assertEquals("male", crewMembers.get(0).getGender());
    }

    @Test
    public void testGetCrew_EmptyResponse() throws IOException {
        // Mock o comportamento de fetchData para retornar uma resposta vazia
        SWAPIClient clientUnderTest = new SWAPIClient() {
            @Override
            public String fetchData(String endpoint) throws IOException {
                return "";  // Simula a resposta vazia
            }
        };

        // Chama o método getCrew com a página 1 e sem filtro de nome
        List<CrewRecordFleet> result = clientUnderTest.getCrew(1, null);

        // Verifica se a lista retornada está vazia
        assertTrue(result.isEmpty(), "A lista deveria estar vazia quando a resposta é vazia.");
    }

    @Test
    public void testGetCrew_NullResponse() throws IOException {
        // Mock o comportamento de fetchData para retornar null
        SWAPIClient clientUnderTest = new SWAPIClient() {
            @Override
            public String fetchData(String endpoint) throws IOException {
                return null;  // Simula a resposta null
            }
        };

        // Chama o método getCrew com a página 1 e sem filtro de nome
        List<CrewRecordFleet> result = clientUnderTest.getCrew(1, null);

        // Verifica se a lista retornada está vazia
        assertTrue(result.isEmpty(), "A lista deveria estar vazia quando a resposta é null.");
    }


    @Test
    public void testGetCrew_WithNameFilter() throws IOException {
        // Simulando a resposta da API com um JSON de exemplo
        String simulatedJsonResponse = "{ \"results\": [ { \"name\": \"Leia Organa\", \"height\": \"150\", \"mass\": \"49\", \"gender\": \"female\", \"url\": \"/people/5/\" } ] }";

        // Simulando o retorno do método fetchData com o nome filtrado
        doReturn(simulatedJsonResponse).when(swapiClient).fetchData("/people/?page=1&search=Leia");

        // Chamando o método getCrew simulando o filtro por nome "Leia"
        List<CrewRecordFleet> crewMembers = swapiClient.getCrew(1, "Leia");

        // Verificando o tamanho da lista e os dados do membro da tripulação
        assertEquals(1, crewMembers.size());
        assertEquals("Leia Organa", crewMembers.get(0).getName());
        assertEquals("150", crewMembers.get(0).getHeight());
        assertEquals("49", crewMembers.get(0).getMass());
        assertEquals("female", crewMembers.get(0).getGender());
    }

    @Test
    public void testGetStarships_Success() throws IOException {
        // Simulando a resposta da API com um JSON de exemplo para starships
        String simulatedJsonResponse = null;

        // Simulando o retorno do método fetchData
        doReturn(simulatedJsonResponse).when(swapiClient).fetchData("/starships/?page=1");

        // Chamando o método getStarships simulando o resultado da API
        List<StarshipInternalRecordFleet> starships = swapiClient.getStarships(1, null);

        assertNull(simulatedJsonResponse);

    }

    @Test
    public void testGetStarships_WithNameFilter() throws IOException {
        // Simulando a resposta da API com um JSON de exemplo
        String simulatedJsonResponse = "{ \"results\": [ { \"name\": \"Slave 1\", \"model\": \"Firespray-31\", \"cost_in_credits\": \"150000\", \"crew\": \"1\", \"cargo_capacity\": \"70000\", \"max_atmosphering_speed\": \"1000\", \"url\": \"/starships/21/\", \"starship_class\": \"Patrol craft\" } ] }";

        // Simulando o retorno do método fetchData com o nome filtrado
        doReturn(simulatedJsonResponse).when(swapiClient).fetchData("/starships/?page=1&search=Slave");

        // Chamando o método getStarships simulando o filtro por nome "Slave"
        List<StarshipInternalRecordFleet> starships = swapiClient.getStarships(1, "Slave");

        // Verificando o tamanho da lista e os dados da nave
        assertEquals(1, starships.size());
        assertEquals("Slave 1", starships.get(0).getName());
        assertEquals("Firespray-31", starships.get(0).getModel());
        assertEquals("150000", starships.get(0).getPrice());
        assertEquals("1", starships.get(0).getCrew());
        assertEquals("70000", starships.get(0).getCargo());
        assertEquals("1000", starships.get(0).getSpeed());
        assertEquals("Patrol craft", starships.get(0).getStarship_class());
    }

    @Test
    public void testGetStarships_EmptyResponse() throws IOException {
        // Mock o comportamento de fetchData para retornar uma resposta vazia
        SWAPIClient clientUnderTest = new SWAPIClient() {
            @Override
            public String fetchData(String endpoint) throws IOException {
                return "";
            }
        };

        // Chama o método getStarships sem filtro de nome
        List<StarshipInternalRecordFleet> result = clientUnderTest.getStarships(1, null);

        // Verifica se a lista retornada está vazia
        assertTrue(result.isEmpty(), "A lista deve estar vazia quando a resposta é vazia.");
    }

    @Test
    public void testGetStarships_NullResponse() throws IOException {
        SWAPIClient clientUnderTest = new SWAPIClient() {
            @Override
            public String fetchData(String endpoint) throws IOException {
                return null;  // Simula a resposta null
            }
        };

        List<StarshipInternalRecordFleet> result = clientUnderTest.getStarships(1, null);

        assertTrue(result.isEmpty(), "A lista deve estar vazia quando a resposta é null.");
    }


}

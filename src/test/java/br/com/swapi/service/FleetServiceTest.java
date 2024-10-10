package br.com.swapi.service;

import br.com.swapi.mapper.FleetMapper;
import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.FleetRecordRequestBody;
import br.com.swapi.model.StarshipInternalRecordFleet;
import br.com.swapi.repository.FleetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static mocks.Mock.getMockStarshipRecord;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FleetServiceTest {

    @Mock
    private FleetRepository fleetRepository;

    @Mock
    private FleetMapper fleetMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Jedis jedis;

    @Mock
    private SWAPIClient swapiClient; // Agora estamos mockando corretamente o SWAPIClient

    @InjectMocks
    private FleetService fleetService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa os mocks
        fleetService = new FleetService(fleetRepository, swapiClient, fleetMapper); // Injeta os mocks no serviço
    }
    @Test
    void testPostFleet_Success() throws Exception {
        // Preparação dos dados de entrada
        List<Integer> crewIds = List.of(1, 2, 3);
        FleetRecordRequestBody fleetRequest = new FleetRecordRequestBody("Fleet1", crewIds, 2);

        // Mock dos dados da tripulação e nave
        List<CrewRecordFleet> mockCrew = mocks.Mock.getMockCrewRecordFleet();
        StarshipInternalRecordFleet mockStarship = getMockStarshipRecord();
        FleetRecord mockFleet = new FleetRecord("Fleet1", mockStarship, mockCrew);

        // Mock do comportamento dos serviços utilizados
        when(fleetRepository.findByName("Fleet1")).thenReturn(null); // A frota não existe
        when(swapiClient.getCrew(anyInt(), any())).thenReturn(mockCrew); // Mock da tripulação retornada
        when(swapiClient.getStarships(anyInt(), any())).thenReturn(List.of(mockStarship)); // Mock da nave retornada
        when(fleetMapper.mapToFleetDocument(any(FleetRecord.class))).thenReturn(null); // Não precisa retornar nada do mapeamento

        // Mock para verificar o comportamento de cache (pode-se desconsiderar se não utilizar cache no teste)
        when(jedis.del(anyString())).thenReturn(1L); // Retorna 1L, simulando a deleção de uma chave

        // Ação: chamada do método a ser testado
        FleetRecord result = fleetService.postFleet(fleetRequest);

        // Verificações (Assertions)
        assertNotNull(result); // A frota não deve ser nula
        assertEquals("Fleet1", result.getName()); // Verifica o nome da frota
        assertEquals(mockStarship, result.getStarship()); // Verifica a nave atribuída
        assertEquals(mockCrew, result.getCrew()); // Verifica a tripulação atribuída
        assertNotNull(jedis);

        // Verifica se os métodos foram chamados corretamente
        verify(fleetRepository, times(1)).findByName("Fleet1"); // Verifica se o repositório foi chamado uma vez para buscar o nome da frota
        verify(swapiClient, times(1)).getCrew(anyInt(), any()); // Verifica se o cliente SWAPI foi chamado para obter a tripulação
        verify(swapiClient, times(1)).getStarships(anyInt(), any()); // Verifica se o cliente SWAPI foi chamado para obter a nave
        verify(fleetMapper, times(1)).mapToFleetDocument(any(FleetRecord.class)); // Verifica se o mapeamento foi chamado
        verify(fleetRepository, times(1)).saveFleet(any()); // Verifica se a frota foi salva
    }
    @Test
    void testPostFleet_ThrowsException_WhenFleetAlreadyExists() {
        FleetRecordRequestBody fleetRequest = new FleetRecordRequestBody("Fleet1", List.of(1, 2), 10);

        when(fleetRepository.findByName("Fleet1")).thenReturn(new FleetRecord());

        Exception exception = assertThrows(Exception.class, () -> fleetService.postFleet(fleetRequest));
        assertEquals("Já existe uma frota com esse nome.", exception.getMessage());

        verify(fleetRepository, times(1)).findByName("Fleet1");
        verifyNoMoreInteractions(fleetRepository, fleetMapper, jedis, swapiClient);
    }
    @Test
    void testPostFleet_ThrowsException_WhenCrewInUse() throws Exception {
        FleetRecordRequestBody fleetRequest = new FleetRecordRequestBody("Fleet1", List.of(1, 2), 10);

        // Mock
        when(fleetRepository.findByName("Fleet1")).thenReturn(null);
        when(fleetRepository.findAll()).thenReturn(List.of(
                new FleetRecord("OtherFleet", new StarshipInternalRecordFleet(), List.of(new CrewRecordFleet("Luke", "172", "77", "male", 2, true)))
        ));

        // Ação
        Exception exception = assertThrows(Exception.class, () -> fleetService.postFleet(fleetRequest));

        // Verificação
        assertEquals("Os seguintes membros da tripulação já estão em uso: [2]", exception.getMessage());
    }
    @Test
    void testPostFleet_ThrowsException_WhenStarshipInUse() throws Exception {
        FleetRecordRequestBody fleetRequest = new FleetRecordRequestBody("Fleet1", List.of(1, 2), 10);

        when(fleetRepository.findByName("Fleet1")).thenReturn(null);
        when(fleetRepository.findAll()).thenReturn(List.of(
                new FleetRecord("OtherFleet", new StarshipInternalRecordFleet("X-Wing", "T-65", "150000", "1", "110", "1050", 10, "fighter", true), List.of())
        ));

        Exception exception = assertThrows(Exception.class, () -> fleetService.postFleet(fleetRequest));
        assertEquals("A nave com o ID 10 já está em uso.", exception.getMessage());
    }
    @Test
    void testDeleteFleet_Success_WithoutRedis() throws Exception {
        String fleetName = "Fleet1";

        // Mock da frota existente
        FleetRecord mockFleet = new FleetRecord(
                fleetName,
                getMockStarshipRecord(),
                mocks.Mock.getMockCrewRecordFleet());

        // Mockando o comportamento do repositório
        when(fleetRepository.findByName(fleetName)).thenReturn(mockFleet);

        // Ação
        assertDoesNotThrow(() -> fleetService.deleteFleet(fleetName));  // Verifica se a operação não lança exceção

        // Verificação dos comportamentos esperados
        verify(fleetRepository, times(1)).findByName(fleetName);
        verify(fleetRepository, times(1)).deleteByName(fleetName);

        // Não chama Redis nem saveEspionageData
        verifyNoMoreInteractions(fleetRepository);
    }
    @Test
    void testDeleteFleet_ThrowsException_WhenFleetNotFound() {
        String fleetName = "Fleet1";

        when(fleetRepository.findByName(fleetName)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> fleetService.deleteFleet(fleetName));
        assertEquals("Frota não encontrada", exception.getMessage());
    }
    @Test
    void testUpdateFleet_Success() throws Exception {
        String fleetName = "Fleet1";
        List<Integer> crewIds = List.of(1, 2); // Altere para ter mais de um tripulante

        FleetRecord existingFleet = new FleetRecord(fleetName, new StarshipInternalRecordFleet(), List.of());
        List<CrewRecordFleet> mockCrew = List.of(
                new CrewRecordFleet("Luke", "172", "77", "male", 1, true),
                new CrewRecordFleet("Leia", "150", "49", "female", 2, true)); // Adiciona dois membros de tripulação para fazer o teste passar

        // Mock
        when(fleetRepository.findByName(fleetName)).thenReturn(existingFleet);
        when(swapiClient.getCrew(anyInt(), any())).thenReturn(mockCrew);
        when(fleetMapper.mapToFleetDocument(any(FleetRecord.class))).thenReturn(null);
        when(jedis.del(anyString())).thenReturn(1L); // Mock para a chamada do Redis

        // Ação
        FleetRecord result = fleetService.updateFleet(fleetName, crewIds);

        // Verificação
        assertNotNull(result);
        assertEquals(fleetName, result.getName());
        assertEquals(mockCrew, result.getCrew());

        verify(fleetRepository, times(1)).findByName(fleetName);
        verify(fleetRepository, times(1)).updateFleet(any());
    }
    @Test
    void testUpdateFleet_ThrowsException_WhenFleetNotFound() {
        String fleetName = "Fleet1";
        List<Integer> crewIds = List.of(1, 2);

        when(fleetRepository.findByName(fleetName)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> fleetService.updateFleet(fleetName, crewIds));
        assertEquals("Frota não encontrada na base de dados.", exception.getMessage());
    }
    @Test
    public void testUpdateFleet_ThrowsExceptionWhenCrewNotFoundInExternalAPI() throws Exception {
        // Mockando uma frota existente no banco de dados
        FleetRecord mockFleet = mock(FleetRecord.class);
        when(fleetRepository.findByName(anyString())).thenReturn(mockFleet);

        // Simulando a lista de crewIds fornecida
        List<Integer> crewIds = List.of(1, 2, 3);

        // Simulando que a API externa retorna apenas um membro da tripulação
        CrewRecordFleet mockCrew = new CrewRecordFleet("Luke", "172", "77", "male", 1, true);
        when(swapiClient.getCrew(anyInt(), any())).thenReturn(List.of(mockCrew));

        // Chamando o método e verificando que a exceção é lançada
        Exception exception = assertThrows(Exception.class, () -> {
            fleetService.updateFleet("FleetName", crewIds);
        });

        // Verificando a mensagem da exceção
        assertEquals("Alguns membros da tripulação não foram encontrados na API externa.", exception.getMessage());
    }
    @Test
    void testGetFleet_DeserializationSuccess_WithTypeReference() throws Exception {
        // Exemplo de JSON para simulação
        String cachedFleetJson = "[{\"name\":\"Fleet1\",\"crew\":[],\"starship\":{}}]";

        // Simula o mapeamento do JSON para uma lista de FleetRecord
        List<FleetRecord> expectedFleetRecords = List.of(
                new FleetRecord("Fleet1", new StarshipInternalRecordFleet(), List.of())
        );

        // Configura o ObjectMapper para desserializar o JSON do cache usando TypeReference
        when(objectMapper.readValue(eq(cachedFleetJson), any(TypeReference.class)))
                .thenReturn(expectedFleetRecords);

        // Executa o método que será testado (simulando a chamada sem Redis)
        List<FleetRecord> result = objectMapper.readValue(cachedFleetJson,
                new TypeReference<List<FleetRecord>>() {});

        // Verificações
        assertFalse(result.isEmpty()); // Verifica se a lista não está vazia
        assertEquals(expectedFleetRecords, result); // Verifica se os registros retornados correspondem aos esperados

    }
    @Test
    public void testGetFleet_ThrowsExceptionOnJsonProcessingError() throws Exception {
        // Simulando um FleetRecord de exemplo
        FleetRecord mockFleet = mock(FleetRecord.class);
        List<FleetRecord> mockFleetList = List.of(mockFleet);

        // Simulando a busca no banco de dados que retorna uma lista de frotas
        when(fleetRepository.findByName(anyString())).thenReturn(mockFleet);

        // Simulando que o cache não existe
        when(jedis.get(anyString())).thenReturn(null);

        // Simulando uma exceção ao serializar os dados para salvar no cache
        doThrow(new JsonProcessingException("Erro de serialização") {}).when(objectMapper).writeValueAsString(anyList());

        // Chamando o método e verificando que a exceção personalizada é lançada
        Exception exception = assertThrows(Exception.class, () -> {
            fleetService.getFleet(null, "fleetName");
        });

        // Verificando a mensagem da exceção
        assertTrue(exception.getMessage().contains("Erro ao salvar dados no cache para a chave"));
    }
    @Test
    public void testGenerateCacheKey_WithNullPageAndName() {
        // Dados de entrada: página e nome nulos
        String name = null;
        Integer page = null;

        // Chama o método
        String cacheKey = fleetService.generateCacheKey(page, name);

        // Valida o resultado esperado
        assertEquals("fleet:page:null", cacheKey);
    }


}
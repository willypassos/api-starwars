package br.com.swapi.mapper;

import br.com.swapi.mapper.CrewMapper;
import br.com.swapi.model.CrewRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrewMapperTest {

    private CrewMapper crewMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        crewMapper = new CrewMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testMapToCrew_Success() throws IOException {
        // JSON simulado para um tripulante
        String crewJsonString = """
            {
                "name": "Luke Skywalker",
                "height": "172",
                "mass": "77",
                "gender": "male",
                "url": "https://swapi.dev/api/people/1/"
            }
        """;

        // Converte o JSON string para JsonNode
        JsonNode crewJson = objectMapper.readTree(crewJsonString);

        // Chama o método que está sendo testado
        CrewRecordFleet crew = crewMapper.mapToCrew(crewJson);

        // Verifica os valores mapeados
        assertEquals("Luke Skywalker", crew.getName());
        assertEquals("172", crew.getHeight());
        assertEquals("77", crew.getMass());
        assertEquals("male", crew.getGender());
        assertEquals(1, crew.getExternalId());
        assertTrue(crew.isAvailable());  // Verifica se o personagem está disponível
    }

    @Test
    public void testMapCrewFromJson_Success() throws IOException {
        // JSON simulado para uma lista de tripulantes
        String crewListJsonString = """
            {
                "crew": [
                    {
                        "name": "Luke Skywalker",
                        "height": "172",
                        "mass": "77",
                        "gender": "male",
                        "url": "https://swapi.dev/api/people/1/"
                    },
                    {
                        "name": "Leia Organa",
                        "height": "150",
                        "mass": "49",
                        "gender": "female",
                        "url": "https://swapi.dev/api/people/2/"
                    }
                ]
            }
        """;

        // Converte o JSON string para JsonNode
        JsonNode crewListJson = objectMapper.readTree(crewListJsonString);

        // Chama o método que está sendo testado
        List<CrewRecordFleet> crewList = crewMapper.mapCrewFromJson(crewListJson);

        // Verifica o número de membros da tripulação
        assertEquals(2, crewList.size());

        // Verifica o primeiro tripulante
        CrewRecordFleet firstCrewMember = crewList.get(0);
        assertEquals("Luke Skywalker", firstCrewMember.getName());
        assertEquals("172", firstCrewMember.getHeight());
        assertEquals("77", firstCrewMember.getMass());
        assertEquals("male", firstCrewMember.getGender());
        assertEquals(1, firstCrewMember.getExternalId());
        assertTrue(firstCrewMember.isAvailable());

        // Verifica o segundo tripulante
        CrewRecordFleet secondCrewMember = crewList.get(1);
        assertEquals("Leia Organa", secondCrewMember.getName());
        assertEquals("150", secondCrewMember.getHeight());
        assertEquals("49", secondCrewMember.getMass());
        assertEquals("female", secondCrewMember.getGender());
        assertEquals(2, secondCrewMember.getExternalId());
        assertTrue(secondCrewMember.isAvailable());
    }

    @Test
    public void testMapCrewFromJson_EmptyCrewList() throws IOException {
        // JSON simulado sem tripulantes
        String emptyCrewJsonString = """
            {
                "crew": []
            }
        """;

        // Converte o JSON string para JsonNode
        JsonNode crewListJson = objectMapper.readTree(emptyCrewJsonString);

        // Chama o método que está sendo testado
        List<CrewRecordFleet> crewList = crewMapper.mapCrewFromJson(crewListJson);

        // Verifica que a lista está vazia
        assertTrue(crewList.isEmpty());
    }

    @Test
    public void testMapCrewFromJson_NullCrew() throws IOException {
        // JSON simulado com valor null para crew
        String nullCrewJsonString = """
            {
                "crew": null
            }
        """;

        // Converte o JSON string para JsonNode
        JsonNode crewListJson = objectMapper.readTree(nullCrewJsonString);

        // Chama o método que está sendo testado
        List<CrewRecordFleet> crewList = crewMapper.mapCrewFromJson(crewListJson);

        // Verifica que a lista está vazia
        assertTrue(crewList.isEmpty());
    }

}

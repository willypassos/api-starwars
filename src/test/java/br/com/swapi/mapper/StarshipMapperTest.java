package br.com.swapi.mapper;

import br.com.swapi.model.StarshipInternalRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StarshipMapperTest {

    private StarshipMapper starshipMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        starshipMapper = new StarshipMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testMapStarshipFromJson_Success() throws Exception {
        // Configura um JSON fictício para a nave espacial
        String starshipJsonStr = "{ \"name\": \"Millennium Falcon\", \"model\": \"YT-1300\", \"cost_in_credits\": \"100000\", \"crew\": \"4\", \"cargo_capacity\": \"100000\", \"max_atmosphering_speed\": \"1050\", \"url\": \"/starships/10/\", \"starship_class\": \"Freighter\" }";
        JsonNode starshipJson = objectMapper.readTree(starshipJsonStr);

        // Chama o método de mapeamento
        StarshipInternalRecord starship = starshipMapper.mapStarshipFromJson(starshipJson, true);

        // Verifica os valores mapeados
        assertEquals("Millennium Falcon", starship.getName());
        assertEquals("YT-1300", starship.getModel());
        assertEquals("100000", starship.getPrice());
        assertEquals("4", starship.getCrew());
        assertEquals("100000", starship.getCargo());
        assertEquals("1050", starship.getSpeed());
        assertEquals(10, starship.getExternalId());  // Verifica se o ID foi extraído corretamente
        assertEquals("Freighter", starship.getStarshipClass());
        assertTrue(starship.isAvailable());
    }

    @Test
    public void testMapStarshipFromJson_MissingFields() throws Exception {
        // Configura um JSON fictício com campos ausentes
        String starshipJsonStr = "{ \"name\": \"X-Wing\", \"model\": \"T-65 X-Wing\", \"url\": \"/starships/12/\" }";
        JsonNode starshipJson = objectMapper.readTree(starshipJsonStr);

        // Chama o método de mapeamento
        StarshipInternalRecord starship = starshipMapper.mapStarshipFromJson(starshipJson, false);

        // Verifica os valores mapeados e os campos vazios
        assertEquals("X-Wing", starship.getName());
        assertEquals("T-65 X-Wing", starship.getModel());
        assertEquals("", starship.getPrice());  // Campo ausente no JSON
        assertEquals("", starship.getCrew());   // Campo ausente no JSON
        assertEquals("", starship.getCargo());  // Campo ausente no JSON
        assertEquals("", starship.getSpeed());  // Campo ausente no JSON
        assertEquals(12, starship.getExternalId());  // Verifica se o ID foi extraído corretamente
        assertEquals("", starship.getStarshipClass());  // Campo ausente no JSON
        assertFalse(starship.isAvailable());  // Verifica a disponibilidade
    }

    @Test
    public void testMapStarshipFromJson_InvalidUrl() throws Exception {
        // Configura um JSON fictício com uma URL inválida
        String starshipJsonStr = "{ \"name\": \"TIE Fighter\", \"url\": \"/starships/invalid-url/\" }";
        JsonNode starshipJson = objectMapper.readTree(starshipJsonStr);

        // Chama o método de mapeamento
        StarshipInternalRecord starship = starshipMapper.mapStarshipFromJson(starshipJson, true);

        // Verifica que o ID não foi extraído corretamente
        assertEquals(-1, starship.getExternalId());  // Valor de fallback para URL inválida
        assertEquals("TIE Fighter", starship.getName());
    }

    @Test
    public void testExtractIdFromUrl_Success() {
        // Testa a extração de ID com uma URL válida
        String validUrl = "/starships/10/";
        int id = starshipMapper.mapStarshipFromJson(new ObjectMapper().createObjectNode().put("url", validUrl), true).getExternalId();
        assertEquals(10, id);
    }

    @Test
    public void testExtractIdFromUrl_InvalidUrl() {
        // Testa a extração de ID com uma URL inválida
        String invalidUrl = "/starships/invalid/";
        int id = starshipMapper.mapStarshipFromJson(new ObjectMapper().createObjectNode().put("url", invalidUrl), true).getExternalId();
        assertEquals(-1, id);  // Verifica se o ID inválido retorna -1
    }

    @Test
    public void testExtractIdFromUrl_EmptyUrl() {
        // Testa a extração de ID com uma URL vazia
        String emptyUrl = "";
        int id = starshipMapper.mapStarshipFromJson(new ObjectMapper().createObjectNode().put("url", emptyUrl), true).getExternalId();
        assertEquals(-1, id);  // Verifica se uma URL vazia retorna -1
    }
}

package br.com.swapi.service;

import br.com.swapi.model.StarshipInternalRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StarshipService implements IStarshipService {

    private final SWAPIClient swapiClient = new SWAPIClient();  // Cliente para acessar a SWAPI
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Método conforme o Swagger (operationId: getStarshipByPage)
    public List<StarshipInternalRecordFleet> getStarshipByPage(int page, String name) throws IOException {
        // Monta a URL para a consulta da SWAPI
        String endpoint = "/starships/?page=" + page;
        if (name != null && !name.isEmpty()) {
            endpoint += "&search=" + name;
        }

        String response = swapiClient.fetchData(endpoint);  // Faz a requisição à SWAPI
        return mapStarshipsFromJson(response);  // Processa e mapeia os dados recebidos
    }

    // Método para mapear as naves a partir do JSON
    private List<StarshipInternalRecordFleet> mapStarshipsFromJson(String json) throws IOException {
        List<StarshipInternalRecordFleet> starships = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode results = rootNode.get("results");

        if (results.isArray()) {
            for (JsonNode starshipJson : results) {
                StarshipInternalRecordFleet starship = mapStarshipFromJson(starshipJson);
                starships.add(starship);
            }
        }

        return starships;
    }

    // Método de mapeamento de uma nave a partir do JSON da SWAPI
    private StarshipInternalRecordFleet mapStarshipFromJson(JsonNode starshipJson) {
        return new StarshipInternalRecordFleet(
                starshipJson.get("name").asText(),
                starshipJson.get("model").asText(),
                starshipJson.get("cost_in_credits").asText(),
                starshipJson.get("crew").asText(),
                starshipJson.get("cargo_capacity").asText(),
                starshipJson.get("max_atmosphering_speed").asText(),
                extractIdFromUrl(starshipJson.get("url").asText()),
                starshipJson.get("starship_class").asText(),
                true  // Supondo que a nave está disponível
        );
    }

    // Método auxiliar para extrair o ID da URL da nave
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}

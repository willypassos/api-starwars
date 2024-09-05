package br.com.swapi.service;

import br.com.swapi.model.StarshipInternalRecord;
import br.com.swapi.repository.StarshipRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StarshipService implements IStarshipService {

    private final SWAPIClient swapiClient;
    private final StarshipRepository starshipRepository;
    private final ObjectMapper objectMapper;

    public StarshipService(SWAPIClient swapiClient, StarshipRepository starshipRepository) {
        this.swapiClient = swapiClient;
        this.starshipRepository = starshipRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<StarshipInternalRecord> getStarshipByPage(int page, String name) throws IOException {
        List<StarshipInternalRecord> starships = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            // Verificar no banco de dados
            StarshipInternalRecord starship = starshipRepository.findStarshipByName(name);
            if (starship != null) {
                starships.add(starship);
                return starships;
            }
        }

        // Consulta a SWAPI
        String endpoint = "/starships/?page=" + page;
        String response = swapiClient.fetchData(endpoint);
        starships = parseStarships(response);

        // Salvar as naves no banco
        for (StarshipInternalRecord starship : starships) {
            if (starshipRepository.findStarshipByName(starship.getName()) == null) {
                starshipRepository.saveStarship(starship);
            }
        }

        return filterStarshipsByName(starships, name);
    }

    // Método que processa os dados da API e converte em objetos
    private List<StarshipInternalRecord> parseStarships(String json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode results = rootNode.get("results");
        List<StarshipInternalRecord> starshipList = new ArrayList<>();

        if (results.isArray()) {
            for (JsonNode starshipJson : results) {
                int externalId = extractIdFromUrl(starshipJson.get("url").asText());
                if (externalId < 1) {
                    continue;
                }

                String name = starshipJson.get("name").asText();
                String model = starshipJson.has("model") ? starshipJson.get("model").asText() : "";
                String price = starshipJson.has("cost_in_credits") ? starshipJson.get("cost_in_credits").asText() : "";
                String crew = starshipJson.has("crew") ? starshipJson.get("crew").asText() : "";
                String cargo = starshipJson.has("cargo_capacity") ? starshipJson.get("cargo_capacity").asText() : "";
                String speed = starshipJson.has("max_atmosphering_speed") ? starshipJson.get("max_atmosphering_speed").asText() : "";
                String starshipClass = starshipJson.has("starship_class") ? starshipJson.get("starship_class").asText() : "";

                boolean available = checkAvailability(externalId, name);

                StarshipInternalRecord starship = new StarshipInternalRecord(name, model, price, crew, cargo, speed, externalId, starshipClass, available);
                starshipList.add(starship);
            }
        }

        return starshipList;
    }

    // Filtro para buscar naves pelo nome
    private List<StarshipInternalRecord> filterStarshipsByName(List<StarshipInternalRecord> starships, String name) {
        if (name == null || name.isEmpty()) {
            return starships;
        }
        List<StarshipInternalRecord> filteredStarships = new ArrayList<>();
        for (StarshipInternalRecord starship : starships) {
            if (starship.getName().equalsIgnoreCase(name)) {
                filteredStarships.add(starship);
            }
        }
        return filteredStarships;
    }

    // Método auxiliar para extrair o ID de uma URL
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    // Método para verificar se a nave já está disponível
    private boolean checkAvailability(int externalId, String name) {
        StarshipInternalRecord starship = starshipRepository.findStarshipByName(name);
        return starship == null;
    }
}

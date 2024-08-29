package br.com.swapi.service;

import br.com.swapi.model.StarshipInternalRecord;
import br.com.swapi.repository.StarshipRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StarshipService implements IStarshipService {

    private final SWAPIClient swapiClient;
    private final StarshipRepository starshipRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StarshipService(SWAPIClient swapiClient, StarshipRepository starshipRepository) {
        this.swapiClient = swapiClient;
        this.starshipRepository = starshipRepository;
    }

    @Override
    public List<StarshipInternalRecord> getStarshipByPage(int page, String name) throws IOException {
        List<StarshipInternalRecord> starships = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            StarshipInternalRecord starship = starshipRepository.findStarshipByName(name);
            if (starship != null) {
                starships.add(starship);
                return starships;
            }
        }

        String endpoint = "/starships/?page=" + page;
        String response = swapiClient.fetchData(endpoint);
        starships = parseStarships(response);

        // Salvar as naves no banco e cache
        for (StarshipInternalRecord starship : starships) {
            starshipRepository.saveStarship(
                    starship.getName(),
                    starship.getCargo(),
                    starship.getStarshipClass(),
                    starship.getSpeed()
            );
        }

        return filterStarshipsByName(starships, name);
    }


    private List<StarshipInternalRecord> parseStarships(String json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode results = rootNode.get("results");
        List<StarshipInternalRecord> starshipList = new ArrayList<>();

        if (results.isArray()) {
            for (JsonNode starshipJson : results) {
                int externalId = extractIdFromUrl(starshipJson.get("url").asText());
                if (externalId < 1) {
                    continue; // Pula registros inválidos
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

    private List<StarshipInternalRecord> filterStarshipsByName(List<StarshipInternalRecord> starships, String name) {
        if (name == null || name.isEmpty()) {
            return starships; // Se o nome não for fornecido, retorne a lista completa
        }
        List<StarshipInternalRecord> filteredStarships = new ArrayList<>();
        for (StarshipInternalRecord starship : starships) {
            if (starship.getName().equalsIgnoreCase(name)) {
                filteredStarships.add(starship);
            }
        }
        return filteredStarships;
    }

    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private boolean checkAvailability(int externalId, String name) {
        StarshipInternalRecord starship = starshipRepository.findStarshipByName(name);
        return starship == null;
    }
}

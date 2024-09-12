package br.com.swapi.service;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.StarshipInternalRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SWAPIClient {

    private static final String BASE_URL = "https://swapi.dev/api";
    private final ObjectMapper objectMapper;

    public SWAPIClient() {
        this.objectMapper = new ObjectMapper();
    }

    // Método para buscar tripulações (CrewRecordFleet)
    public List<CrewRecordFleet> getCrew(int page, String name) throws IOException {
        String endpoint = "/people/?page=" + page;  // Endpoint da SWAPI para personagens
        if (name != null && !name.isEmpty()) {
            endpoint += "&search=" + name;  // Acrescenta o nome no endpoint para filtrar por nome
        }

        String response = fetchData(endpoint);  // Faz a requisição
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();  // Retorna uma lista vazia se não houver dados
        }

        JsonNode rootNode = objectMapper.readTree(response);  // Converte a resposta para JSON
        JsonNode results = rootNode.get("results");

        List<CrewRecordFleet> crewMembers = new ArrayList<>();  // Lista para armazenar os tripulantes
        for (JsonNode crewJson : results) {
            CrewRecordFleet crew = mapToCrewRecordFleet(crewJson);  // Converte cada JSON para CrewRecordFleet
            crewMembers.add(crew);
        }
        return crewMembers;
    }

    // Método para buscar naves (StarshipInternalRecordFleet)
    public List<StarshipInternalRecordFleet> getStarships(int page, String name) throws IOException {
        String endpoint = "/starships/?page=" + page;  // Endpoint da SWAPI para naves
        if (name != null && !name.isEmpty()) {
            endpoint += "&search=" + name;  // Acrescenta o nome no endpoint para filtrar por nome
        }

        String response = fetchData(endpoint);  // Faz a requisição
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();  // Retorna uma lista vazia se não houver dados
        }

        JsonNode rootNode = objectMapper.readTree(response);  // Converte a resposta para JSON
        JsonNode results = rootNode.get("results");

        List<StarshipInternalRecordFleet> starships = new ArrayList<>();  // Lista para armazenar as naves
        for (JsonNode starshipJson : results) {
            StarshipInternalRecordFleet starship = mapToStarshipInternalRecordFleet(starshipJson);  // Converte cada JSON para StarshipInternalRecordFleet
            starships.add(starship);
        }
        return starships;
    }

    // Método genérico para fazer requisições HTTP
    public String fetchData(String endpoint) throws IOException {
        String urlToCall = BASE_URL + endpoint;
        URL url = new URL(urlToCall);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            StringBuilder content;
            try (var in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            conn.disconnect();
            return content.toString();  // Retorna o conteúdo da resposta
        } else {
            throw new IOException("Failed to connect to SWAPI: Response code " + responseCode);
        }
    }

    // Método auxiliar para mapear JSON para CrewRecordFleet
    private CrewRecordFleet mapToCrewRecordFleet(JsonNode crewJson) {
        return new CrewRecordFleet(
                crewJson.get("name").asText(),
                crewJson.get("height").asText(),
                crewJson.get("mass").asText(),
                crewJson.get("gender").asText(),
                extractIdFromUrl(crewJson.get("url").asText()),  // Extrai o external_id a partir da URL
                true  // Supondo que o tripulante está disponível
        );
    }

    // Método auxiliar para mapear JSON para StarshipInternalRecordFleet
    private StarshipInternalRecordFleet mapToStarshipInternalRecordFleet(JsonNode starshipJson) {
        return new StarshipInternalRecordFleet(
                starshipJson.get("name").asText(),
                starshipJson.get("model").asText(),
                starshipJson.get("cost_in_credits").asText(),
                starshipJson.get("crew").asText(),
                starshipJson.get("cargo_capacity").asText(),
                starshipJson.get("max_atmosphering_speed").asText(),
                extractIdFromUrl(starshipJson.get("url").asText()),  // Extrai o external_id a partir da URL
                starshipJson.get("starship_class").asText(),
                true  // Supondo que a nave está disponível
        );
    }

    // Método auxiliar para extrair o ID da URL
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);  // Extrai o último valor da URL como o ID
    }
}

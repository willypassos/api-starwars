package br.com.swapi.service;

import br.com.swapi.model.CrewRecord;
import br.com.swapi.model.StarshipInternalRecord;
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

    // Método para buscar tripulações
    public List<CrewRecord> getCrew(int page, String name) throws IOException {
        String endpoint = "/people/?page=" + page; // Endpoint da SWAPI
        if (name != null && !name.isEmpty()) { // Verifica se o nome foi informado
            endpoint += "&name=" + name; // Acrescenta o nome na URL
        }

        String response = fetchData(endpoint); // Faz a requisição
        if (response == null || response.isEmpty()) { // Verifica se a resposta é vazia
            return new ArrayList<>(); // Caso seja, retorna uma lista vazia
        }

        JsonNode rootNode = objectMapper.readTree(response); // Cria um objeto JsonNode a partir da resposta
        JsonNode results = rootNode.get("results"); // Pega os resultados da resposta

        List<CrewRecord> crewMembers = new ArrayList<>(); // Cria uma lista vazia para armazenar os tripulantes
        for (JsonNode crewJson : results) { // Percorre todos os tripulantes na resposta
            CrewRecord crew = objectMapper.treeToValue(crewJson, CrewRecord.class); // Cria um objeto CrewRecord a partir do JSON
            crewMembers.add(crew); //
        }
        return crewMembers;
    }

    // Método para buscar naves
    public List<StarshipInternalRecord> getStarships(int page, String name) throws IOException {
        String endpoint = "/starships/?page=" + page;
        if (name != null && !name.isEmpty()) {
            endpoint += "&name=" + name;
        }

        String response = fetchData(endpoint);
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode results = rootNode.get("results");

        List<StarshipInternalRecord> starships = new ArrayList<>();
        for (JsonNode starshipJson : results) {
            StarshipInternalRecord starship = objectMapper.treeToValue(starshipJson, StarshipInternalRecord.class);
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
            return content.toString();
        } else {
            throw new IOException("Failed to connect to SWAPI: Response code " + responseCode);
        }
    }
}

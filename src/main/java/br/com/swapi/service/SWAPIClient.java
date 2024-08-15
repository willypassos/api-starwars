package br.com.swapi.service;

import br.com.swapi.model.FleetRecord;
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

    public List<FleetRecord> getFleet(int page, String name) throws IOException {
        String endpoint = "/fleet/?page=" + page;
        if (name != null && !name.isEmpty()) {
            endpoint += "&name=" + name;
        }

        String response = fetchData(endpoint);
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode results = rootNode.get("results");

        List<FleetRecord> fleets = new ArrayList<>();
        for (JsonNode fleetJson : results) {
            FleetRecord fleet = objectMapper.treeToValue(fleetJson, FleetRecord.class);
            fleets.add(fleet);
        }
        return fleets;
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

package br.com.swapi.client;

import br.com.swapi.model.StarshipInternalRecordFleet;
import br.com.swapi.service.IStarshipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class StarshipHandler implements HttpHandler {

    private final IStarshipService starshipService;
    private final ObjectMapper objectMapper;

    public StarshipHandler(IStarshipService starshipService) {
        this.starshipService = starshipService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && path.equals("/starship")) {
            handleGetStarshipByPage(exchange);
        } else {
            sendResponse(exchange, "Not Found", 404);
        }
    }

    // Handler para o método GET com paginação e filtro opcional por nome
    private void handleGetStarshipByPage(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        // Obtenha os parâmetros de consulta (page e name)
        int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));  // Parâmetro page
        String name = queryParams.get("name");  // Parâmetro name opcional

        try {
            // Use o método atualizado getStarshipByPage
            List<StarshipInternalRecordFleet> starships = starshipService.getStarshipByPage(page, name);
            String jsonResponse = objectMapper.writeValueAsString(starships);  // Serializa a resposta

            sendResponse(exchange, jsonResponse, 200);
        } catch (IOException e) {
            handleError(exchange, "Failed to fetch starships data: " + e.getMessage(), 500);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryPairs = new java.util.HashMap<>();
        if (query == null || query.isEmpty()) {
            return queryPairs;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return queryPairs;
    }

    private void sendResponse(HttpExchange exchange, String responseText, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseText.getBytes());
        }
    }

    private void handleError(HttpExchange exchange, String errorMessage, int statusCode) throws IOException {
        sendResponse(exchange, "{\"error\": \"" + errorMessage + "\"}", statusCode);
    }
}

package br.com.swapi.client;

import br.com.swapi.enums.HttpStatus;
import br.com.swapi.exception.GenericExceptionDTO;
import br.com.swapi.model.StarshipInternalRecordFleet;
import br.com.swapi.service.SWAPIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class StarshipHandler implements HttpHandler {

    private final SWAPIClient swapiClient; // Agora diretamente usando SWAPIClient
    private final ObjectMapper objectMapper;

    public StarshipHandler(SWAPIClient swapiClient) {
        this.swapiClient = swapiClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && path.equals("/starwars/v1/starship")) {
            handleGetStarshipByPage(exchange);
        } else {
            sendError(exchange, "Not Found", HttpStatus.NOT_FOUND.getCode());
        }
    }

    // Handler para o método GET com paginação e filtro opcional por nome
    private void handleGetStarshipByPage(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
            String name = queryParams.get("name");

            // Use o SWAPIClient diretamente para buscar os dados
            List<StarshipInternalRecordFleet> starships = swapiClient.getStarships(page, name);
            String jsonResponse = objectMapper.writeValueAsString(starships);
            sendResponse(exchange, jsonResponse, HttpStatus.OK.getCode());

        } catch (IOException e) {
            sendError(exchange, "pagina nao existe na api externa: " + e.getMessage(), HttpStatus.NOT_FOUND.getCode());
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
        exchange.sendResponseHeaders(statusCode, responseText.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseText.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, String errorMessage, int statusCode) throws IOException {
        GenericExceptionDTO errorResponse = new GenericExceptionDTO(String.valueOf(statusCode), errorMessage);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        sendResponse(exchange, jsonResponse, statusCode);
    }
}

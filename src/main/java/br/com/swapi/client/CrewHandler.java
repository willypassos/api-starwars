package br.com.swapi.client;

import br.com.swapi.exception.GenericExceptionDTO;
import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.service.SWAPIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CrewHandler implements HttpHandler {

    private final SWAPIClient swapiClient;
    private final ObjectMapper objectMapper;

    public CrewHandler(SWAPIClient swapiClient) {
        this.swapiClient = swapiClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && path.equals("/starwars/v1/crew")) {
            handleGetCrewByPage(exchange);
        } else {
            sendErrorResponse(exchange, "Not Found", "404", 404);
        }
    }

    private void handleGetCrewByPage(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
        String name = queryParams.get("name");

        try {
            List<CrewRecordFleet> crew = swapiClient.getCrew(page, name);
            String jsonResponse = objectMapper.writeValueAsString(crew);

            sendResponse(exchange, jsonResponse, 200);
        } catch (IOException e) {
            sendErrorResponse(exchange, "Failed to fetch crew data", e.getMessage(), 500);
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

    private void sendErrorResponse(HttpExchange exchange, String error, String code, int statusCode) throws IOException {
        GenericExceptionDTO exceptionDTO = new GenericExceptionDTO(code, error);
        String jsonResponse = objectMapper.writeValueAsString(exceptionDTO);
        sendResponse(exchange, jsonResponse, statusCode);
    }
}

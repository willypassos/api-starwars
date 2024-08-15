package br.com.swapi.client;

import br.com.swapi.model.StarshipInternalRecord;
import br.com.swapi.service.IStarshipService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class StarshipHandler implements HttpHandler {
    private final IStarshipService starshipService;

    public StarshipHandler(IStarshipService starshipService) {
        this.starshipService = starshipService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);

            int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
            String name = queryParams.get("name");  // Obtém o nome, se estiver presente

            List<StarshipInternalRecord> starshipRecords = starshipService.getStarshipByPage(page, name);

            String jsonResponse = convertToJson(starshipRecords);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
        } catch (IOException e) {
            handleError(exchange, "Failed to fetch starships data: " + e.getMessage(), 500);
        } catch (Exception e) {
            handleError(exchange, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

    private String convertToJson(List<StarshipInternalRecord> starshipRecords) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < starshipRecords.size(); i++) {
            StarshipInternalRecord record = starshipRecords.get(i);
            jsonBuilder.append("{")
                    .append("\"name\":\"").append(record.getName()).append("\",")
                    .append("\"model\":\"").append(record.getModel()).append("\",")
                    .append("\"price\":\"").append(record.getPrice()).append("\",")
                    .append("\"crew\":\"").append(record.getCrew()).append("\",")
                    .append("\"cargo\":\"").append(record.getCargo()).append("\",")
                    .append("\"speed\":\"").append(record.getSpeed()).append("\",")
                    .append("\"starship_class\":\"").append(record.getStarshipClass()).append("\",")
                    .append("\"external_id\":").append(record.getExternalId()).append(",")
                    .append("\"available\":").append(record.isAvailable())
                    .append("}");
            if (i < starshipRecords.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    // Método auxiliar para analisar a query string e extrair os parâmetros
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

    // Método para tratar erros e enviar respostas de erro ao cliente
    private void handleError(HttpExchange exchange, String errorMessage, int statusCode) {
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, errorMessage.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorMessage.getBytes());
            }
        } catch (IOException e) {
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }
}

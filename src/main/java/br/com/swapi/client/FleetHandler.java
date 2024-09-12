package br.com.swapi.client;

import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.FleetRecordRequestBody;
import br.com.swapi.service.IFleetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class FleetHandler implements HttpHandler {

    private final IFleetService fleetService;
    private final ObjectMapper objectMapper;

    public FleetHandler(IFleetService fleetService) {
        this.fleetService = fleetService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String responseText;

        try {
            if ("POST".equalsIgnoreCase(method) && path.equals("/starwars/v1/fleet")) {
                handlePostFleet(exchange);
            } else if ("PUT".equalsIgnoreCase(method) && path.startsWith("/starwars/v1/fleet/")) {
                String fleetName = path.split("/")[4];  // Corrigido para extrair o nome da frota
                handleUpdateFleet(exchange, fleetName);
            } else if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/starwars/v1/fleet/")) {
                String fleetName = path.split("/")[4];  // Corrigido para extrair o nome da frota
                handleDeleteFleet(exchange, fleetName);
            } else if ("GET".equalsIgnoreCase(method) && path.equals("/starwars/v1/fleet")) {
                handleGetFleet(exchange);
            } else {
                sendResponse(exchange, "Not Found", 404);
            }
        } catch (Exception e) {
            handleError(exchange, e.getMessage(), 500);
        }
    }

    private void handlePostFleet(HttpExchange exchange) throws IOException {
        FleetRecordRequestBody request = objectMapper.readValue(exchange.getRequestBody(), FleetRecordRequestBody.class);
        try {
            FleetRecord fleet = fleetService.postFleet(request);
            String jsonResponse = objectMapper.writeValueAsString(fleet);
            sendResponse(exchange, jsonResponse, 201);
        } catch (Exception e) {
            handleError(exchange, e.getMessage(), 500);
        }
    }

    private void handleUpdateFleet(HttpExchange exchange, String name) throws IOException {
        List<Integer> crewIds = objectMapper.readValue(exchange.getRequestBody(), List.class);
        try {
            FleetRecord fleet = fleetService.updateFleet(name, crewIds);
            String jsonResponse = objectMapper.writeValueAsString(fleet);
            sendResponse(exchange, jsonResponse, 200);
        } catch (Exception e) {
            handleError(exchange, e.getMessage(), 500);
        }
    }

    private void handleDeleteFleet(HttpExchange exchange, String name) throws IOException {
        fleetService.deleteFleet(name);
        sendResponse(exchange, "Fleet deleted successfully", 200);
    }

    private void handleGetFleet(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        Integer page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 1;  // Usa 1 como padrão
        String name = queryParams.get("name");

        List<FleetRecord> fleets = fleetService.getFleet(page, name);
        String jsonResponse = objectMapper.writeValueAsString(fleets);
        sendResponse(exchange, jsonResponse, 200);
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

    private void handleError(HttpExchange exchange, String errorMessage, int statusCode) throws IOException {
        String jsonResponse = "{\"error\": \"" + errorMessage + "\"}";
        sendResponse(exchange, jsonResponse, statusCode);
    }
}

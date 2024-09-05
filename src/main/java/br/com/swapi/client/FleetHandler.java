package br.com.swapi.client;

import br.com.swapi.model.FleetRecord;
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
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
        String name = queryParams.get("name");

        List<FleetRecord> fleetRecords = fleetService.getFleet(page, name);

        String jsonResponse = objectMapper.writeValueAsString(fleetRecords);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
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
}

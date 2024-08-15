package br.com.swapi.client;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.StarshipInternalRecordFleet;
import br.com.swapi.service.IFleetService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class FleetHandler implements HttpHandler {
    private final IFleetService fleetService;

    public FleetHandler(IFleetService fleetService) {
        this.fleetService = fleetService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);

            int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
            String name = queryParams.get("name");  // Obtém o nome, se estiver presente

            List<FleetRecord> fleetRecords = fleetService.getFleet(page, name);

            String jsonResponse = convertToJson(fleetRecords);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
        } catch (IOException e) {
            handleError(exchange, "Failed to fetch fleet data: " + e.getMessage(), 500);
        } catch (Exception e) {
            handleError(exchange, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

    private String convertToJson(List<FleetRecord> fleetRecords) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < fleetRecords.size(); i++) {
            FleetRecord record = fleetRecords.get(i);
            jsonBuilder.append("{")
                    .append("\"name\":\"").append(record.getName()).append("\",")
                    .append("\"crew\":").append(convertCrewToJson(record.getCrew())).append(",")
                    .append("\"starship\":").append(convertStarshipToJson(record.getStarship()))
                    .append("}");
            if (i < fleetRecords.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    private String convertCrewToJson(List<CrewRecordFleet> crewList) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < crewList.size(); i++) {
            CrewRecordFleet crew = crewList.get(i);
            jsonBuilder.append("{")
                    .append("\"name\":\"").append(crew.getName()).append("\",")
                    .append("\"height\":\"").append(crew.getHeight()).append("\",")
                    .append("\"mass\":\"").append(crew.getMass()).append("\",")
                    .append("\"gender\":\"").append(crew.getGender()).append("\",")
                    .append("\"external_id\":").append(crew.getExternalId())
                    .append("}");
            if (i < crewList.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    private String convertStarshipToJson(StarshipInternalRecordFleet starship) {
        return "{"
                + "\"name\":\"" + starship.getName() + "\","
                + "\"model\":\"" + starship.getModel() + "\","
                + "\"price\":\"" + starship.getPrice() + "\","
                + "\"crew\":\"" + starship.getCrew() + "\","
                + "\"cargo\":\"" + starship.getCargo() + "\","
                + "\"speed\":\"" + starship.getSpeed() + "\","
                + "\"starship_class\":\"" + starship.getStarshipClass() + "\","
                + "\"external_id\":" + starship.getExternalId()
                + "}";
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

package br.com.swapi.client;

import br.com.swapi.model.CrewRecord;
import br.com.swapi.service.CrewService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CrewHandler implements HttpHandler {
    private final CrewService crewService;

    public CrewHandler(CrewService crewService) {
        this.crewService = crewService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);

            int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
            String name = queryParams.get("name");

            List<CrewRecord> crewRecords = crewService.getCrewByPage(page, name);
            String jsonResponse = convertToJson(crewRecords);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (Exception e) {
            String errorResponse = "Internal Server Error: " + e.getMessage();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(errorResponse.getBytes());
            os.close();
        }
    }

    private String convertToJson(List<CrewRecord> crewRecords) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < crewRecords.size(); i++) {
            CrewRecord record = crewRecords.get(i);
            jsonBuilder.append("{")
                    .append("\"name\":\"").append(record.getName()).append("\",")
                    .append("\"height\":\"").append(record.getHeight()).append("\",")
                    .append("\"mass\":\"").append(record.getMass()).append("\",")
                    .append("\"gender\":\"").append(record.getGender()).append("\",")
                    .append("\"available\":").append(record.isAvailable()).append(",")
                    .append("\"external_id\":").append(record.getExternalId())
                    .append("}");
            if (i < crewRecords.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
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

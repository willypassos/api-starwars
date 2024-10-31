package br.com.swapi.client;

import br.com.swapi.enums.HttpStatus;
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
        String method = exchange.getRequestMethod(); // Pega o método HTTP da requisição
        String path = exchange.getRequestURI().getPath(); // Pega o caminho da requisição

        try {
            if ("POST".equalsIgnoreCase(method) && path.equals("/starwars/v1/fleet")) { // Verifica se o método é POST e o caminho é /starwars/v1/fleet
                handlePostFleet(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.equals("/starwars/v1/fleet")) { //
                handleGetFleet(exchange);
            } else if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/starwars/v1/fleet/")) {
                handleDeleteFleet(exchange);
            } else if ("PUT".equalsIgnoreCase(method) && path.startsWith("/starwars/v1/fleet/")) {
                handleUpdateFleet(exchange);
            } else {
                sendResponse(exchange, "Not Found", HttpStatus.NOT_FOUND.getCode()); // Envia uma resposta de erro para o cliente
            }
        } catch (Exception e) {
            handleError(exchange, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getCode()); // Envia uma resposta de erro para o cliente
        }
    }

    public void handlePostFleet(HttpExchange exchange) throws IOException {
        FleetRecordRequestBody request = objectMapper.readValue(exchange.getRequestBody(), FleetRecordRequestBody.class); // Le o corpo da requisição como FleetRecordRequestBody
        try {
            FleetRecord fleet = fleetService.postFleet(request); // Chama o método postFleet da IFleetService
            String jsonResponse = objectMapper.writeValueAsString(fleet); // Converte o objeto para JSON
            sendResponse(exchange, jsonResponse, HttpStatus.CREATED.getCode()); // Envia a resposta para o cliente
        } catch (Exception e) {
            handleError(exchange, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getCode()); // Envia uma resposta de erro para o cliente
        }
    }

    public void handleGetFleet(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery()); // Pega os parâmetros da requisição
        Integer page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 1; // Use o SWAPIClient diretamente para buscar os dados
        String name = queryParams.get("name"); // Use o SWAPIClient diretamente para buscar os dados

        try {
            List<FleetRecord> fleets = fleetService.getFleet(page, name); // chama o método getFleet da IFleetService
            String jsonResponse = objectMapper.writeValueAsString(fleets); // Converte o objeto para JSON
            sendResponse(exchange, jsonResponse, HttpStatus.OK.getCode()); // Envia a resposta para o cliente
        } catch (Exception e) {
            handleError(exchange, "Erro ao buscar frotas: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getCode()); // Envia uma resposta de erro para o cliente
        }
    }

    public void handleDeleteFleet(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        String fleetName = pathParts[pathParts.length - 1]; // Obtém o último segmento da URL como o nome

        try {
            fleetService.deleteFleet(fleetName);
            sendResponse(exchange, "Frota deletada com sucesso: ", HttpStatus.OK.getCode()); // Envia a resposta para o cliente
        } catch (Exception e) {
            handleError(exchange, "Erro ao deletar frota: " + e.getMessage(), HttpStatus.NOT_FOUND.getCode()); // Envia uma resposta de erro para o cliente
        }
    }

    private void handleUpdateFleet(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        String fleetName = pathParts[pathParts.length - 1];

        List<Integer> crewIds = objectMapper.readValue(exchange.getRequestBody(), List.class);
        try {
            FleetRecord updatedFleet = fleetService.updateFleet(fleetName, crewIds);
            String jsonResponse = objectMapper.writeValueAsString(updatedFleet);
            sendResponse(exchange, jsonResponse, HttpStatus.CREATED.getCode());
        } catch (Exception e) {
            handleError(exchange, "Erro ao atualizar a frota: " + e.getMessage(), HttpStatus.NOT_FOUND.getCode());;
        }
    }

    public Map<String, String> parseQueryParams(String query) {
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
        String jsonResponse = "{\"error\": \"" + errorMessage + "\"}";
        sendResponse(exchange, jsonResponse, statusCode);
    }
}

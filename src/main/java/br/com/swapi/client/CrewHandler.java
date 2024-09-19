package br.com.swapi.client;

import br.com.swapi.enums.HttpStatus;
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

        if ("GET".equalsIgnoreCase(method) && path.equals("/starwars/v1/crew")) { //
            handleGetCrewByPage(exchange);
        } else {
            sendErrorResponse(exchange, "Not Found", "404", HttpStatus.NOT_FOUND.getCode());
        }
    }

    private void handleGetCrewByPage(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        int page = Integer.parseInt(queryParams.getOrDefault("page", "1")); // Use o SWAPIClient diretamente para buscar os dados
        String name = queryParams.get("name"); // Use o SWAPIClient diretamente para buscar os dados

        try {
            List<CrewRecordFleet> crew = swapiClient.getCrew(page, name);// Use o SWAPIClient diretamente para buscar os dados
            String jsonResponse = objectMapper.writeValueAsString(crew); // Use o ObjectMapper para transformar os dados em JSON

            sendResponse(exchange, jsonResponse, HttpStatus.OK.getCode()); // Envia a resposta para o cliente
        } catch (IOException e) { // Trata erros de IO
            sendErrorResponse(exchange, "Failed to fetch crew data", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getCode()); // Envia uma resposta de erro para o cliente
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryPairs = new java.util.HashMap<>(); // Cria um HashMap vazio
        if (query == null || query.isEmpty()) { // Verifica se a query esta vazia
            return queryPairs; // Retorna um HashMap vazio
        }
        String[] pairs = query.split("&"); // Divide a query em pares
        for (String pair : pairs) { // Itera sobre os pares
            int idx = pair.indexOf("="); // Encontra o indice do caractere "="
            queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1)); // Adiciona o par ao HashMap
        }
        return queryPairs; // Retorna o HashMap com os pares
    }

    private void sendResponse(HttpExchange exchange, String responseText, int statusCode) throws IOException { // Envia a resposta para o cliente
        exchange.getResponseHeaders().set("Content-Type", "application/json"); // Define o tipo de conteúdo como JSON
        exchange.sendResponseHeaders(statusCode, responseText.length()); // Envia o status e o tamanho da resposta
        try (OutputStream os = exchange.getResponseBody()) { // Cria um fluxo de saida
            os.write(responseText.getBytes()); // Escreve a resposta no fluxo de saida
        }
    }

    private void sendErrorResponse(HttpExchange exchange, String error, String code, int statusCode) throws IOException {
        GenericExceptionDTO exceptionDTO = new GenericExceptionDTO(code, error); // Cria um objeto GenericExceptionDTO com o erro e o status code
        String jsonResponse = objectMapper.writeValueAsString(exceptionDTO); // Transforma o objeto em JSON
        sendResponse(exchange, jsonResponse, statusCode); // Envia a resposta para o cliente
    }
}

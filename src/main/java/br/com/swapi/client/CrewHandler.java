package br.com.swapi.client;

import br.com.swapi.model.CrewRecord;
import br.com.swapi.service.ICrewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CrewHandler implements HttpHandler {
    private final ICrewService crewService;
    private final ObjectMapper objectMapper;

    public CrewHandler(ICrewService crewService) {
        this.crewService = crewService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();  // Obtenha a query string da requisição
            Map<String, String> queryParams = parseQueryParams(query); // Analise a query string

            int page = Integer.parseInt(queryParams.getOrDefault("page", "1")); // Obtenha o parâmetro 'page'
            String name = queryParams.get("name"); // Obtenha o parâmetro 'name'

            List<CrewRecord> crewRecords = crewService.getCrewByPage(page, name); // Chame o método getCrewByPage

            String jsonResponse = objectMapper.writeValueAsString(crewRecords); // Crie uma string JSON a partir da lista de tripulantes

            exchange.getResponseHeaders().set("Content-Type", "application/json"); // Defina o tipo de conteúdo
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) { // Usando o fluxo de escrita para enviar o conteúdo
                os.write(jsonResponse.getBytes()); // Enviando o conteúdo
            }
        } catch (IOException e) {
            handleError(exchange, "Failed to fetch crew data: " + e.getMessage(), 500);
        } catch (Exception e) {
            handleError(exchange, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryPairs = new java.util.HashMap<>(); // Criando um HashMap para armazenar os parâmetros
        if (query == null || query.isEmpty()) { // Se a query estiver vazia
            return queryPairs; //
        }
        String[] pairs = query.split("&"); // Divide a query string em parâmetros
        for (String pair : pairs) { // Para cada parâmetro
            int idx = pair.indexOf("=");// Encontra o índice do caractere "="
            queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));// Adiciona o parâmetro ao HashMap
        }
        return queryPairs; // Retorna o HashMap
    }

    private void handleError(HttpExchange exchange, String errorMessage, int statusCode) { // Método para tratar erros e enviar respostas de erro ao cliente
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/json"); // Definindo o tipo de conteúdo
            exchange.sendResponseHeaders(statusCode, errorMessage.getBytes().length); // Enviando o conteúdo
            try (OutputStream os = exchange.getResponseBody()) { // Usando o fluxo de escrita para enviar o conteúdo
                os.write(errorMessage.getBytes()); // Enviando o conteúdo
            }
        } catch (IOException e) {
            System.err.println("Failed to send error response: " + e.getMessage()); // Exibir uma mensagem de erro
        }
    }
}

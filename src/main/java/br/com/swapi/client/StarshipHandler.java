package br.com.swapi.client;

import br.com.swapi.model.StarshipInternalRecord;
import br.com.swapi.service.IStarshipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class StarshipHandler implements HttpHandler {
    private final IStarshipService starshipService;
    private final ObjectMapper objectMapper;

    public StarshipHandler(IStarshipService starshipService) {
        this.starshipService = starshipService;
        this.objectMapper = new ObjectMapper(); // Instanciando o ObjectMapper para serialização JSON
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery(); // Obtém a query string
            Map<String, String> queryParams = parseQueryParams(query); // Usando o método auxiliar

            int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
            String name = queryParams.get("name");  // Obtém o nome, se estiver presente

            List<StarshipInternalRecord> starshipRecords = starshipService.getStarshipByPage(page, name); // Usando o método de busca de tripulantes

            String jsonResponse = objectMapper.writeValueAsString(starshipRecords); // Usando Jackson para converter a lista em JSON

            exchange.getResponseHeaders().set("Content-Type", "application/json"); // Definindo o tipo de conteúdo
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length); // Enviando o conteúdo
            try (OutputStream os = exchange.getResponseBody()) { // Usando o fluxo de escrita para enviar o conteúdo
                os.write(jsonResponse.getBytes()); // Enviando o conteúdo
            }
        } catch (IOException e) {
            handleError(exchange, "Failed to fetch starships data: " + e.getMessage(), 500);
        } catch (Exception e) {
            handleError(exchange, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

    // Método auxiliar para analisar a query string e extrair os parâmetros
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryPairs = new java.util.HashMap<>(); // Criando um HashMap para armazenar os parâmetros
        if (query == null || query.isEmpty()) { // Se a query estiver vazia
            return queryPairs; // Retorna um HashMap vazio
        }
        String[] pairs = query.split("&"); // Divide a query string em parâmetros
        for (String pair : pairs) { // Para cada parâmetro
            int idx = pair.indexOf("="); // Encontra o índice do caractere "="
            queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1)); // Adiciona o parâmetro ao HashMap
        }
        return queryPairs; // Retorna o HashMap
    }

    // Método para tratar erros e enviar respostas de erro ao cliente
    private void handleError(HttpExchange exchange, String errorMessage, int statusCode) {
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/json"); // Definindo o tipo de conteúdo
            exchange.sendResponseHeaders(statusCode, errorMessage.getBytes().length); // Enviando o conteúdo
            try (OutputStream os = exchange.getResponseBody()) { // Usando o fluxo de escrita para enviar o conteúdo
                os.write(errorMessage.getBytes()); // Enviando o conteúdo
            }
        } catch (IOException e) {
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }
}

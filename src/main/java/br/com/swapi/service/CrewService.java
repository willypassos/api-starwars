package br.com.swapi.service;

import br.com.swapi.model.CrewRecord;
import br.com.swapi.repository.CrewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrewService implements ICrewService {

    private final SWAPIClient swapiClient;
    private final CrewRepository crewRepository;
    private final ObjectMapper objectMapper;

    public CrewService(SWAPIClient swapiClient, CrewRepository crewRepository) {
        this.swapiClient = swapiClient;
        this.crewRepository = crewRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<CrewRecord> getCrewByPage(int page, String name) throws IOException {
        List<CrewRecord> crewRecords = new ArrayList<>(); // Lista para armazenar os tripulantes

        if (name != null && !name.isEmpty()) { // Verifica se o nome foi informado
            CrewRecord crew = crewRepository.findCrewByName(name); //  Busca pelo tripulante
            if (crew != null) { // Se o tripulante foi encontrado, adiciona na lista
                crewRecords.add(crew); // Adiciona o tripulante
                return crewRecords; // Retorna a lista
            }
        }

        // Faz a requisição para a API externa da SWAPI
        String endpoint = "/people/?page=" + page; // Endpoint da SWAPI para buscar tripulantes
        String response = swapiClient.fetchData(endpoint); // Resposta da requisição
        crewRecords = parseCrew(response); // Preenche a lista com os tripulantes

        // Salva os tripulantes no banco de dados se não estiverem presentes
        for (CrewRecord crew : crewRecords) {
            if (crewRepository.findCrewByName(crew.getName()) == null) { // Verifica se o tripulante ainda não foi salvo
                crewRepository.saveCrew(crew); // Salva o tripulante
            }
        }

        return crewRecords;// Retorna a lista
    }

    private List<CrewRecord> parseCrew(String json) throws IOException { // Método para criar uma lista de CrewRecord a partir de uma String JSON
        JsonNode rootNode = objectMapper.readTree(json);// Obtem o rootNode da resposta da API
        JsonNode results = rootNode.get("results");// Obtem os resultados da resposta da API
        List<CrewRecord> crewList = new ArrayList<>(); // Lista para armazenar os tripulantes

        if (results.isArray()) { // Verifica se os resultados são um array
            for (JsonNode crewJson : results) { // Percorre o array de resultados
                int externalId = extractIdFromUrl(crewJson.get("url").asText()); // Obtem o ID do tripulante
                if (externalId < 1) { // Verifica se o ID é inválido
                    continue; // Pula registros inválidos
                }

                String name = crewJson.get("name").asText(); // Obtem o nome do tripulante
                String height = crewJson.has("height") ? crewJson.get("height").asText() : "";
                String mass = crewJson.has("mass") ? crewJson.get("mass").asText() : "";
                String gender = crewJson.has("gender") ? crewJson.get("gender").asText() : "";

                boolean available = checkAvailability(externalId); // Verifica a disponibilidade do tripulante

                CrewRecord crew = new CrewRecord(name, height, mass, gender, available, externalId)
                        ;
                crewList.add(crew);
            }
        }

        return crewList;
    }

    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/"); // Divide a URL em partes
        return Integer.parseInt(parts[parts.length - 1]); // Retorna o ID
    }

    // Verifica se o tripulante já está associado a uma tripulação (crew) existente
    private boolean checkAvailability(int externalId) {
        CrewRecord crew = crewRepository.findCrewByExternalId(externalId); // Busca pelo tripulante
        return crew == null; // Se o tripulante não foi encontrado, está disponível
    }
}

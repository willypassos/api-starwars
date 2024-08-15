package br.com.swapi.service;

import br.com.swapi.model.CrewRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrewService implements ICrewService {

    private final SWAPIClient swapiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Simulação de tripulações já cadastradas
    private final Map<String, List<Integer>> existingFleets = new HashMap<>();

    public CrewService(SWAPIClient swapiClient) {
        this.swapiClient = swapiClient;
        initializeMockFleets(); // Inicializa as tripulações para simulação
    }

    @Override
    public List<CrewRecord> getCrewByPage(int page, String name) throws IOException {
        String endpoint = "/people/?page=" + page;
        String response = swapiClient.fetchData(endpoint);
        return filterCrewByName(parseCrew(response), name);
    }

    private List<CrewRecord> parseCrew(String json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json); // Lendo o JSON
        JsonNode results = rootNode.get("results"); // Obtendo os resultados da lógica de negócio
        List<CrewRecord> crewList = new ArrayList<>(); // Criando uma lista vazia para armazenar os dados

        if (results.isArray()) {
            for (JsonNode crewJson : results) { // Iterando sobre os resultados
                int externalId = extractIdFromUrl(crewJson.get("url").asText()); // Obtendo o ID do personagem

                String name = crewJson.get("name").asText(); // Nome do personagem
                String height = crewJson.get("height").asText(); // Altura
                String mass = crewJson.get("mass").asText(); // Peso
                String gender = crewJson.get("gender").asText(); // Gênero

                boolean available = checkAvailability(externalId); // Verificando se o membro da tripulação está disponível

                CrewRecord crew = new CrewRecord(name, height, mass, gender, available, externalId);
                crewList.add(crew);
            }
        }

        return crewList;
    }

    private List<CrewRecord> filterCrewByName(List<CrewRecord> crewList, String name) {
        if (name == null || name.isEmpty()) {
            return crewList; // Se o nome não for fornecido, retorne a lista completa
        }
        List<CrewRecord> filteredCrew = new ArrayList<>();
        for (CrewRecord crew : crewList) {
            if (crew.getName().equalsIgnoreCase(name)) {
                filteredCrew.add(crew);
            }
        }
        return filteredCrew;
    }

    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    // Método para verificar se o membro da tripulação está disponível
    private boolean checkAvailability(int externalId) {
        for (List<Integer> crewIds : existingFleets.values()) { // Iterando sobre as tripulações existentes
            if (crewIds.contains(externalId)) { // Verificando se o membro está em uma tripulação
                return false; // Já está em uma tripulação, não está disponível
            }
        }
        return true; // Disponível
    }

    // Método para adicionar membros a uma tripulação (simulação)
    public void addCrewMemberToFleet(String fleetName, int externalId) {
        if (checkAvailability(externalId)) { // Verificando se o membro está disponível
            existingFleets.computeIfAbsent(fleetName, k -> new ArrayList<>()).add(externalId); // Adicionando o membro à tripulação
            System.out.println("Membro adicionado à tripulação " + fleetName); // Mensagem de sucesso
        } else {
            System.out.println("Membro já está em uma tripulação, não pode ser adicionado.");// Mensagem de erro
        }
    }

    // Inicializa uma simulação de tripulações já existentes
    private void initializeMockFleets() {
        List<Integer> crew1 = new ArrayList<>();
        crew1.add(1); // ID 1 já está em uma tripulação
        crew1.add(2); // ID 2 já está em uma tripulação
        existingFleets.put("Fleet1", crew1);
    }
}

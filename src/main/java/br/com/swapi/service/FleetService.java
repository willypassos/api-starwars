package br.com.swapi.service;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.StarshipInternalRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FleetService implements IFleetService {

    private final SWAPIClient swapiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FleetService(SWAPIClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    @Override
    public List<FleetRecord> getFleet(int page, String name) throws IOException {
        String endpoint = "/starships/?page=" + page;
        String response = swapiClient.fetchData(endpoint); // Fazendo a requisição HTTP
        return filterFleetByName(parseFleet(response), name); // Filtrando a frota
    }

    @Override
    public FleetRecord createFleet(String name, List<CrewRecordFleet> crewList, StarshipInternalRecordFleet starship) {
        validateCrewSize(crewList); // Validando o tamanho da tripulação
        validateStarshipPresence(starship); // Validando  se possui nave
        return new FleetRecord(name, crewList, starship);
    }

    private List<FleetRecord> parseFleet(String json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json); // Lendo o JSON
        JsonNode results = rootNode.get("results"); // Obtendo os resultados da lógica de negócio
        List<FleetRecord> fleetList = new ArrayList<>(); // Criando uma lista vazia para armazenar os dados

        if (results.isArray()) {
            for (JsonNode fleetJson : results) { // iterando sobre os resultados
                int externalId = extractIdFromUrl(fleetJson.get("url").asText()); // obtendo o ID da nave

                // podem ser preenchidos em outra parte da lógica de negócio
                List<CrewRecordFleet> crewList = new ArrayList<>(); // Criando uma lista vazia para armazenar os dados

                // Criando o StarshipInternalRecordFleet baseado nos dados recebidos
                StarshipInternalRecordFleet starship = new StarshipInternalRecordFleet( // Criando o objeto StarshipInternalRecordFleet
                        fleetJson.get("name").asText(), // Nome da nave
                        fleetJson.has("model") ? fleetJson.get("model").asText() : "", // Model da nave
                        fleetJson.has("cost_in_credits") ? fleetJson.get("cost_in_credits").asText() : "", // Cost_in_credits da nave
                        fleetJson.has("crew") ? fleetJson.get("crew").asText() : "", // Crew(tripulação) da nave
                        fleetJson.has("cargo_capacity") ? fleetJson.get("cargo_capacity").asText() : "",// Cargo_capacity da nave
                        fleetJson.has("max_atmosphering_speed") ? fleetJson.get("max_atmosphering_speed").asText() : "", // Max_atmosphering_speed da nave
                        externalId, // ID da nave
                        fleetJson.has("starship_class") ? fleetJson.get("starship_class").asText() : "" // Starship_class da nave
                );

                // Adicionando o FleetRecord à lista com a nave e a tripulação associada
                FleetRecord fleet = new FleetRecord(fleetJson.get("name").asText(), crewList, starship); // Criando o objeto FleetRecord
                fleetList.add(fleet); // Adicionando o objeto FleetRecord à lista
            }
        }

        return fleetList; // Retornando a lista de objetos FleetRecord
    }

    private List<FleetRecord> filterFleetByName(List<FleetRecord> fleetList, String name) {
        if (name == null || name.isEmpty()) { // Verificando se o nome foi fornecido
            return fleetList; // Se o nome não for fornecido, retorne a lista completa
        }
        List<FleetRecord> filteredFleet = new ArrayList<>(); // Criando uma lista vazia para armazenar os dados
        for (FleetRecord fleet : fleetList) { // iterando  uma lista de fleet(frota)
            if (fleet.getName().equalsIgnoreCase(name)) { // comparando o nome da frota com o nome fornecido
                filteredFleet.add(fleet); // adicionando a frota a lista filtrada
            }
        }
        return filteredFleet; // retornando a lista filtrada
    }

    private int extractIdFromUrl(String url) { // Extrai o ID da nave a partir da URL fornecida
        String[] parts = url.split("/"); // dividindo a URL em partes
        return Integer.parseInt(parts[parts.length - 1]); // retornando o ID da nave
    }

    private void validateCrewSize(List<CrewRecordFleet> crewList) { // Validando o tamanho da tripulação
        if (crewList == null || crewList.isEmpty()) { // Verificando se a tripulação foi fornecida
            throw new IllegalArgumentException("A frota deve conter pelo menos um tripulante."); //lancando uma exceção
        }
        if (crewList.size() > 5) { //
            throw new IllegalArgumentException("A frota não pode conter mais de 5 tripulantes."); //lancando uma exceção
        }
    }

    private void validateStarshipPresence(StarshipInternalRecordFleet starship) { // Validando  se possui nave
        if (starship == null) { // Verificando se a nave foi fornecida
            throw new IllegalArgumentException("A frota deve conter uma nave."); //lancando uma exceção
        }
    }
}

package br.com.swapi.service;

import br.com.swapi.model.CrewRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrewService implements ICrewService {

    private final SWAPIClient swapiClient = new SWAPIClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Método conforme o operationId do Swagger: getCrewByPage
    @Override
    public List<CrewRecord> getCrewByPage(int page, String name) throws IOException {
        // Monta o endpoint conforme a paginação e o nome opcional
        String endpoint = "/people/?page=" + page;
        if (name != null && !name.isEmpty()) {
            endpoint += "&search=" + name;
        }

        // Faz a requisição para SWAPI
        String response = swapiClient.fetchData(endpoint);
        return mapCrewFromJson(response);  // Mapeia os dados retornados para objetos CrewRecord
    }

    // Método auxiliar para mapear a tripulação a partir do JSON retornado pela SWAPI
    private List<CrewRecord> mapCrewFromJson(String json) throws IOException {
        List<CrewRecord> crewList = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode results = rootNode.get("results");

        if (results.isArray()) {
            for (JsonNode crewJson : results) {
                CrewRecord crewMember = mapCrewFromJson(crewJson);
                crewList.add(crewMember);
            }
        }

        return crewList;
    }

    // Método auxiliar para mapear um tripulante individual
    private CrewRecord mapCrewFromJson(JsonNode crewJson) {
        return new CrewRecord(
                crewJson.get("name").asText(),
                crewJson.get("height").asText(),
                crewJson.get("mass").asText(),
                crewJson.get("gender").asText(),
                true,  // Supondo que o tripulante está disponível
                extractIdFromUrl(crewJson.get("url").asText())  // O external_id deve vir por último
        );
    }


    // Extrai o ID da URL do tripulante
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}

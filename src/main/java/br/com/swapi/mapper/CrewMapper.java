package br.com.swapi.mapper;

import br.com.swapi.model.CrewRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class CrewMapper {

    // Mapeia os dados do JSON para um objeto CrewRecordFleet, incluindo o campo available.
    public CrewRecordFleet mapToCrew(JsonNode crewJson) {
        return new CrewRecordFleet(
                crewJson.get("name").asText(""),  // name
                crewJson.get("height").asText(""),  // height
                crewJson.get("mass").asText(""),  // mass
                crewJson.get("gender").asText(""),  // gender
                extractIdFromUrl(crewJson.get("url").asText()),  // external_id
                checkAvailability(extractIdFromUrl(crewJson.get("url").asText()))  // Verifica a disponibilidade
        );
    }

    // Mapeia uma lista de personagens de tripulação a partir do JSON.
    public List<CrewRecordFleet> mapCrewFromJson(JsonNode crewJson) {
        List<CrewRecordFleet> crewList = new ArrayList<>();
        JsonNode crewArray = crewJson.get("crew");

        if (crewArray != null && crewArray.isArray()) {
            for (JsonNode member : crewArray) {
                CrewRecordFleet crew = new CrewRecordFleet(
                        member.get("name").asText(""),
                        member.get("height").asText(""),
                        member.get("mass").asText(""),
                        member.get("gender").asText(""),
                        extractIdFromUrl(member.get("url").asText()),  // external_id
                        checkAvailability(extractIdFromUrl(member.get("url").asText()))  // Verifica a disponibilidade
                );
                crewList.add(crew);
            }
        }

        return crewList;
    }

    // Verifica a disponibilidade de um personagem na base de tripulações.
    private boolean checkAvailability(int externalId) {
        // Aqui você pode implementar a lógica para verificar na base de dados ou em um serviço
        // se o tripulante com o externalId já está em uso.
        // Exemplo: se o personagem não estiver alocado em nenhuma frota, retorna true (disponível).
        return true;  // Placeholder para a lógica de verificação.
    }

    // Extrai o ID do personagem a partir da URL fornecida.
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}

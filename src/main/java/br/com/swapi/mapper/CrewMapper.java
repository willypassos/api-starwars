package br.com.swapi.mapper;

import br.com.swapi.model.CrewRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class CrewMapper {

    /**
     * Mapeia os dados do JSON para um objeto CrewRecordFleet.
     *
     * @param crewJson O JsonNode que representa os dados do personagem.
     * @return Um objeto CrewRecordFleet com os dados mapeados.
     */
    public CrewRecordFleet mapToCrew(JsonNode crewJson) {
        return new CrewRecordFleet(
                crewJson.get("name").asText(""),
                crewJson.get("height").asText(""),
                crewJson.get("mass").asText(""),
                crewJson.get("gender").asText(""),
                extractIdFromUrl(crewJson.get("url").asText()) // external_id mapeado a partir da URL
        );
    }

    /**
     * Mapeia uma lista de personagens de tripulação a partir do JSON.
     *
     * @param crewJson O JsonNode que representa a lista de dados da tripulação.
     * @return Uma lista de objetos CrewRecordFleet.
     */
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
                        extractIdFromUrl(member.get("url").asText()) // external_id
                );
                crewList.add(crew);
            }
        }

        return crewList;
    }

    /**
     * Extrai o ID do personagem a partir da URL fornecida.
     *
     * @param url A URL do personagem.
     * @return O identificador único extraído da URL.
     */
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}

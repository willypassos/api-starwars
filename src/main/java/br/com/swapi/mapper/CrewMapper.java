package br.com.swapi.mapper;

import br.com.swapi.model.CrewRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;

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

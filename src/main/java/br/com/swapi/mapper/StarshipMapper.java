package br.com.swapi.mapper;

import br.com.swapi.model.StarshipInternalRecord;
import com.fasterxml.jackson.databind.JsonNode;

public class StarshipMapper {

    /**
     * Mapeia os dados do JSON para um objeto StarshipInternalRecord.
     *
     * @param starshipJson O JsonNode que representa os dados da nave espacial.
     * @return Um objeto StarshipInternalRecord com os dados mapeados.
     */
    public StarshipInternalRecord mapToStarship(JsonNode starshipJson) {
        return new StarshipInternalRecord(
                starshipJson.get("name").asText(""),
                starshipJson.get("model").asText(""),
                starshipJson.get("cost_in_credits").asText(""),
                starshipJson.get("crew").asText(""),
                starshipJson.get("cargo_capacity").asText(""),
                starshipJson.get("max_atmosphering_speed").asText(""),
                extractIdFromUrl(starshipJson.get("url").asText()), // external_id mapeado a partir da URL
                starshipJson.get("starship_class").asText(""),
                true // Disponibilidade será verificada na lógica da aplicação
        );
    }

    /**
     * Extrai o ID da nave espacial a partir da URL fornecida.
     *
     * @param url A URL da nave espacial.
     * @return O identificador único extraído da URL.
     */
    private int extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}

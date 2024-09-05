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
    public StarshipInternalRecord mapStarshipFromJson(JsonNode starshipJson, boolean available) {
        return new StarshipInternalRecord(
                starshipJson.has("name") ? starshipJson.get("name").asText("") : "",  // Nome da nave espacial
                starshipJson.has("model") ? starshipJson.get("model").asText("") : "",  // Modelo da nave
                starshipJson.has("cost_in_credits") ? starshipJson.get("cost_in_credits").asText("") : "",  // Custo em créditos
                starshipJson.has("crew") ? starshipJson.get("crew").asText("") : "",  // Tamanho da tripulação
                starshipJson.has("cargo_capacity") ? starshipJson.get("cargo_capacity").asText("") : "",  // Capacidade de carga
                starshipJson.has("max_atmosphering_speed") ? starshipJson.get("max_atmosphering_speed").asText("") : "",  // Velocidade máxima
                extractIdFromUrl(starshipJson.has("url") ? starshipJson.get("url").asText("") : ""),  // ID extraído da URL
                starshipJson.has("starship_class") ? starshipJson.get("starship_class").asText("") : "",  // Classe da nave
                available  // Disponibilidade passada dinamicamente
        );
    }

    /**
     * Extrai o ID da nave espacial a partir da URL fornecida.
     *
     * @param url A URL da nave espacial.
     * @return O identificador único extraído da URL.
     */
    private int extractIdFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Tratamento de erro caso o ID não possa ser extraído corretamente
            return -1;  // Retorna um valor inválido ou lança uma exceção personalizada
        }
    }
}

package br.com.swapi.mapper;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.StarshipInternalRecordFleet;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class FleetMapper {

    private final CrewMapper crewMapper = new CrewMapper();
    private final StarshipMapper starshipMapper = new StarshipMapper();

    /**
     * Mapeia os dados da requisição JSON para um objeto FleetRecord.
     *
     * @param fleetJson O JsonNode que representa os dados da frota.
     * @param crewList A lista de objetos CrewRecordFleet representando a tripulação.
     * @param starship O objeto StarshipInternalRecordFleet representando a nave.
     * @return Um objeto FleetRecord com os dados mapeados.
     */
    public FleetRecord mapToFleet(JsonNode fleetJson, List<CrewRecordFleet> crewList, StarshipInternalRecordFleet starship) {
        return new FleetRecord(
                fleetJson.get("name").asText(""),
                crewList,
                starship
        );
    }

    /**
     * Mapeia os dados da frota a partir de IDs de tripulação e nave.
     *
     * @param name O nome da frota.
//     * @param crewIds A lista de IDs dos membros da tripulação.
//     * @param starshipId O ID da nave.
     * @return Um objeto FleetRecord com os dados mapeados.
     */
    public FleetRecord mapToFleet(String name, List<CrewRecordFleet> crewList, StarshipInternalRecordFleet starship) {
        return new FleetRecord(
                name,
                crewList,
                starship

        );
    }
}

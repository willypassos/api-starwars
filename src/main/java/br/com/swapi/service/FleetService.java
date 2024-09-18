package br.com.swapi.service;

import br.com.swapi.model.*;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.mapper.FleetMapper;

import java.util.List;
import java.util.stream.Collectors;

public class FleetService implements IFleetService {

    private final FleetRepository fleetRepository = new FleetRepository();
    private final SWAPIClient swapiClient = new SWAPIClient(); // Consumindo diretamente da SWAPI
    private final FleetMapper fleetMapper = new FleetMapper();

    @Override
    public FleetRecord postFleet(FleetRecordRequestBody fleetRequest) throws Exception {
        // Verifica se a frota já existe no banco de dados local
        if (fleetRepository.findByName(fleetRequest.getName()) != null) {
            throw new Exception("Já existe uma frota com esse nome.");
        }

        // Verifica se os IDs da tripulação existem na SWAPI
        List<CrewRecordFleet> crew = swapiClient.getCrew(1, null)
                .stream()
                .filter(crewRecord -> fleetRequest.getCrewIds().contains(crewRecord.getExternalId()))
                .collect(Collectors.toList());

        if (crew.size() != fleetRequest.getCrewIds().size()) {
            throw new Exception("Some crew members were not found in the external API.");
        }

        // Verifica se o ID da nave existe na SWAPI
        StarshipInternalRecordFleet starship = swapiClient.getStarships(1, null)
                .stream()
                .filter(starshipRecord -> starshipRecord.getExternal_id() == fleetRequest.getStarshipId())
                .findFirst()
                .orElseThrow(() -> new Exception("Starship not found in the external API"));

        // Cria a frota com os dados da tripulação e da nave
        FleetRecord fleet = new FleetRecord(fleetRequest.getName(), starship, crew);

        // Mapeia o FleetRecord para Document antes de salvar
        fleetRepository.saveFleet(fleetMapper.mapToFleetDocument(fleet));

        return fleet;
    }



    @Override
    public List<FleetRecord> getFleet(Integer page, String name) {
        if (name != null && !name.isEmpty()) {
            // Busca a frota pelo nome
            FleetRecord fleet = fleetRepository.findByName(name);
            return fleet != null ? List.of(fleet) : List.of();  // Retorna uma lista com a frota ou uma lista vazia
        } else {
            // Retorna todas as frotas de forma paginada
            int pageNumber = (page != null && page > 0) ? page : 1;  // Define um valor padrão para a página
            return fleetRepository.findAllPaginated(pageNumber);
        }
    }

    @Override
    public void deleteFleet(String name) throws Exception {
        // Verifica se a frota existe
        FleetRecord existingFleet = fleetRepository.findByName(name);
        if (existingFleet == null) {
            throw new Exception("Fleet not found");
        }

        // Remove a frota do banco de dados
        fleetRepository.deleteByName(name);
    }

    private boolean fleetExistsInSWAPI(FleetRecordRequestBody fleetRequest) throws Exception {
        // Busca a nave na SWAPI e verifica se o starshipId existe
        StarshipInternalRecordFleet existingStarship = swapiClient.getStarships(1, null)
                .stream()
                .filter(starshipRecord -> starshipRecord.getExternal_id() == fleetRequest.getStarshipId())
                .findFirst()
                .orElse(null);

        // Busca a tripulação na SWAPI e verifica se os crewIds existem
        List<Integer> requestedCrewIds = fleetRequest.getCrewIds();
        List<CrewRecordFleet> existingCrew = swapiClient.getCrew(1, null)
                .stream()
                .filter(crewRecord -> requestedCrewIds.contains(crewRecord.getExternalId()))
                .collect(Collectors.toList());

        return existingStarship != null && !existingCrew.isEmpty();
    }

}

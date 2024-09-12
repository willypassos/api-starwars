package br.com.swapi.service;

import br.com.swapi.model.*;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.mapper.FleetMapper;

import java.util.List;
import java.util.stream.Collectors;

public class FleetService implements IFleetService {

    private final FleetRepository fleetRepository = new FleetRepository();
    private final CrewService crewService = new CrewService();
    private final StarshipService starshipService = new StarshipService();
    private final FleetMapper fleetMapper = new FleetMapper();

    @Override
    public FleetRecord postFleet(FleetRecordRequestBody fleetRequest) throws Exception {
        // Verifique se a frota já existe
        if (fleetExistsInSWAPI(fleetRequest)) {
            throw new Exception("Fleet already exists in the external API.");
        }

        // Busca a tripulação da SWAPI com base no método getCrewByPage
        List<CrewRecordFleet> crew = crewService.getCrewByPage(1, null)  // Usa paginação e nome conforme necessário
                .stream()
                .filter(crewRecord -> fleetRequest.getCrewIds().contains(crewRecord.getExternalId()))  // Filtra pelos IDs de tripulantes
                .map(this::convertCrewRecordToCrewRecordFleet)  // Converte CrewRecord para CrewRecordFleet
                .collect(Collectors.toList());

        // Busca a nave da SWAPI com base no starship_id
        StarshipInternalRecordFleet starship = starshipService.getStarshipByPage(1, fleetRequest.getName())
                .stream()
                .findFirst()
                .orElseThrow(() -> new Exception("Starship not found in the external API"));

        // Cria a frota com os dados da tripulação e da nave
        FleetRecord fleet = new FleetRecord(fleetRequest.getName(), starship, crew);  // Passa o starship diretamente, não como lista

        // Mapeia o FleetRecord para Document antes de salvar
        fleetRepository.saveFleet(fleetMapper.mapToFleetDocument(fleet));

        return fleet;
    }

    @Override
    public FleetRecord updateFleet(String name, List<Integer> crewIds) throws Exception {
        // Verifica se a frota existe
        FleetRecord existingFleet = fleetRepository.findByName(name);
        if (existingFleet == null) {
            throw new Exception("Fleet not found");
        }

        // Atualiza a tripulação com base nos novos IDs
        List<CrewRecordFleet> updatedCrew = crewService.getCrewByPage(1, null)
                .stream()
                .filter(crew -> crewIds.contains(crew.getExternalId()))  // Filtra pelos IDs de tripulantes
                .map(this::convertCrewRecordToCrewRecordFleet)  // Converte CrewRecord para CrewRecordFleet
                .collect(Collectors.toList());

        existingFleet.setCrew(updatedCrew);
        fleetRepository.updateFleet(existingFleet);  // Atualiza a frota no banco de dados

        return existingFleet;
    }

    @Override
    public void deleteFleet(String name) {
        FleetRecord existingFleet = fleetRepository.findByName(name);
        if (existingFleet == null) {
            throw new RuntimeException("Fleet not found");
        }

        fleetRepository.deleteByName(name);
    }

    @Override
    public List<FleetRecord> getFleet(Integer page, String name) {
        if (name != null && !name.isEmpty()) {
            return List.of(fleetRepository.findByName(name));
        } else {
            return fleetRepository.findAllPaginated(page);
        }
    }

    // Verifica se a frota já existe na SWAPI
    private boolean fleetExistsInSWAPI(FleetRecordRequestBody fleetRequest) throws Exception {
        StarshipInternalRecordFleet existingStarship = starshipService.getStarshipByPage(1, fleetRequest.getName())
                .stream()
                .findFirst()
                .orElse(null);

        List<CrewRecordFleet> existingCrew = crewService.getCrewByPage(1, null)
                .stream()
                .filter(crewRecord -> fleetRequest.getCrewIds().contains(crewRecord.getExternalId()))  // Filtra pelos IDs de tripulantes
                .map(this::convertCrewRecordToCrewRecordFleet)  // Converte CrewRecord para CrewRecordFleet
                .collect(Collectors.toList());

        return existingStarship != null && !existingCrew.isEmpty();
    }

    // Método para converter CrewRecord para CrewRecordFleet
    private CrewRecordFleet convertCrewRecordToCrewRecordFleet(CrewRecord crewRecord) {
        return new CrewRecordFleet(
                crewRecord.getName(),
                crewRecord.getHeight(),
                crewRecord.getMass(),
                crewRecord.getGender(),
                crewRecord.getExternalId(),
                true  // Suponha que o tripulante está disponível
        );
    }
}

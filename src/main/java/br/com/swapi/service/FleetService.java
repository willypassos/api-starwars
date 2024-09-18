package br.com.swapi.service;

import br.com.swapi.model.*;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.mapper.FleetMapper;

import java.util.List;
import java.util.stream.Collectors;

public class FleetService implements IFleetService {

    private final FleetRepository fleetRepository = new FleetRepository(); //
    private final SWAPIClient swapiClient = new SWAPIClient(); // Consumindo diretamente da SWAPI
    private final FleetMapper fleetMapper = new FleetMapper();

    @Override
    public FleetRecord postFleet(FleetRecordRequestBody fleetRequest) throws Exception {
        // Verifica se a frota já existe no banco de dados local
        if (fleetRepository.findByName(fleetRequest.getName()) != null) { // Verifica se o nome da frota existe
            throw new Exception("Já existe uma frota com esse nome."); // Caso exista, lança uma exceção
        }

        // Verifica se os IDs da tripulação existem na SWAPI
        List<CrewRecordFleet> crew = swapiClient.getCrew(1, null) // Busca os tripulantes da SWAPI
                .stream() // Transforma os dados em Stream
                .filter(crewRecord -> fleetRequest.getCrewIds().contains(crewRecord.getExternalId())) // Filtra pelos IDs da tripulacao
                .collect(Collectors.toList()); // Transforma em uma lista

        // Valida o número de tripulantes
        validateCrewSize(crew); // Verifica se a tripulação possui entre 1 e 5 membros

        if (crew.size() != fleetRequest.getCrewIds().size()) {
            throw new Exception("Alguns membros da tripulação não foram encontrados na API externa."); // Caso não existam, lança uma exceção
        }

        // Verifica se o ID da nave existe na SWAPI
        StarshipInternalRecordFleet starship = swapiClient.getStarships(1, null) // Busca a nave da SWAPI
                .stream() // Transforma os dados em Stream
                .filter(starshipRecord -> starshipRecord.getExternal_id() == fleetRequest.getStarshipId()) // Filtra pelo ID da nave
                .findFirst() // Busca o primeiro registro da nave
                .orElseThrow(() -> new Exception("Nave não encontrada na API externa")); // Caso não exista, lança uma exceção

        // Cria a frota com os dados da tripulação e da nave
        FleetRecord fleet = new FleetRecord(fleetRequest.getName(), starship, crew); // Cria a frota

        // Mapeia o FleetRecord para Document antes de salvar
        fleetRepository.saveFleet(fleetMapper.mapToFleetDocument(fleet)); // Salva a frota

        return fleet; // Retorna a frota
    }




    @Override
    public List<FleetRecord> getFleet(Integer page, String name) {
        if (name != null && !name.isEmpty()) { // Verifica se o nome foi informado
            // Busca a frota pelo nome
            FleetRecord fleet = fleetRepository.findByName(name);
            return fleet != null ? List.of(fleet) : List.of();  // Retorna uma lista com a frota ou uma lista vazia
        } else {
            // Retorna todas as frotas de forma paginada
            int pageNumber = (page != null && page > 0) ? page : 1;  // Define um valor padrão para a página
            return fleetRepository.findAllPaginated(pageNumber); // Retorna uma lista com as frotas
        }
    }

    @Override
    public void deleteFleet(String name) throws Exception {
        // Verifica se a frota existe
        FleetRecord existingFleet = fleetRepository.findByName(name); // Busca a frota pelo nome
        if (existingFleet == null) {
            throw new Exception("Frota não encontrada"); // Caso não exista, lança uma exceção
        }

        // Remove a frota do banco de dados
        fleetRepository.deleteByName(name);
    }

    @Override
    public FleetRecord updateFleet(String name, List<Integer> crewIds) throws Exception {
        // Verifica se a frota existe no banco de dados local
        FleetRecord existingFleet = fleetRepository.findByName(name); // Busca a frota pelo nome
        if (existingFleet == null) { // Caso não exista, lança uma exceção
            throw new Exception("Frota não encontrada na base de dados."); // Caso não exista, lança uma exceção
        }

        // Verifica se os IDs da tripulação existem na SWAPI e se estão disponíveis
        List<CrewRecordFleet> updatedCrew = swapiClient.getCrew(1, null) // Busca os tripulantes da SWAPI
                .stream()
                .filter(crewRecord -> crewIds.contains(crewRecord.getExternalId())) // Filtra pelos IDs da tripulacao
                .collect(Collectors.toList()); // Transforma em uma lista

        // Valida o número de tripulantes
        validateCrewSize(updatedCrew); // Verifica se a tripulação possui entre 1 e 5 membros

        if (updatedCrew.size() != crewIds.size()) { // Verifica se todos os IDs da tripulacao existem
            throw new Exception("Alguns membros da tripulação não foram encontrados na API externa."); // Caso não existam, lança uma exceção
        }

        // Verifica se algum dos novos crewIds já está em uso em outra frota
        for (CrewRecordFleet crew : updatedCrew) {
            if (isCrewInUse(crew.getExternalId(), name)) { // Verifica excluindo a frota atual
                throw new Exception("Crew member with ID " + crew.getExternalId() + " está atualmente sendo utilizado."); // Caso esteja em uso, lança uma exceção
            }
        }

        // Atualiza a frota com os novos dados
        existingFleet.setCrew(updatedCrew); // Atualiza os tripulantes da frota

        // Mapeia o FleetRecord atualizado para Document e salva no banco de dados
        fleetRepository.updateFleet(fleetMapper.mapToFleetDocument(existingFleet)); // Atualiza a frota no MongoDB

        return existingFleet; // Retorna a frota atualizada
    }



    // Verifica se um tripulante está em uso em outra frota
    private boolean isCrewInUse(int crewId, String currentFleetName) {
        return fleetRepository.findAll().stream()// Busca todas as frota
                .filter(fleet -> !fleet.getName().equals(currentFleetName)) // Exclui a frota atual da verificação,para evitar falsa identificação.
                .flatMap(fleet -> fleet.getCrew().stream()) // Busca os tripulantes de cada frota
                .anyMatch(crew -> crew.getExternalId() == crewId); // Verifica se algum tripulante da frota atual tem o ID informado
    }

    private void validateCrewSize(List<CrewRecordFleet> crew) throws Exception {
        if (crew.size() < 1 || crew.size() > 5) {
            throw new Exception("Uma frota deve conter no mínimo 1 e no máximo 5 tripulantes.");
        }
    }


    private boolean isStarshipInUse(int starshipId) {
        return fleetRepository.findAll().stream()
                .anyMatch(fleet -> fleet.getStarship().getExternal_id() == starshipId);
    }
}

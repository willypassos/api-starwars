package br.com.swapi.service;

import br.com.swapi.model.*;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.mapper.FleetMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.stream.Collectors;

public class FleetService implements IFleetService {

    private final FleetRepository fleetRepository;
    private final SWAPIClient swapiClient ;
    private final FleetMapper fleetMapper;
    private final Jedis jedis;
    private final ObjectMapper objectMapper;

    // Construtor que injeta as dependências
    public FleetService(FleetRepository fleetRepository, SWAPIClient swapiClient, FleetMapper fleetMapper, Jedis jedis) {
        this.fleetRepository = fleetRepository;
        this.swapiClient = swapiClient;
        this.fleetMapper = fleetMapper;
        this.jedis = jedis;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FleetRecord postFleet(FleetRecordRequestBody fleetRequest) throws Exception {
        // Verifica se a frota já existe no banco de dados local
        if (fleetRepository.findByName(fleetRequest.getName()) != null) { // Verifica se o nome da frota existe
            throw new Exception("Já existe uma frota com esse nome."); // Caso exista, lança uma exceção
        }

        // Verifica se algum membro da tripulação já está em uso
        List<Integer> crewInUse = fleetRequest.getCrewIds().stream()
                .filter(crewId -> isCrewInUse(crewId, fleetRequest.getName())) // Verifica se o membro já está em uso
                .collect(Collectors.toList());

        if (!crewInUse.isEmpty()) {
            throw new Exception("Os seguintes membros da tripulação já estão em uso: " + crewInUse); // Lança uma exceção com todos os IDs em uso
        }

        // Verifica se o ID da nave já está em uso
        if (isStarshipInUse(fleetRequest.getStarshipId())) {
            throw new Exception("A nave com o ID " + fleetRequest.getStarshipId() + " já está em uso."); // Lança uma exceção se a nave estiver em uso
        }

        // Verifica se os IDs da tripulação existem na SWAPI
        List<CrewRecordFleet> crew = swapiClient.getCrew(1, null) // Busca os tripulantes da SWAPI
                .stream() // Transforma os dados em Stream
                .filter(crewRecord -> fleetRequest.getCrewIds().contains(crewRecord.getExternalId())) // Filtra pelos IDs da tripulação
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
    public List<FleetRecord> getFleet(Integer page, String name) throws Exception {
        String cacheKey = generateCacheKey(page, name);

        String cachedFleet = jedis.get(cacheKey);
        if (cachedFleet != null) {
            try {
                return objectMapper.readValue(cachedFleet, objectMapper.getTypeFactory().
                        constructCollectionType(List.class, FleetRecord.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new Exception("Erro ao processar o cache de dados da frota.");
            }
        }

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
                .filter(crewRecord -> crewIds.contains(crewRecord.getExternalId())) // Filtra pelos IDs da tripulação
                .collect(Collectors.toList()); // Transforma em uma lista

        // Valida o número de tripulantes
        validateCrewSize(updatedCrew); // Verifica se a tripulação possui entre 1 e 5 membros

        if (updatedCrew.size() != crewIds.size()) { // Verifica se todos os IDs da tripulação existem
            throw new Exception("Alguns membros da tripulação não foram encontrados na API externa."); // Caso não existam, lança uma exceção
        }

        // Verifica se algum dos novos crewIds já está em uso em outra frota
        List<Integer> crewInUse = crewIds.stream()
                .filter(crewId -> isCrewInUse(crewId, name)) // Verifica excluindo a frota atual
                .collect(Collectors.toList());

        if (!crewInUse.isEmpty()) {
            throw new Exception("Os seguintes membros da tripulação já estão em uso: " + crewInUse); // Lança exceção com os IDs em uso
        }

        // Atualiza a frota com os novos dados
        existingFleet.setCrew(updatedCrew); // Atualiza os tripulantes da frota

        // Mapeia o FleetRecord atualizado para Document e salva no banco de dados
        fleetRepository.updateFleet(fleetMapper.mapToFleetDocument(existingFleet)); // Atualiza a frota no MongoDB

        return existingFleet; // Retorna a frota atualizada
    }

    // Verifica se um tripulante está em uso em outra frota
    private boolean isCrewInUse(int crewId, String currentFleetName) {
        return fleetRepository.findAll().stream()// Busca todas as frotas
                .filter(fleet -> !fleet.getName().equals(currentFleetName)) // Exclui a frota atual da verificação
                .flatMap(fleet -> fleet.getCrew().stream())// Busca os tripulantes da frota
                .anyMatch(crew -> crew.getExternalId() == crewId); // Verifica se o tripulante está em uso
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

    // Gera uma chave de cache baseada na página e no nome da frota
    private String generateCacheKey(Integer page, String name) {
        if (name != null && !name.isEmpty()) {
            return "fleet:name:" + name;
        }
        return "fleet:page:" + page;
    }
}

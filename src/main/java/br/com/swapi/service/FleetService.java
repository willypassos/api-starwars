package br.com.swapi.service;

import br.com.swapi.config.RedisConfig;
import br.com.swapi.model.*;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.mapper.FleetMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FleetService implements IFleetService {

    private final FleetRepository fleetRepository;
    private final SWAPIClient swapiClient;
    private final FleetMapper fleetMapper;
    private final Jedis jedis;
    private final ObjectMapper objectMapper;

    // Construtor que injeta as dependências utilizando valores fixos para o Redis
    public FleetService(FleetRepository fleetRepository, SWAPIClient swapiClient, FleetMapper fleetMapper) {
        this.fleetRepository = fleetRepository;
        this.swapiClient = swapiClient;
        this.fleetMapper = fleetMapper;

        // Utiliza o RedisConfig para obter a instância de Jedis com o Redis rodando localmente
        this.jedis = RedisConfig.getJedis();
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

        // Limpa o cache relacionado a essa frota
        String cacheKey = generateCacheKey(null, fleetRequest.getName());
        jedis.del(cacheKey); // Remove o cache para garantir que novos dados sejam consultados

        // Salva os dados da frota em um arquivo JSON de espionagem
        saveEspionageData(fleet, "Criar");

        return fleet; // Retorna a frota
    }

    @Override
    public List<FleetRecord> getFleet(Integer page, String name) throws Exception {
        // Gera uma chave de cache baseada na página e no nome da frota
        String cacheKey = generateCacheKey(page, name);

        // Verifica se os dados estão no cache do Redis
        String cachedFleet = jedis.get(cacheKey);
        if (cachedFleet != null) { // Se o cache estiver disponível
            try {
                // Tenta deserializar os dados do cache armazenados como JSON
                List<FleetRecord> fleetRecords = objectMapper.readValue(cachedFleet, objectMapper.getTypeFactory() // Cria um objeto de mapeamento do Jackson
                        .constructCollectionType(List.class, FleetRecord.class));// Cria uma lista de FleetRecord

                // Verifica se a lista de fleetRecords não é nula ou vazia
                if (fleetRecords != null && !fleetRecords.isEmpty()) {
                    return fleetRecords; // Se o cache for válido, retorna os dados
                } else {
                    // Se o cache estiver vazio ou nulo, remove a chave do cache
                    jedis.del(cacheKey);
                }
            } catch (JsonProcessingException e) {
                // Remove o cache inválido para evitar futuros erros e lança exceção
                jedis.del(cacheKey);
                throw new Exception("Erro ao processar o cache de dados da frota para a chave: " + cacheKey, e);
            }
        }

        // Se o cache não estiver disponível ou ocorreu um erro, busca no banco de dados
        List<FleetRecord> fleets;
        if (name != null && !name.isEmpty()) {
            // Busca a frota pelo nome no banco de dados local
            FleetRecord fleet = fleetRepository.findByName(name);
            fleets = fleet != null ? List.of(fleet) : List.of(); // Retorna uma lista contendo a frota ou uma lista vazia
        } else {
            // Se o nome não for especificado, busca frotas paginadas
            int pageNumber = (page != null && page > 0) ? page : 1;
            fleets = fleetRepository.findAllPaginated(pageNumber); // Busca frotas paginadas
        }

        // Armazena os dados no cache do Redis
        try {
            int cacheExpiration = 3600; // Valor fixo para a expiração do cache (em segundos)
            jedis.setex(cacheKey, cacheExpiration, objectMapper.writeValueAsString(fleets));
        } catch (JsonProcessingException e) {
            // Se houver erro ao serializar os dados para o cache, lança exceção
            throw new Exception("Erro ao salvar dados no cache para a chave: " + cacheKey, e);
        }

        return fleets; // Retorna as frotas encontradas
    }

    @Override
    public void deleteFleet(String name) throws Exception {
        // Verifica se a frota existe
        FleetRecord existingFleet = fleetRepository.findByName(name);
        if (existingFleet == null) {
            throw new Exception("Frota não encontrada");
        }

        // Remove a frota do banco de dados
        fleetRepository.deleteByName(name);

        // Limpa o cache relacionado à frota excluída
        jedis.del("fleet:name:" + name);

        // Salva a atualização da exclusão no arquivo JSON de espionagem
        saveEspionageData(existingFleet, "Excluir");
    }

    @Override
    public FleetRecord updateFleet(String name, List<Integer> crewIds) throws Exception {
        // Verifica se a frota existe no banco de dados local
        FleetRecord existingFleet = fleetRepository.findByName(name);
        if (existingFleet == null) {
            throw new Exception("Frota não encontrada na base de dados.");
        }

        // Verifica se os IDs da tripulação existem na SWAPI e se estão disponíveis
        List<CrewRecordFleet> updatedCrew = swapiClient.getCrew(1, null)
                .stream()
                .filter(crewRecord -> crewIds.contains(crewRecord.getExternalId()))
                .collect(Collectors.toList());

        // Valida o número de tripulantes
        validateCrewSize(updatedCrew);

        if (updatedCrew.size() != crewIds.size()) {
            throw new Exception("Alguns membros da tripulação não foram encontrados na API externa.");
        }

        // Verifica se algum dos novos crewIds já está em uso em outra frota
        List<Integer> crewInUse = crewIds.stream()
                .filter(crewId -> isCrewInUse(crewId, name))
                .collect(Collectors.toList());

        if (!crewInUse.isEmpty()) {
            throw new Exception("Os seguintes membros da tripulação já estão em uso: " + crewInUse);
        }

        // Atualiza a frota com os novos dados
        existingFleet.setCrew(updatedCrew);
        fleetRepository.updateFleet(fleetMapper.mapToFleetDocument(existingFleet));

        // Limpa o cache relacionado à frota atualizada
        jedis.del("fleet:name:" + name);

        // Salva os dados atualizados no arquivo JSON de espionagem
        saveEspionageData(existingFleet, "Atualizar");

        return existingFleet;
    }

    // Verifica se um tripulante está em uso em outra frota
    private boolean isCrewInUse(int crewId, String currentFleetName) {
        return fleetRepository.findAll().stream()
                .filter(fleet -> !fleet.getName().equals(currentFleetName))
                .flatMap(fleet -> fleet.getCrew().stream())
                .anyMatch(crew -> crew.getExternalId() == crewId);
    }

    // Valida se o tamanho da tripulação está dentro dos limites permitidos
    private void validateCrewSize(List<CrewRecordFleet> crew) throws Exception {
        if (crew.size() < 1 || crew.size() > 5) {
            throw new Exception("Uma frota deve conter no mínimo 1 e no máximo 5 tripulantes.");
        }
    }

    // Verifica se a nave está em uso
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

    // Método para salvar dados de espionagem em um arquivo JSON de forma assíncrona
    private void saveEspionageData(FleetRecord fleet, String operation) {
        CompletableFuture.runAsync(() -> { // Cria uma tarefa assíncrona para gravar os dados
            try {
                // Carrega o conteúdo existente do arquivo
                File file = new File("espionagem.json");
                List<Object> fleetRecords = new ArrayList<>();

                if (file.exists()) {
                    // Se o arquivo existir, ler o conteúdo como uma lista de FleetRecord
                    try (FileReader reader = new FileReader(file)) {
                        fleetRecords = objectMapper.readValue(reader, new TypeReference<List<Object>>() {});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Cria um registro contendo a operação e os dados da frota
                var registro = new Object() {
                    public final String operacao = operation;
                    public final FleetRecord frota = fleet;
                };

                fleetRecords.add(registro); // Adiciona o novo registro ao array

                // Grava o array atualizado de frotas de volta ao arquivo
                try (FileWriter writer = new FileWriter(file)) {
                    objectMapper.writeValue(writer, fleetRecords);
                }
            } catch (IOException e) {
                // Tratar erro ao gravar o arquivo de espionagem
                e.printStackTrace();
            }
        });
    }
}

package br.com.swapi;

import br.com.swapi.client.CrewHandler;
import br.com.swapi.client.StarshipHandler;
import br.com.swapi.client.FleetHandler;
import br.com.swapi.config.RedisConfig;
import br.com.swapi.mapper.FleetMapper;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.service.IFleetService;
import br.com.swapi.service.SWAPIClient;
import br.com.swapi.service.FleetService;
import com.sun.net.httpserver.HttpServer;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        // Criando o servidor na porta 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Instanciando o SWAPIClient
        SWAPIClient swapiClient = new SWAPIClient();
        // Instanciando o Redis
        Jedis jedis = RedisConfig.getJedis();
        FleetRepository fleetRepository = new FleetRepository();
        FleetMapper fleetMapper = new FleetMapper();

        // Instanciando o serviço para Fleet
        IFleetService fleetService = new FleetService(fleetRepository, swapiClient, fleetMapper, jedis);


        // Instanciando os handlers, agora passando SWAPIClient e fleetService diretamente
        CrewHandler crewHandler = new CrewHandler(swapiClient);
        StarshipHandler starshipHandler = new StarshipHandler(swapiClient);
        FleetHandler fleetHandler = new FleetHandler(fleetService);

        // Configuração dos contextos (rotas) para os handlers
        server.createContext("/starwars/v1/crew", crewHandler);
        server.createContext("/starwars/v1/starship", starshipHandler);
        server.createContext("/starwars/v1/fleet", fleetHandler);  // Rota para Fleet

        // Definindo o executor e iniciando o servidor
        server.setExecutor(null);  // Usa o executor padrão
        server.start();

        System.out.println("Servidor rodando em http://localhost:8080/starwars/v1");
    }
}

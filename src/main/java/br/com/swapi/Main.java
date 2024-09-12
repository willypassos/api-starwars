package br.com.swapi;

import br.com.swapi.client.CrewHandler;
import br.com.swapi.client.StarshipHandler;
import br.com.swapi.client.FleetHandler;
import br.com.swapi.config.MongoDBConfig;
import br.com.swapi.repository.FleetRepository;
import br.com.swapi.service.*;

import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        // Criando o servidor na porta 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Instanciando o SWAPIClient
        SWAPIClient swapiClient = new SWAPIClient();

        // Instanciando o MongoDB Database
        MongoDatabase mongoDatabase = MongoDBConfig.getDatabase();

        // Instanciando o repositório do Fleet
        FleetRepository fleetRepository = new FleetRepository();

        // Instanciando os serviços
        IStarshipService starshipService = new StarshipService();
        ICrewService crewService = new CrewService();
        IFleetService fleetService = new FleetService();

        // Instanciando os handlers
        CrewHandler crewHandler = new CrewHandler(crewService);
        StarshipHandler starshipHandler = new StarshipHandler(starshipService);
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

package br.com.swapi;

import br.com.swapi.client.CrewHandler;
import br.com.swapi.client.StarshipHandler;
import br.com.swapi.config.MongoDBConfig;
import br.com.swapi.repository.CrewRepository;
import br.com.swapi.repository.StarshipRepository;
import br.com.swapi.service.CrewService;
import br.com.swapi.service.IStarshipService;
import br.com.swapi.service.SWAPIClient;
import br.com.swapi.service.StarshipService;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Instanciando o SWAPIClient
        SWAPIClient swapiClient = new SWAPIClient();

        // Instanciando o MongoDB Database
        MongoDatabase mongoDatabase = MongoDBConfig.getDatabase();

        // Instanciando os repositórios
        StarshipRepository starshipRepository = new StarshipRepository();
        CrewRepository crewRepository = new CrewRepository();

        // Instanciando os serviços usando as interfaces
        IStarshipService starshipService = new StarshipService(swapiClient, starshipRepository);
        CrewService crewService = new CrewService(swapiClient, crewRepository);

        // Instanciando os handlers
        CrewHandler crewHandler = new CrewHandler(crewService);
        StarshipHandler starshipHandler = new StarshipHandler(starshipService);

        // Configuração dos contextos (rotas) para os handlers
        server.createContext("/starwars/v1/crew", crewHandler);
        server.createContext("/starwars/v1/starship", starshipHandler);
//        server.createContext("/starwars/v1/fleet", fleetHandler);

        server.setExecutor(null); // Define o executor do servidor
        server.start(); // Inicia o servidor

        System.out.println("Servidor rodando em http://localhost:8080/starwars/v1");
    }
}

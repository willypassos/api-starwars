package br.com.swapi;

import br.com.swapi.client.CrewHandler;
import br.com.swapi.client.FleetHandler;
import br.com.swapi.client.StarshipHandler;
import br.com.swapi.service.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Instanciando o SWAPIClient
        SWAPIClient swapiClient = new SWAPIClient();

        // Instanciando os serviços usando as interfaces
        ICrewService crewService = new CrewService(swapiClient);
        IStarshipService starshipService = new StarshipService(swapiClient);
        IFleetService fleetService = new FleetService(swapiClient);

        // Instanciando os handlers
        CrewHandler crewHandler = new CrewHandler((CrewService) crewService);
        StarshipHandler starshipHandler = new StarshipHandler(starshipService);
        FleetHandler fleetHandler = new FleetHandler(fleetService);

        // Configuração dos contextos (rotas) para os handlers
        server.createContext("/starwars/v1/crew", crewHandler);
        server.createContext("/starwars/v1/starship", starshipHandler);
        server.createContext("/starwars/v1/fleet", fleetHandler);

        server.setExecutor(null);
        server.start();

        System.out.println("Servidor rodando em http://localhost:8080/starwars/v1");
    }
}

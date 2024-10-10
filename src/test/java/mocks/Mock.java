package mocks;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.FleetRecordRequestBody;
import br.com.swapi.model.StarshipInternalRecordFleet;

import java.util.List;

public class Mock {
    public  static FleetRecordRequestBody getMockFleetRecordRequestBody() {
        return new FleetRecordRequestBody("Fleet1", List.of(1,2,3), getMockStarshipRecord().getExternal_id());
    }
   // Método estático para retornar um mock de StarshipInternalRecordFleet
    public static StarshipInternalRecordFleet getMockStarshipRecord() {
        return new StarshipInternalRecordFleet(
                "Teste", // Nome da nave
                "YT-1300 light freighter", // Modelo
                "100000", // Preço
                "4", // Tripulação
                "100000", // Capacidade de carga
                "1050", // Velocidade máxima
                2, // ID externo
                "Freighter", // Classe da nave
                true // Disponível
        );
    }

    // Método estático para retornar uma lista de CrewRecordFleet
    public static List<CrewRecordFleet> getMockCrewRecordFleet() {
        return List.of(
                new CrewRecordFleet("Luke Skywalker", "172", "77", "male", 1, true),  // Tripulante 1
                new CrewRecordFleet("Han Solo", "180", "80", "male", 2, true),        // Tripulante 2
                new CrewRecordFleet("Leia Organa", "150", "49", "female", 3, true)    // Tripulante 3
        );
    }

    // Método estático para retornar um mock de FleetRecord
    public static FleetRecord getMockFleetRecord() {
        return new FleetRecord("Fleet1", getMockStarshipRecord(), getMockCrewRecordFleet());
    }
}

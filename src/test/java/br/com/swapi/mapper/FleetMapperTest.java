package br.com.swapi.mapper;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.StarshipInternalRecordFleet;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FleetMapperTest {

    private FleetMapper fleetMapper;

    @BeforeEach
    public void setUp() {
        fleetMapper = new FleetMapper();
    }

    @Test
    public void testMapToFleetDocument_Success() {
        // Configura uma nave fictícia
        StarshipInternalRecordFleet starship = new StarshipInternalRecordFleet(
                "Millennium Falcon", "YT-1300", "100000", "4", "100000", "1000", 10, "Freighter", true
        );

        // Configura uma tripulação fictícia
        CrewRecordFleet crew1 = new CrewRecordFleet("Luke Skywalker", "172", "77", "male", 1, true);
        CrewRecordFleet crew2 = new CrewRecordFleet("Han Solo", "180", "80", "male", 2, true);

        // Configura uma frota fictícia
        FleetRecord fleetRecord = new FleetRecord("Rebel Fleet", starship, List.of(crew1, crew2));

        // Chama o método que está sendo testado
        Document fleetDoc = fleetMapper.mapToFleetDocument(fleetRecord);

        // Verifica os valores do documento
        assertEquals("Rebel Fleet", fleetDoc.getString("name"));

        List<Document> crewDocs = (List<Document>) fleetDoc.get("crew");
        assertEquals(2, crewDocs.size());

        // Verifica o primeiro tripulante
        Document crewDoc1 = crewDocs.get(0);
        assertEquals("Luke Skywalker", crewDoc1.getString("name"));
        assertEquals("172", crewDoc1.getString("height"));
        assertEquals(1, crewDoc1.getInteger("external_id"));
        assertTrue(crewDoc1.getBoolean("available"));

        // Verifica o documento da nave
        Document starshipDoc = (Document) fleetDoc.get("starship");
        assertEquals("Millennium Falcon", starshipDoc.getString("name"));
        assertEquals("YT-1300", starshipDoc.getString("model"));
        assertTrue(starshipDoc.getBoolean("available"));
    }

    @Test
    public void testMapToFleetRecord_Success() {
        // Configura uma nave fictícia como Document MongoDB
        Document starshipDoc = new Document("external_id", 10)
                .append("name", "Millennium Falcon")
                .append("model", "YT-1300")
                .append("price", "100000")
                .append("crew", "4")
                .append("cargo", "100000")
                .append("speed", "1000")
                .append("starship_class", "Freighter")
                .append("available", true);

        // Configura uma tripulação fictícia como Document MongoDB
        Document crewDoc1 = new Document("external_id", 1)
                .append("name", "Luke Skywalker")
                .append("height", "172")
                .append("mass", "77")
                .append("gender", "male")
                .append("available", true);

        Document crewDoc2 = new Document("external_id", 2)
                .append("name", "Han Solo")
                .append("height", "180")
                .append("mass", "80")
                .append("gender", "male")
                .append("available", true);

        // Configura uma frota fictícia como Document MongoDB
        Document fleetDoc = new Document("name", "Rebel Fleet")
                .append("crew", List.of(crewDoc1, crewDoc2))
                .append("starship", starshipDoc);

        // Chama o método que está sendo testado
        FleetRecord fleetRecord = fleetMapper.mapToFleetRecord(fleetDoc);

        // Verifica os valores da frota
        assertEquals("Rebel Fleet", fleetRecord.getName());

        // Verifica a tripulação
        List<CrewRecordFleet> crew = fleetRecord.getCrew();
        assertEquals(2, crew.size());
        assertEquals("Luke Skywalker", crew.get(0).getName());
        assertEquals(1, crew.get(0).getExternalId());

        // Verifica a nave
        StarshipInternalRecordFleet starship = fleetRecord.getStarship();
        assertEquals("Millennium Falcon", starship.getName());
        assertEquals(10, starship.getExternal_id());
        assertTrue(starship.isAvailable());
    }

    @Test
    public void testMapToFleetRecord_NullStarship() {
        // Configura uma frota fictícia sem nave como Document MongoDB
        Document crewDoc = new Document("external_id", 1)
                .append("name", "Luke Skywalker")
                .append("height", "172")
                .append("mass", "77")
                .append("gender", "male")
                .append("available", true);

        Document fleetDoc = new Document("name", "Rebel Fleet")
                .append("crew", List.of(crewDoc))
                .append("starship", null);  // Nave nula

        // Chama o método que está sendo testado
        FleetRecord fleetRecord = fleetMapper.mapToFleetRecord(fleetDoc);

        // Verifica que a nave está nula
        assertNull(fleetRecord.getStarship());

        // Verifica que a tripulação foi mapeada corretamente
        List<CrewRecordFleet> crew = fleetRecord.getCrew();
        assertEquals(1, crew.size());
        assertEquals("Luke Skywalker", crew.get(0).getName());
    }

    @Test
    public void testMapToFleetRecord_EmptyCrewList() {
        // Configura uma frota fictícia com lista de tripulação vazia como Document MongoDB
        Document starshipDoc = new Document("external_id", 10)
                .append("name", "Millennium Falcon")
                .append("model", "YT-1300")
                .append("available", true);

        Document fleetDoc = new Document("name", "Rebel Fleet")
                .append("crew", List.of())  // Lista de tripulação vazia
                .append("starship", starshipDoc);

        // Chama o método que está sendo testado
        FleetRecord fleetRecord = fleetMapper.mapToFleetRecord(fleetDoc);

        // Verifica que a lista de tripulação está vazia
        assertTrue(fleetRecord.getCrew().isEmpty());

        // Verifica que a nave foi mapeada corretamente
        assertEquals("Millennium Falcon", fleetRecord.getStarship().getName());
    }
}

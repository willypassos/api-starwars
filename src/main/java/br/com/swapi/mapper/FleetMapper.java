package br.com.swapi.mapper;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.StarshipInternalRecordFleet;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class FleetMapper {

    // Mapeia um FleetRecord para um Document do MongoDB
    public Document mapToFleetDocument(FleetRecord fleet) {
        List<Document> crewDocs = fleet.getCrew().stream()
                .map(this::mapCrewToDocument)
                .collect(Collectors.toList());

        Document starshipDoc = mapStarshipToDocument(fleet.getStarship());

        return new Document("name", fleet.getName())
                .append("crew", crewDocs)
                .append("starship", starshipDoc);
    }

    // Mapeia um Document do MongoDB para um FleetRecord
//    public FleetRecord mapToFleetRecord(Document doc) {
//        List<CrewRecordFleet> crew = ((List<Document>) doc.get("crew")).stream()
//                .map(this::mapDocumentToCrew)
//                .collect(Collectors.toList());
//
//        StarshipInternalRecordFleet starship = mapDocumentToStarship((Document) doc.get("starship"));
//
//        return new FleetRecord(
//                doc.getString("name"),
//                starship,
//                crew
//        );
//    }

    public FleetRecord mapToFleetRecord(Document doc) {
        List<CrewRecordFleet> crew = doc.containsKey("crew") && doc.get("crew") != null
                ? ((List<Document>) doc.get("crew")).stream()
                .map(this::mapDocumentToCrew)
                .collect(Collectors.toList())
                : List.of(); // Retorna uma lista vazia se "crew" não estiver presente ou for nulo

        StarshipInternalRecordFleet starship = doc.containsKey("starship") && doc.get("starship") != null
                ? mapDocumentToStarship((Document) doc.get("starship"))
                : null; // Retorna nulo se "starship" não estiver presente ou for nulo

        return new FleetRecord(
                doc.getString("name"),
                starship,
                crew
        );
    }


    // Mapeia um CrewRecordFleet para um Document MongoDB
    private Document mapCrewToDocument(CrewRecordFleet crew) {
        return new Document("external_id", crew.getExternalId())
                .append("name", crew.getName())
                .append("height", crew.getHeight())
                .append("mass", crew.getMass())
                .append("gender", crew.getGender())
                .append("available", crew.isAvailable());
    }

    // Mapeia um Document MongoDB para um CrewRecordFleet
    private CrewRecordFleet mapDocumentToCrew(Document doc) {
        // Verifica se o campo "available" existe e é um Boolean, caso contrário, define como false
        boolean available = doc.containsKey("available") && Boolean.TRUE.equals(doc.getBoolean("available"));
        return new CrewRecordFleet(
                doc.getString("name"),
                doc.getString("height"),
                doc.getString("mass"),
                doc.getString("gender"),
                doc.getInteger("external_id"),
                available
        );
    }

    // Mapeia um StarshipInternalRecordFleet para um Document MongoDB
    private Document mapStarshipToDocument(StarshipInternalRecordFleet starship) {
        return new Document("external_id", starship.getExternal_id())
                .append("name", starship.getName())
                .append("model", starship.getModel())
                .append("price", starship.getPrice())
                .append("crew", starship.getCrew())
                .append("cargo", starship.getCargo())
                .append("speed", starship.getSpeed())
                .append("starship_class", starship.getStarship_class())
                .append("available", starship.isAvailable());
    }

    // Mapeia um Document MongoDB para um StarshipInternalRecordFleet
    private StarshipInternalRecordFleet mapDocumentToStarship(Document doc) {
        // Verifica se o campo "available" existe e é um Boolean, caso contrário, define como false
        boolean available = doc.containsKey("available") && Boolean.TRUE.equals(doc.getBoolean("available"));
        return new StarshipInternalRecordFleet(
                doc.getString("name"),
                doc.getString("model"),
                doc.getString("price"),
                doc.getString("crew"),
                doc.getString("cargo"),
                doc.getString("speed"),
                doc.getInteger("external_id"),
                doc.getString("starship_class"),
                available
        );
    }
}

package br.com.swapi.model;

import org.bson.Document;
import java.util.List;

public class FleetRecord {
    private String name;
    private List<CrewRecordFleet> crewList;  // Lista de membros da tripulação
    private StarshipInternalRecordFleet starship;  // Nave

    // Construtor completo
    public FleetRecord(String name, List<CrewRecordFleet> crewList, StarshipInternalRecordFleet starship) {
        this.name = name;
        this.crewList = crewList;
        this.starship = starship;
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CrewRecordFleet> getCrewList() {
        return crewList;
    }

    public void setCrewList(List<CrewRecordFleet> crewList) {
        this.crewList = crewList;
    }

    public StarshipInternalRecordFleet getStarship() {
        return starship;
    }

    public void setStarship(StarshipInternalRecordFleet starship) {
        this.starship = starship;
    }

    // Conversão para Document do MongoDB
    public Document toDocument() {
        Document fleetDoc = new Document("name", this.name)
                .append("crewList", this.crewList.stream().map(CrewRecordFleet::toDocument).toList())
                .append("starship", this.starship.toDocument());
        return fleetDoc;
    }

    // Conversão de Document para FleetRecord
    public static FleetRecord fromDocument(Document doc) {
        List<CrewRecordFleet> crewList = doc.getList("crewList", Document.class).stream()
                .map(CrewRecordFleet::fromDocument).toList();
        StarshipInternalRecordFleet starship = StarshipInternalRecordFleet.fromDocument(doc.get("starship", Document.class));

        return new FleetRecord(
                doc.getString("name"),
                crewList,
                starship
        );
    }
}

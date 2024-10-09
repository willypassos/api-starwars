package br.com.swapi.model;

import java.util.List;

public class FleetRecord {
    private String name;
    private List<CrewRecordFleet> crew;  // Usando CrewRecordFleet conforme especificado no Swagger
    private StarshipInternalRecordFleet starship;  // Usando StarshipInternalRecordFleet conforme especificado

    public FleetRecord() {}
    public FleetRecord(String name, StarshipInternalRecordFleet starship, List<CrewRecordFleet> crew) {
        this.name = name;
        this.starship = starship;
        this.crew = crew;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CrewRecordFleet> getCrew() {
        return crew;
    }

    public void setCrew(List<CrewRecordFleet> crew) {
        this.crew = crew;
    }

    public StarshipInternalRecordFleet getStarship() {
        return starship;
    }

    public void setStarship(StarshipInternalRecordFleet starship) {
        this.starship = starship;
    }
}

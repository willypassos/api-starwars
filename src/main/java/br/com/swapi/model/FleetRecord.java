package br.com.swapi.model;

import java.util.List;

public class FleetRecord {
    private String name;
    private List<CrewRecordFleet> crew;
    private StarshipInternalRecordFleet starship;

    public FleetRecord() {
    }

    public FleetRecord(String name, List<CrewRecordFleet> crew, StarshipInternalRecordFleet starship) {
        this.name = name;
        this.crew = crew;
        this.starship = starship;
    }

    public FleetRecord(String name, String s, String s1, String s2, String s3, String s4, int url, String s5, boolean b) {
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

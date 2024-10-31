package br.com.swapi.model;

import java.util.List;

public class FleetRecordRequestBody {
    private String name;
    private List<Integer> crewIds;
    private Integer starshipId;

    public FleetRecordRequestBody() {
    }

    public FleetRecordRequestBody(String name, List<Integer> crewIds, Integer starshipId) {
        this.name = name;
        this.crewIds = crewIds;
        this.starshipId = starshipId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getCrewIds() {
        return crewIds;
    }

    public void setCrewIds(List<Integer> crewIds) {
        this.crewIds = crewIds;
    }

    public int getStarshipId() {
        return starshipId;
    }

    public void setStarshipId(int starshipId) {
        this.starshipId = starshipId;
    }
}

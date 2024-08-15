package br.com.swapi.service;

import br.com.swapi.model.CrewRecordFleet;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.StarshipInternalRecordFleet;

import java.io.IOException;
import java.util.List;

public interface IFleetService {
    List<FleetRecord> getFleet(int page, String name) throws IOException;
    FleetRecord createFleet(String name, List<CrewRecordFleet> crewList, StarshipInternalRecordFleet starship);

}

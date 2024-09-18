package br.com.swapi.service;

import br.com.swapi.model.FleetRecord;
import br.com.swapi.model.FleetRecordRequestBody;

import java.util.List;

public interface IFleetService {
    FleetRecord postFleet(FleetRecordRequestBody fleetRequest) throws Exception;
    List<FleetRecord> getFleet(Integer page, String name) throws Exception;
    void deleteFleet(String name) throws Exception;
    FleetRecord updateFleet(String name, List<Integer> crewIds) throws Exception;}

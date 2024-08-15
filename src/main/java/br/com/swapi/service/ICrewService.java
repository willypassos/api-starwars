package br.com.swapi.service;

import br.com.swapi.model.CrewRecord;

import java.io.IOException;
import java.util.List;

public interface ICrewService {
    List<CrewRecord> getCrewByPage(int page, String name) throws IOException;
}

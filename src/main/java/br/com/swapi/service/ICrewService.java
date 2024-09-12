package br.com.swapi.service;

import br.com.swapi.model.CrewRecord;
import br.com.swapi.model.CrewRecordFleet;
import java.io.IOException;
import java.util.List;

public interface ICrewService {
    // Método conforme o operationId do Swagger: getCrewByPage
    List<CrewRecord> getCrewByPage(int page, String name) throws IOException;
}

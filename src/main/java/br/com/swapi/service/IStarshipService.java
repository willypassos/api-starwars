package br.com.swapi.service;

import br.com.swapi.model.StarshipInternalRecordFleet;
import java.io.IOException;
import java.util.List;

public interface IStarshipService {

    // Método conforme o operationId do Swagger: getStarshipByPage
    List<StarshipInternalRecordFleet> getStarshipByPage(int page, String name) throws IOException;
}

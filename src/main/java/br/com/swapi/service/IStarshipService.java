package br.com.swapi.service;

import br.com.swapi.model.StarshipInternalRecord;

import java.io.IOException;
import java.util.List;

public interface IStarshipService {
    List<StarshipInternalRecord> getStarshipByPage(int page, String name) throws IOException;
}

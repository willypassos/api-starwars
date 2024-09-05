package br.com.swapi.repository;

import br.com.swapi.config.MongoDBConfig;
import br.com.swapi.model.StarshipInternalRecord;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarshipRepository {

    private static final Logger logger = LoggerFactory.getLogger(StarshipRepository.class);
    private MongoCollection<Document> collection;

    public StarshipRepository() {
        try {
            MongoDatabase database = MongoDBConfig.getDatabase();
            this.collection = database.getCollection("starships");
        } catch (Exception e) {
            logger.error("Erro ao conectar ao MongoDB", e);
        }
    }

    public void saveStarship(StarshipInternalRecord starship) {
        try {
            Document starshipDoc = starship.toDocument();
            collection.insertOne(starshipDoc);
            logger.info("Nave '{}' salva no MongoDB", starship.getName());
        } catch (Exception e) {
            logger.error("Erro ao salvar a nave no MongoDB", e);
        }
    }

    public StarshipInternalRecord findStarshipByName(String name) {
        try {
            Document starshipDoc = collection.find(new Document("name", name)).first();
            if (starshipDoc != null) {
                logger.info("Nave '{}' encontrada no MongoDB", name);
                return StarshipInternalRecord.fromDocument(starshipDoc);
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar a nave no MongoDB", e);
        }
        return null;
    }
}

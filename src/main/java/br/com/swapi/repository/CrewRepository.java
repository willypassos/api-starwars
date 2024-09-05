package br.com.swapi.repository;

import br.com.swapi.config.MongoDBConfig;
import br.com.swapi.model.CrewRecord;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrewRepository {

    private static final Logger logger = LoggerFactory.getLogger(CrewRepository.class);

    private MongoCollection<Document> collection;

    public CrewRepository() {
        try {
            MongoDatabase database = MongoDBConfig.getDatabase();// Conectar ao MongoDB
            this.collection = database.getCollection("crew");// Selecionar a coleção
        } catch (Exception e) {
            logger.error("Erro ao conectar ao MongoDB", e);// Log de erro
        }
    }

    public void saveCrew(CrewRecord crew) {
        try {
            Document crewDoc = crew.toDocument(); // Converter de CrewRecord para Document
            collection.insertOne(crewDoc); // Salvar no MongoDB
            logger.info("Tripulante '{}' salvo no MongoDB", crew.getName()); // Log de sucesso
        } catch (Exception e) {
            logger.error("Erro ao salvar o tripulante: ", e); // Log de erro
        }
    }

    public CrewRecord findCrewByName(String name) {
        try {
            Document crewDoc = collection.find(new Document("name", name)).first();// Buscar pelo nome
            if (crewDoc != null) {
                logger.info("Tripulante '{}' encontrado no MongoDB", name);// Log de sucesso
                return CrewRecord.fromDocument(crewDoc); // Converter de Document para CrewRecord
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar o tripulante: ", e); // Log de erro
        }
        return null;
    }

    public CrewRecord findCrewByExternalId(int externalId) {
        try {
            Document crewDoc = collection.find(new Document("externalId", externalId)).first();// Buscar pelo ID
            if (crewDoc != null) { // Se o tripulante foi encontrado
                logger.info("Tripulante com ID externo '{}' encontrado no MongoDB", externalId);// Log de sucesso
                return CrewRecord.fromDocument(crewDoc); // Converter de Document para CrewRecord
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar o tripulante por ID externo: ", e); // Log de erro
        }
        return null;
    }
}

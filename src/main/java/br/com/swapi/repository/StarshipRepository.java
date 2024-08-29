package br.com.swapi.repository;

import br.com.swapi.config.MongoDBConfig;
import br.com.swapi.config.RedisConfig;
import br.com.swapi.model.StarshipInternalRecord;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import com.mongodb.MongoException;

public class StarshipRepository {

    private static final Logger logger = LoggerFactory.getLogger(StarshipRepository.class);

    private MongoCollection<Document> collection;
    private Jedis jedis;

    public StarshipRepository() {
        try {
            MongoDatabase database = MongoDBConfig.getDatabase();
            this.collection = database.getCollection("starships");
            this.jedis = RedisConfig.getJedis();
        } catch (MongoException e) {
            logger.error("Erro ao conectar ao MongoDB", e);
        } catch (JedisConnectionException e) {
            logger.error("Erro ao conectar ao Redis", e);
        }
    }

    public void saveStarship(String name, String cargoCapacity, String starshipClass, String maxSpeed) {
        try {
            // Remover a conversão para int, armazenar como String
            Document starship = new Document("name", name)
                    .append("cargoCapacity", cargoCapacity)
                    .append("starshipClass", starshipClass)
                    .append("maxSpeed", maxSpeed);

            // Log para verificação do documento antes de salvar
            logger.info("Tentando salvar nave: {}", starship.toJson());

            collection.insertOne(starship);
            jedis.set(name, starship.toJson());
            logger.info("Nave '{}' salva no MongoDB e cacheada no Redis", name);
        } catch (MongoException e) {
            logger.error("Erro ao salvar a nave no MongoDB", e);
        } catch (JedisConnectionException e) {
            logger.error("Erro ao salvar a nave no Redis", e);
        }
    }

    public StarshipInternalRecord findStarshipByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.error("O nome da nave não pode ser nulo ou vazio");
            return null;
        }

        try {
            String cachedStarship = jedis.get(name);
            if (cachedStarship != null) {
                logger.info("Nave '{}' encontrada no cache Redis", name);
                return StarshipInternalRecord.fromDocument(Document.parse(cachedStarship));
            }

            Document starshipDoc = collection.find(new Document("name", name)).first();
            if (starshipDoc != null) {
                jedis.set(name, starshipDoc.toJson());
                logger.info("Nave '{}' encontrada no MongoDB e cacheada no Redis", name);
                return StarshipInternalRecord.fromDocument(starshipDoc);
            }
            return null;
        } catch (MongoException e) {
            logger.error("Erro ao buscar a nave no MongoDB", e);
            return null;
        } catch (JedisConnectionException e) {
            logger.error("Erro ao buscar a nave no Redis", e);
            return null;
        }
    }

    public void closeConnections() {
        try {
            MongoDBConfig.close();
            if (jedis != null) {
                jedis.close();
                logger.info("Conexões com MongoDB e Redis fechadas");
            }
        } catch (MongoException e) {
            logger.error("Erro ao fechar a conexão com o MongoDB", e);
        } catch (JedisConnectionException e) {
            logger.error("Erro ao fechar a conexão com o Redis", e);
        }
    }
}

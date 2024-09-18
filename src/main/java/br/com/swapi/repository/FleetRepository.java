package br.com.swapi.repository;

import br.com.swapi.config.MongoDBConfig;
import br.com.swapi.model.FleetRecord;
import br.com.swapi.mapper.FleetMapper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class FleetRepository {

    private final MongoCollection<Document> fleetCollection;

    public FleetRepository() {
        // Inicializa a conexão com o MongoDB e acessa a coleção de frota
        this.fleetCollection = MongoDBConfig.getDatabase().getCollection("fleet");
    }

    // Método para salvar a frota no MongoDB
    public void saveFleet(Document fleetDocument) {
        // Insere o documento da frota no MongoDB
        fleetCollection.insertOne(fleetDocument);
    }

    // Busca uma frota pelo nome
    public FleetRecord findByName(String name) {
        Document query = new Document("name", name);
        Document result = fleetCollection.find(query).first();
        return result != null ? new FleetMapper().mapToFleetRecord(result) : null;
    }

    public void deleteByName(String name) {
        Document query = new Document("name", name);
        fleetCollection.deleteOne(query);
    }


    // Busca todas as frotas de forma paginada
    public List<FleetRecord> findAllPaginated(int page) {
        int pageSize = 10; // Define o tamanho da página
        List<FleetRecord> fleets = new ArrayList<>();
        fleetCollection.find()
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .forEach(doc -> fleets.add(new FleetMapper().mapToFleetRecord(doc)));
        return fleets;
    }

    public void updateFleet(Document fleetDocument) {
        String fleetName = fleetDocument.getString("name");
        Document filter = new Document("name", fleetName);
        Document update = new Document("$set", fleetDocument);

        // Atualiza o documento da frota no MongoDB
        fleetCollection.updateOne(filter, update);
    }


    public List<FleetRecord> findAll() {
        List<FleetRecord> fleets = new ArrayList<>();
        fleetCollection.find().forEach(doc -> fleets.add(new FleetMapper().mapToFleetRecord(doc)));
        return fleets;
    }

}

package br.com.swapi.repository;

import br.com.swapi.mapper.FleetMapper;
import br.com.swapi.model.FleetRecord;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class FleetRepositoryTest {

    private FleetRepository fleetRepository;
    private MongoCollection<Document> fleetCollection;
    private FleetMapper fleetMapper;

    @BeforeEach
    public void setUp() {
        // Mock da coleção do MongoDB
        fleetCollection = mock(MongoCollection.class);
        fleetMapper = mock(FleetMapper.class);  // Mock FleetMapper

        // Inicializamos o repositório e mockamos o comportamento do `fleetCollection`
        fleetRepository = new FleetRepository();

        // Aqui fazemos com que o fleetRepository use o mock de `MongoCollection` diretamente
        // através da reflexão (para acessar o campo privado `fleetCollection`)
        try {
            var fleetCollectionField = FleetRepository.class.getDeclaredField("fleetCollection");
            fleetCollectionField.setAccessible(true);
            fleetCollectionField.set(fleetRepository, fleetCollection);  // Injeta o mock na instância
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveFleet_Success() {
        Document fleetDocument = new Document("name", "Fleet1");

        fleetRepository.saveFleet(fleetDocument);

        verify(fleetCollection).insertOne(fleetDocument);
    }
    @Test
    public void testFindByName_Success() {
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        // Cria um documento mockado que será retornado pelo find do MongoDB
        Document mockDocument = new Document("name", "Fleet1");

        // Simula o comportamento do MongoDB find() e first()
        when(fleetCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(mockDocument);

        // Simula o mapeamento do documento para o FleetRecord
        FleetRecord expectedFleet = new FleetRecord("Fleet1", null, null);
        when(fleetMapper.mapToFleetRecord(mockDocument)).thenReturn(expectedFleet);

        // Executa o método a ser testado
        FleetRecord result = fleetRepository.findByName("Fleet1");

        // Verifica o resultado esperado
        assertEquals("Fleet1", result.getName());
    }

    @Test
    public void testFindByName_NullResult() {
        // Cria um mock de FindIterable para simular o MongoDB retornando nulo
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        // Simula o comportamento do MongoDB find() retornando null
        when(fleetCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);

        // Executa o método com um nome vazio (ou inexistente)
        FleetRecord result = fleetRepository.findByName("");

        // Verifica se o retorno foi null, já que nenhum documento foi encontrado
        assertNull(result);
    }

    @Test
    public void testDeleteByName() {
        // Documento esperado para ser deletado
        Document expectedQuery = new Document("name", "Fleet1");

        // Executa o método deleteByName
        fleetRepository.deleteByName("Fleet1");

        // Verifica se o método deleteOne foi chamado com o documento correto
        verify(fleetCollection).deleteOne(expectedQuery);
    }

    @Test
    public void testFindAllPaginated_Success() {
        // Cria um mock de FindIterable para simular a resposta do MongoDB
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        // Cria uma lista de documentos que serão retornados pelo MongoDB
        List<Document> documents = new ArrayList<>();
        documents.add(new Document("name", "Fleet1"));
        documents.add(new Document("name", "Fleet2"));

        // Simula o comportamento do MongoDB find(), skip() e limit()
        when(fleetCollection.find()).thenReturn(mockFindIterable);
        when(mockFindIterable.skip(anyInt())).thenReturn(mockFindIterable);
        when(mockFindIterable.limit(anyInt())).thenReturn(mockFindIterable);

        // Simula o comportamento do forEach no FindIterable
        doAnswer(invocation -> {
            documents.forEach(invocation.getArgument(0));  // Simula a iteração
            return null;
        }).when(mockFindIterable).forEach(any());

        // Simula o comportamento do FleetMapper para mapear documentos para FleetRecord
        when(fleetMapper.mapToFleetRecord(any(Document.class)))
                .thenReturn(new FleetRecord("Fleet1", null, null), new FleetRecord("Fleet2", null, null));

        // Executa o método a ser testado
        List<FleetRecord> result = fleetRepository.findAllPaginated(1);

        // Verifica o resultado esperado
        assertEquals(2, result.size());
        assertEquals("Fleet1", result.get(0).getName());
        assertEquals("Fleet2", result.get(1).getName());

        // Verifica se os métodos skip, limit e forEach foram chamados corretamente
        verify(mockFindIterable).skip((1 - 1) * 10);  // Para a página 1, o skip é 0
        verify(mockFindIterable).limit(10);  // O tamanho da página é 10
        verify(mockFindIterable).forEach(any());  // Verifica a iteração dos resultados
    }
    @Test
    public void testUpdateFleet() {
        // Cria um documento mockado que será atualizado no MongoDB
        Document mockFleetDocument = new Document("name", "Fleet1").append("crew", 10);

        // Cria o filtro esperado com base no nome da frota
        Document expectedFilter = new Document("name", "Fleet1");

        // Cria o documento de atualização esperado
        Document expectedUpdate = new Document("$set", mockFleetDocument);

        // Executa o método updateFleet
        fleetRepository.updateFleet(mockFleetDocument);

        // Verifica se o método updateOne foi chamado com o filtro e o documento de atualização corretos
        verify(fleetCollection).updateOne(expectedFilter, expectedUpdate);
    }
    @Test
    public void testFindAll_Success() {
        // Cria um mock de FindIterable para simular a resposta do MongoDB
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        // Cria uma lista de documentos que serão retornados pelo MongoDB
        List<Document> documents = new ArrayList<>();
        documents.add(new Document("name", "Fleet1"));
        documents.add(new Document("name", "Fleet2"));

        // Simula o comportamento do MongoDB find()
        when(fleetCollection.find()).thenReturn(mockFindIterable);

        // Simula o comportamento do forEach no FindIterable
        doAnswer(invocation -> {
            documents.forEach(invocation.getArgument(0));  // Simula a iteração
            return null;
        }).when(mockFindIterable).forEach(any());

        // Simula o comportamento do FleetMapper para mapear documentos para FleetRecord
        when(fleetMapper.mapToFleetRecord(any(Document.class)))
                .thenReturn(new FleetRecord("Fleet1", null, null), new FleetRecord("Fleet2", null, null));

        // Executa o método a ser testado
        List<FleetRecord> result = fleetRepository.findAll();

        // Verifica o resultado esperado
        assertEquals(2, result.size());
        assertEquals("Fleet1", result.get(0).getName());
        assertEquals("Fleet2", result.get(1).getName());

        // Verifica se o método forEach foi chamado corretamente
        verify(mockFindIterable).forEach(any());
    }

}

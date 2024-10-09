//package repository;
//
//import br.com.swapi.model.FleetRecord;
//import br.com.swapi.mapper.FleetMapper;
//import br.com.swapi.repository.FleetRepository;
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoCollection;
//import org.bson.Document;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class FleetRepositoryTest {
//
//    @Mock
//    private MongoCollection<Document> fleetCollection;
//
//    @Mock
//    private FleetMapper fleetMapper;
//
//    @Mock
//    private FindIterable<Document> findIterable;
//
//    @InjectMocks
//    private FleetRepository fleetRepository;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);  // Inicializa os mocks
//    }
//
//    @Test
//    public void testFindByName_Success() {
//        // Dados de entrada
//        String fleetName = "Teste";
//        Document fleetDocument = new Document("name", fleetName);
//        FleetRecord expectedFleetRecord = new FleetRecord(fleetName,mock.Mock.getMockStarshipRecord(),mock.Mock.getMockCrewRecordFleet());
//
//        // Configura o comportamento dos mocks
//        when(fleetCollection.find(any(Document.class))).thenReturn(findIterable);
//        when(findIterable.first()).thenReturn(fleetDocument);
//        when(fleetMapper.mapToFleetRecord(fleetDocument)).thenReturn(expectedFleetRecord);
//
//        // Executa o método findByName
//        FleetRecord result = fleetRepository.findByName(fleetName);
//
//        // Verifica o resultado
//        assertNotNull(result);
//        assertEquals(expectedFleetRecord, result);
//
//        // Verifica se os métodos mockados foram chamados corretamente
//        verify(fleetCollection, times(1)).find(any(Document.class));
//        verify(findIterable, times(1)).first();
//        verify(fleetMapper, times(1)).mapToFleetRecord(fleetDocument);
//}}

package br.com.swapi.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.net.HttpURLConnection;
import java.net.URL;

class SWAPIClientTest {

    @InjectMocks
    private SWAPIClient swapiClient;


    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        swapiClient = new SWAPIClient();
    }


}

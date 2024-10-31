package com.example.webgraph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GoogleSearchService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.cse.id}")
    private String cseId;

    public String search(String query) {
        // Simula uma busca no Google padrão adicionando parâmetros de refinamento
        String url = "https://www.googleapis.com/customsearch/v1"
                + "?key=" + apiKey
                + "&cx=" + cseId
                + "&q=" + query
                + "&exactTerms=" + query  // Simula aspas em torno do termo da pesquisa
                + "&num=5";  // Reduz o número de resultados para tornar o retorno mais preciso

        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

                // Verifica os resultados para garantir que algum contenha o termo
                for (Map<String, Object> item : items) {
                    String snippet = (String) item.get("snippet");
                    if (snippet != null) {
                        return snippet;
                    }
                }

                // Retorna o snippet do primeiro resultado como último recurso
                return (String) items.get(0).get("snippet");
            }
            return "No result found";
        } catch (Exception e) {
            return "Error: Unable to retrieve information from Google";
        }
    }
}

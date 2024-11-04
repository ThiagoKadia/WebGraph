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
        String url = "https://www.googleapis.com/customsearch/v1"
                + "?key=" + apiKey
                + "&cx=" + cseId
                + "&q=" + query
                + "&exactTerms=" + query
                + "&num=5";

        RestTemplate restTemplate = new RestTemplate();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

                for (Map<String, Object> item : items) {
                    String snippet = (String) item.get("snippet");
                    if (snippet != null) {
                        return snippet;
                    }
                }

                return (String) items.get(0).get("snippet");
            }
            return "No result found";
        } catch (Exception e) {
            return "Error: Unable to retrieve information from Google";
        }
    }
}

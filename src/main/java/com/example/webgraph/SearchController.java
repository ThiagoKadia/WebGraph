package com.example.webgraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private GoogleSearchService googleSearchService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, String>> search(@RequestParam String query, @RequestParam String source) {
        String result = switch (source.toLowerCase()) {
            case "dbpedia" -> fetchDBPediaIntroduction(query);
            case "google" -> googleSearchService.search(query);
            default -> fetchWikipediaIntroduction(query);
        };

        Map<String, String> response = new HashMap<>();
        response.put("extract", result);
        return ResponseEntity.ok(response);
    }

    private String fetchWikipediaIntroduction(String query) {
        String url = "https://pt.wikipedia.org/api/rest_v1/page/summary/" + query;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<WikipediaSummary> response = restTemplate.getForEntity(url, WikipediaSummary.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                WikipediaSummary summary = response.getBody();
                return summary != null ? summary.getExtract() : "No result found";
            } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return fetchClosestWikipediaPage(query);
            } else {
                return "Error: Unexpected response from Wikipedia";
            }
        } catch (RestClientException e) {
            return "Error: Unable to retrieve information from Wikipedia";
        }
    }

    private String fetchClosestWikipediaPage(String query) {
        String searchUrl = "https://pt.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + query + "&format=json";
        RestTemplate restTemplate = new RestTemplate();
        try {
            Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);
            if (response != null && response.containsKey("query")) {
                Map<String, Object> queryMap = (Map<String, Object>) response.get("query");
                List<Map<String, Object>> searchResults = (List<Map<String, Object>>) queryMap.get("search");
                if (searchResults != null && !searchResults.isEmpty()) {
                    String closestTitle = (String) searchResults.get(0).get("title");
                    return fetchWikipediaIntroduction(closestTitle);
                }
            }
            return "No result found";
        } catch (RestClientException e) {
            return "Error: Unable to perform search on Wikipedia";
        }
    }

    private String fetchDBPediaIntroduction(String query) {
        String url = "https://dbpedia.org/data/" + query.replace(" ", "_") + ".json";
        RestTemplate restTemplate = new RestTemplate();
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                String resourceKey = "http://dbpedia.org/resource/" + query.replace(" ", "_");
                if (response.containsKey(resourceKey)) {
                    Map<String, Object> resource = (Map<String, Object>) response.get(resourceKey);
                    if (resource.containsKey("http://dbpedia.org/ontology/abstract")) {
                        List<Map<String, Object>> abstracts = (List<Map<String, Object>>) resource.get("http://dbpedia.org/ontology/abstract");
                        for (Map<String, Object> entry : abstracts) {
                            if ("en".equals(entry.get("lang"))) {
                                return (String) entry.get("value");
                            }
                        }
                    }
                }
            }
            return "No result found";
        } catch (RestClientException e) {
            return "Error: Unable to retrieve information from DBPedia";
        }
    }

    private static class WikipediaSummary {
        private String extract;

        public String getExtract() {
            return extract;
        }
    }
}

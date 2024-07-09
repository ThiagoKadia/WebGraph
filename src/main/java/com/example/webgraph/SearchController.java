package com.example.webgraph;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("searchForm", new SearchForm());
        return "index";
    }

    @PostMapping("/search")
    public String search(@ModelAttribute SearchForm searchForm, Model model) {
        String result = fetchWikipediaIntroduction(searchForm.getQuery());
        model.addAttribute("result", result);
        model.addAttribute("searchForm", searchForm);
        return "index";
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
            return fetchClosestWikipediaPage(query);
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

    private static class WikipediaSummary {
        private String extract;

        public String getExtract() {
            return extract;
        }

        public void setExtract(String extract) {
            this.extract = extract;
        }
    }
}

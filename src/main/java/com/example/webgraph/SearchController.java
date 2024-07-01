package com.example.webgraph;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
                return "No result found";
            } else {
                return "Error: Unexpected response from Wikipedia";
            }
        } catch (RestClientException e) {
            return "Error: Unable to retrieve information from Wikipedia";
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

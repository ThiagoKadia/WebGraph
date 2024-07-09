package com.example.webgraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GraphController {

    @Autowired
    private GraphService graphService;

    @GetMapping("/api/graph")
    public Map<String, Object> getGraph() {
        return graphService.getGraph();
    }
}

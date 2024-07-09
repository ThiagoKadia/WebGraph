package com.example.webgraph;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GraphViewController {

    @GetMapping("/graph")
    public String graphView() {
        return "graph";
    }
}

package com.example.webgraph;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    @Autowired
    private Driver driver;

    public Map<String, Object> getGraph() {
        String cypherQuery = "MATCH (n)-[r]->(m) RETURN n, r, m";
        Set<Map<String, Object>> nodes = new HashSet<>();
        Set<Map<String, Object>> edges = new HashSet<>();

        try (Session session = driver.session()) {
            session.run(cypherQuery).forEachRemaining(record -> {
                Node startNode = record.get("n").asNode();
                Relationship relationship = record.get("r").asRelationship();
                Node endNode = record.get("m").asNode();

                nodes.add(nodeToMap(startNode));
                nodes.add(nodeToMap(endNode));
                edges.add(relationshipToMap(relationship));
            });
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }

    private Map<String, Object> nodeToMap(Node node) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", node.id());
        map.put("labels", node.labels());
        map.put("name", node.get("name").asString());  // Continuar usando "name" para exibição
        map.put("searchText", node.get("searchText").asString());  // Usar "searchText" para a pesquisa
        return map;
    }

    private Map<String, Object> relationshipToMap(Relationship relationship) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", relationship.id());
        map.put("type", relationship.type());
        map.put("startNode", relationship.startNodeId());
        map.put("endNode", relationship.endNodeId());
        relationship.asMap().forEach(map::put);
        return map;
    }
}

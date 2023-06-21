/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.util.*;

/**
 *
 * @author Luka
 */

import java.util.*;

public class DijkstraAlgorithm {
    
    public static Graph calculateMinimumDistanceFromStartingNode(Graph graph, Node startNode) {
        startNode.setDistance(0);

        Set<Node> visitedNodes = new HashSet<>();
        Set<Node> unvisitedNodes = new HashSet<>(graph.getNodes());

        while (!unvisitedNodes.isEmpty()) {
            Node currentNode = getClosestNode(unvisitedNodes);

            unvisitedNodes.remove(currentNode);
            visitedNodes.add(currentNode);

            Map<Node, Integer> adjacentNodes = currentNode.getAllAdjacentNodes();
            for (Node adjacentNode : adjacentNodes.keySet()) {
                if (!visitedNodes.contains(adjacentNode)) {
                    int distance = currentNode.getDistance() + adjacentNodes.get(adjacentNode);
                    if (distance < adjacentNode.getDistance()) {
                        adjacentNode.setDistance(distance);
                        List<Node> shortestPath = new ArrayList<>(currentNode.getShortestPathsToCities());
                        shortestPath.add(currentNode);
                        adjacentNode.setShortestPathsToCities(shortestPath);
                    }
                }
            }
        }
        return graph;
    }

    private static Node getClosestNode(Set<Node> nodes) {
        Node closestNode = null;
        int closestDistance = Integer.MAX_VALUE;

        for (Node node : nodes) {
            int distance = node.getDistance();
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }

        return closestNode;
    }
    
    public static Node getCityNodeWithName(List<Node> connetedCities, String city){
        for (Node n:connetedCities)
            if (n.getName().equals(city))
                return n;
        return null;
    }
    
    public static int getShortestDistance(Graph graph, String destinationCityName){
        for (Node n: graph.getNodes())
            if (n.getName().equals(destinationCityName))
                return n.getDistance();
        return -1;
    }
    
    public static Graph getShortestPathFromSource(List<Node> conneted_cities, Node source) {

            Graph g = new Graph();
            for (Node n:conneted_cities)
                    g.addNode(n);
            g = DijkstraAlgorithm.calculateMinimumDistanceFromStartingNode(g, source);
            return g;
    }
    
}



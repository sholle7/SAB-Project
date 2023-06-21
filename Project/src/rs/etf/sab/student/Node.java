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
class Node {
    private String name;
    private List<Node> shortestPathsToCities = new LinkedList<>();	     
    private Integer distance = Integer.MAX_VALUE;
    private Map<Node, Integer> allAdjacentNodes = new HashMap<>();
    
    public Node(String name) {
        this.name = name;
    }
    
    public void addToAllAdjacentNodes(Node node, int dist) {
        allAdjacentNodes.put(node, dist);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getShortestPathsToCities() {
        return shortestPathsToCities;
    }

    public void setShortestPathsToCities(List<Node> shortestPathsToCities) {
        this.shortestPathsToCities = shortestPathsToCities;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Map<Node, Integer> getAllAdjacentNodes() {
        return allAdjacentNodes;
    }

    public void setAllAdjacentNodes(Map<Node, Integer> allAdjacentNodes) {
        this.allAdjacentNodes = allAdjacentNodes;
    }
	  
    
    
            
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.*;
/**
 *
 * @author Luka
 */
public class sl190204_GeneralOperations implements GeneralOperations{
    
    static private Connection conn=DB.getInstance().getConnection();
    static Calendar currentTime = Calendar.getInstance();

    @Override
    public void setInitialTime(Calendar clndr) {  
       currentTime.setTimeInMillis(clndr.getTimeInMillis());
    }

    @Override
    public Calendar time(int i) {
        long millis = i*24*60*60;
        millis*= 1000;
        
        currentTime.setTimeInMillis(currentTime.getTimeInMillis() + millis);
        
        
        String query1 = "select * from Porudzbina where stanje = ?";
        String query2 = "select idG from Kupac where idK = ?";
        String query3 = "update Porudzbina set idG = ? where idPor = ?";
        String query4 = "update Porudzbina set stanje = ? where idPor = ?";
        String query5 = "select * from Linija";
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setString(1, "sent");
            ResultSet rs1 = ps1.executeQuery();
  
            while(rs1.next()){
                Calendar calendarDateRecieved = Calendar.getInstance();
                Date dateNajbliziGrad = rs1.getDate("datumStigloNajbliziGrad");
                calendarDateRecieved.setTimeInMillis(dateNajbliziGrad.getTime());
                
                if(calendarDateRecieved.before(currentTime)) {
                    // stiglo do najblizeg grada
                    int idPor = rs1.getInt("idPor");
                    int idGNajbliziSource = rs1.getInt("idGNajblizi");
                    int idK = rs1.getInt("idK");
                    int idGKDestination = 0;
                        
                    Calendar calendarDateRecieved1 = Calendar.getInstance();
                    Date dateStigloDoKupca = rs1.getDate("datumStigloDoKupca");
                    calendarDateRecieved1.setTimeInMillis(dateStigloDoKupca.getTime());
                    try(PreparedStatement ps2 = conn.prepareStatement(query2);){
                        ps2.setInt(1, idK);
                        ResultSet rs2 = ps2.executeQuery();
                        if(rs2.next()){
                            idGKDestination = rs2.getInt(1);
                        }                          
                    }
                    
                    
                    if(!calendarDateRecieved1.before(currentTime)) {
                        // putuje od nablizeg grada do grada kupca
                        // racunaj distancu i azuriraj bazu da idG - upisi grad dokle je porudzbina stigla
                        // dijkstra algorithm
                        try(PreparedStatement ps5 = conn.prepareStatement(query5)){
                            ResultSet rs5 = ps5.executeQuery();
                            List<Node> allNodes = new ArrayList<>();
                            
                            while(rs5.next()){
                                int idG1 = rs5.getInt("idG1");
                                int idG2 = rs5.getInt("idG2");
                                int distance = rs5.getInt("razdaljinaUDanima");
                                Node node1 = DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(idG1));
                                Node node2 = DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(idG2));
                                if (node1 == null) node1 = new Node(Integer.toString(idG1));
                                if (node2 == null) node2 = new Node(Integer.toString(idG2));
                                node1.addToAllAdjacentNodes(node2, distance);
                                node2.addToAllAdjacentNodes(node1, distance);

                                if(!allNodes.contains(node1)) allNodes.add(node1);
                                if(!allNodes.contains(node2)) allNodes.add(node2);
                            }
                            Node source = DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(idGNajbliziSource));
                            // graph will contain all nodes and minimum distance from every node to destination node
                            Graph gr = new Graph();
                            gr.setNodes(allNodes);
                            gr = DijkstraAlgorithm.calculateMinimumDistanceFromStartingNode(gr, source);
                            List<Node> nodesFromGraph = gr.getNodes();
                            
                            Node sourceNodeFromGraph = DijkstraAlgorithm.getCityNodeWithName(nodesFromGraph, Integer.toString(idGKDestination));
                            List<Node> shortestPathsToCities = sourceNodeFromGraph.getShortestPathsToCities();
             
                            long differenceInMillis = currentTime.getTimeInMillis() - calendarDateRecieved.getTimeInMillis();
                            int differenceInDays = (int) (differenceInMillis / (24 * 60 * 60 * 1000));
                            
                        
                            int distance = 0;
                            int idGCurrent = idGNajbliziSource;
                            
                            for(int j = shortestPathsToCities.size() - 1; j >=0; j--){
                                Node currentNode = shortestPathsToCities.get(j);
                                int currentDistance = currentNode.getDistance();
                                
                                if(distance == differenceInDays){
                                    try(PreparedStatement ps3 = conn.prepareStatement(query3);){
                                        ps3.setInt(1, idGCurrent);
                                        ps3.setInt(2, idPor);
                                        ps3.executeUpdate();                                        
                                    }
                                    return currentTime;
                                }
                                else {
                                    if(currentDistance + distance <= differenceInDays){
                                        distance +=currentDistance;
                                        idGCurrent = Integer.parseInt(currentNode.getName());
                                    }
                                    else {
                                        try(PreparedStatement ps3 = conn.prepareStatement(query3);){
                                            ps3.setInt(1, idGCurrent);
                                            ps3.setInt(2, idPor);
                                            ps3.executeUpdate();                                        
                                        }
                                        return currentTime;
                                    }
                                }
                            }
  
                        }                   
                    }
                    else{
                        // stigao u grad kupca
                        try(PreparedStatement ps3 = conn.prepareStatement(query3);){
                            ps3.setInt(1, idGKDestination);
                            ps3.setInt(2, idPor);
                            ps3.executeUpdate();
                            try(PreparedStatement ps4 = conn.prepareStatement(query4);){
                                ps4.setString(1, "arrived");
                                ps4.setInt(2, idPor);
                                ps4.executeUpdate();
                            }
                        }
                    }
                }
                else{
                    // nije stiglo do grada najblizeg grada
                }
            }
            
        } catch (Exception ex) {
            return currentTime;
        }
        
        return currentTime;
    }

    @Override
    public Calendar getCurrentTime() {
        return currentTime;
    }

    @Override
    public void eraseAll() {
        try(Statement stmt = conn.createStatement();){
            
            stmt.executeUpdate("delete from Stavka");
            
            stmt.executeUpdate("delete from Artikal");
            
            stmt.executeUpdate("delete from Linija");
            
            stmt.executeUpdate("delete from Transakcija");
            
            stmt.executeUpdate("delete from Prodavnica");
            
            stmt.executeUpdate("delete from Porudzbina");
            
            stmt.executeUpdate("delete from Kupac");
              
            stmt.executeUpdate("delete from Grad");

        } catch (SQLException ex) {
            Logger.getLogger(sl190204_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

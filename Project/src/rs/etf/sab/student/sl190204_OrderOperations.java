/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.math.BigDecimal;
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
import rs.etf.sab.student.Node;
import rs.etf.sab.student.Graph;
import rs.etf.sab.student.DijkstraAlgorithm;

/**
 *
 * @author Luka
 */
public class sl190204_OrderOperations implements OrderOperations{

    static private Connection conn=DB.getInstance().getConnection();
    private GeneralOperations generalOp;
    
    public sl190204_OrderOperations(GeneralOperations generalOp) {
        this.generalOp = generalOp;
    }

    @Override
    public int addArticle(int i, int i1, int i2) {
        // TODO - dodaj proveru da li korisnik ima dovoljno novca
        String query1 = "insert into Stavka (idA, idPor, kolicina) values(?,?,?)";
        String query2 = "select kolicina from Artikal where idA = ?";
        String query3 = "select kolicina from Stavka where idA = ? and idPor = ?";
        String query4 = "update Stavka set kolicina = kolicina + ? where idA = ? and idPor = ?";
        String query5 = "update Artikal set kolicina = kolicina - ? where idA = ?";
        String query6 = "select idS from Stavka where idA = ? and idPor = ?";
        
                           
        int amount = 0;
        int returnValue = 0;
        
        try(PreparedStatement ps2 = conn.prepareStatement(query2);){
            ps2.setInt(1, i1);
            ResultSet rs2 = ps2.executeQuery();
            
            if(rs2.next()){
                amount = rs2.getInt(1);
                if(i2 > amount){
                    return -1;
                }
  
                try(PreparedStatement ps3 = conn.prepareStatement(query3);){
                    ps3.setInt(1, i1);
                    ps3.setInt(2, i);
                    ResultSet rs3 = ps3.executeQuery();
                    
                    if(rs3.next()){
                        try(PreparedStatement ps4 = conn.prepareStatement(query4,PreparedStatement.RETURN_GENERATED_KEYS);){
                            ps4.setInt(1, i2);
                            ps4.setInt(2, i1);
                            ps4.setInt(3, i);
                            ps4.executeUpdate();
                            
                            try (ResultSet rs4 = ps4.getGeneratedKeys();) {
                                if (rs4.next()) {
                                   try(PreparedStatement ps7 = conn.prepareStatement(query6)){
                                        ps7.setInt(1, i1);
                                        ps7.setInt(2, i);   
                                        ResultSet rs7 = ps7.executeQuery();
                                        if(rs7.next()){
                                            returnValue = rs7.getInt(1);
                                            try(PreparedStatement ps5 = conn.prepareStatement(query5);){
                                                ps5.setInt(1, i2);
                                                ps5.setInt(2, i1);                                       
                                                ps5.executeUpdate();
                                                return returnValue;
                                            }
                                        }
                                        else{
                                            return -1;
                                        }
                                   }
                                }
                                else{
                                    return -1;
                                }
                            }catch(Exception ex) {
                                 return -1;
                            }
                        }
                    }
                    
                    else{
                        try(PreparedStatement ps1 = conn.prepareStatement(query1,PreparedStatement.RETURN_GENERATED_KEYS);){
                            ps1.setInt(1, i1);
                            ps1.setInt(2, i);
                            ps1.setInt(3, i2);
                            ps1.executeUpdate();
                            
                            try(ResultSet rs1 = ps1.getGeneratedKeys();) {
                                if(rs1.next()) {
                                    returnValue = rs1.getInt(1);
                                    try(PreparedStatement ps6 = conn.prepareStatement(query5);){
                                        ps6.setInt(1, i2);
                                        ps6.setInt(2, i1);
                                        ps6.executeUpdate();
                                        return returnValue;
                                    }
                                }
                                else{
                                    return -1;
                                }
                            }catch(Exception ex) {
                                 return -1;
                            }
                        }
                    }
                }     
            }
            else{
                return -1;
            }
                   
        } catch (Exception ex) {
           return -1;
        }   
    }

    @Override
    public int removeArticle(int i, int i1) {
        String query1 = "select kolicina from Stavka where idA = ? AND idPor = ?";
        String query2 = "delete from Stavka where idA = ? AND idPor = ?";
	String query3 = "update Artikal set kolicina = kolicina + ? where idA = ?";
        
        int currentAmount = 0;
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1, i1);
            ps1.setInt(2, i);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next()){
                currentAmount = rs1.getInt(1);
                try(PreparedStatement ps2 = conn.prepareStatement(query2);){
                    ps2.setInt(1, i1);
                    ps2.setInt(2, i);
                    ps2.executeUpdate();
                    try(PreparedStatement ps3 = conn.prepareStatement(query3);){
                        ps3.setInt(1, currentAmount);
                        ps3.setInt(2,i1);
                        ps3.executeUpdate();
                        return 1;
                    }
                }
            }
            else{
                return -1;
            }
        } catch (SQLException ex) {
           return -1;
        }
        
    }

    @Override
    public List<Integer> getItems(int i) {     
        String query = "select idS from Stavka where idPor = ?";
        List<Integer> allItems = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                allItems.add(rs.getInt(1));             
            }
            return allItems;
        } catch (Exception ex) {          
            return null;
        }
    }

    @Override
    public int completeOrder(int i) {
       
        String procedureCall = "exec dbo.SP_FINAL_PRICE ?";
        String query1 = "select * from Linija";
        String query2 = "update Porudzbina set stanje = 'sent', datumPoslato = ? where idPor = ?";
        String query3 = "select idG from Kupac where idK = (select idK from Porudzbina where idPor = ?)";
        String query4 = "update Porudzbina set datumStigloDoKupca = DATEADD(day, ?, datumPoslato), idGNajblizi = ?, datumStigloNajbliziGrad = DATEADD(day, ?, datumPoslato) where idPor = ?";
        String query5 = "select p.idG from Prodavnica p join Grad g on (p.idG = g.idG)";
        String query6 = "select g.idG\n" +
                        "from Stavka s join Artikal a on s.idA = a.idA\n" +
                        "join Prodavnica p on a.idPro = p.idPro\n" +
                        "join Grad g on p.idG = g.idG\n" +
                        "where s.idPor = ?";
        
        String query7 = "select sum(discountedPrice)\n" +
                        "from (\n" +
                        "	select cena*s.kolicina * (100 - popust) / 100 AS discountedPrice\n" +
                        "	from Stavka s join Artikal a on s.idA = a.idA\n" +
                        "	join Prodavnica p on a.idPro = p.idPro\n" +
                        "	where s.idPor = ? and popust<>0\n" +
                        "\n" +
                        "	union\n" +
                        "\n" +
                        "	select cena*s.kolicina\n" +
                        "	from Stavka s join Artikal a on s.idA = a.idA\n" +
                        "	join Prodavnica p on a.idPro = p.idPro\n" +
                        "	where s.idPor = ? and popust=0\n" +
                        ") as temporaryTable";
        
        String query8 = "select novac from Kupac where idK = (select idK from Porudzbina where idPor = ?)";
        
        try (CallableStatement cs = conn.prepareCall(procedureCall);
             PreparedStatement ps1 = conn.prepareStatement(query1);
             PreparedStatement ps2 = conn.prepareStatement(query2);
             PreparedStatement ps3 = conn.prepareStatement(query3);
             PreparedStatement ps4 = conn.prepareStatement(query4);
             PreparedStatement ps5 = conn.prepareStatement(query5);
             PreparedStatement ps6 = conn.prepareStatement(query6);
             PreparedStatement ps12 = conn.prepareStatement(query5);){
            
 
            // update date and status of the order
            ps2.setDate(1, new Date(sl190204_GeneralOperations.currentTime.getTimeInMillis()));
            ps2.setInt(2, i);
            ps2.executeUpdate();
            
            try(PreparedStatement ps7 = conn.prepareStatement(query7);){
                ps7.setInt(1, i);
                ps7.setInt(2, i);
                ResultSet rs7 = ps7.executeQuery();
                while(rs7.next()){
                    BigDecimal price = rs7.getBigDecimal(1);
                    try(PreparedStatement ps8 = conn.prepareStatement(query8);){
                        ps8.setInt(1, i);
                        ResultSet rs8 = ps8.executeQuery();
                        while(rs8.next()){
                            BigDecimal money = rs8.getBigDecimal(1);
                            if(price.compareTo(money) == 1) return -1;
                        }
                    }
                }
            }
            
            
            
            // get id of city for buyer
            int destinationCityId = 0;
            ps3.setInt(1, i);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) destinationCityId = rs3.getInt(1);
            else return -1;
            
            // create graph for cities
            ResultSet rs1 = ps1.executeQuery();
            List<Node> allNodes = new ArrayList<>();
            while(rs1.next()){
                int idG1 = rs1.getInt("idG1");
                int idG2 = rs1.getInt("idG2");
                int distance = rs1.getInt("razdaljinaUDanima");
                Node node1 = DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(idG1));
                Node node2 = DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(idG2));
                if (node1 == null) node1 = new Node(Integer.toString(idG1));
                if (node2 == null) node2 = new Node(Integer.toString(idG2));
                node1.addToAllAdjacentNodes(node2, distance);
                node2.addToAllAdjacentNodes(node1, distance);
                
                if(!allNodes.contains(node1)) allNodes.add(node1);
                if(!allNodes.contains(node2)) allNodes.add(node2);
            }
            
            Node source = DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(destinationCityId));
            if (source == null) return -1;
            
            // graph will contain all nodes and minimum distance from every node to destination node
            Graph gr = new Graph();
            gr.setNodes(allNodes);
            gr = DijkstraAlgorithm.calculateMinimumDistanceFromStartingNode(gr, source);
            
            // calculate distances for first step and for full transportation         
            int fullDistance = 0;
            
            // id of nearest city with shop
            int nearestCityId = 0;
            
            // distance to nearest city
            int distanceFirstStep = Integer.MAX_VALUE;
            
            List<Node> allNodesToSend = new ArrayList<>();
            ResultSet rs5 = ps5.executeQuery();
            
            // find nearest city to destination city with shop in it
            while(rs5.next()){
                int idG = rs5.getInt(1);
                
                if (idG == destinationCityId) {
                    nearestCityId = idG;
                    break;
                }
                else {
                    int currentDistance = DijkstraAlgorithm.getShortestDistance(gr, Integer.toString(idG));
                    if (currentDistance < distanceFirstStep){
                        nearestCityId = idG;
                        distanceFirstStep = currentDistance;
                    }
                }
                
            }
            
            ResultSet rs12 = ps12.executeQuery();
            
            // find nearest city to destination city with shop in it
            while(rs12.next()){
                int idG = rs12.getInt(1);
                
                int currentDistance = DijkstraAlgorithm.getShortestDistance(gr, Integer.toString(idG));
                if (currentDistance < distanceFirstStep){                   
                    distanceFirstStep = currentDistance;
                }
                              
            }
            
            
            
            
            ps6.setInt(1, i);
            ResultSet rs6 = ps6.executeQuery();
            while(rs6.next()){
                allNodesToSend.add(DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(rs6.getInt(1))));
            }
            
            Graph gr2 = new Graph();
            gr2.setNodes(allNodes);
            gr2 = DijkstraAlgorithm.calculateMinimumDistanceFromStartingNode(gr2,DijkstraAlgorithm.getCityNodeWithName(allNodes, Integer.toString(nearestCityId)));
            
            int maximumDistance = 0;
            for (Node node : gr2.getNodes()){
                if(allNodesToSend.contains(node)){
                    if (node.getDistance() > maximumDistance)
                        maximumDistance = node.getDistance();
                }
            }
                     
            
            fullDistance = distanceFirstStep + maximumDistance;
   
            
            ps4.setInt(1, fullDistance);
            ps4.setInt(2, nearestCityId);
            ps4.setInt(3, maximumDistance);
            ps4.setInt(4, i);
            
            int returnResult = ps4.executeUpdate();
            
            // call the stored procedure to calculate final price
            cs.setInt(1, i);
            cs.executeUpdate();
            

            if (ps4.getUpdateCount() == 1) {
                return 1;
            }
            

            } catch (Exception ex) {
               return -1;
            }
        
        
        return -1;
    }

    @Override
    public BigDecimal getFinalPrice(int i) {
        String query = "select cenaSaPopustomUkupno from Porudzbina where idPor = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                return rs.getBigDecimal(1).setScale(3);          
            }
        } catch (Exception ex) {          
            return null;
        }
        return null;
    }

    @Override
    public BigDecimal getDiscountSum(int i) {
        String query = "select cenaUkupno, cenaSaPopustomUkupno from Porudzbina where idPor = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                return (rs.getBigDecimal(1).subtract(rs.getBigDecimal(2))).setScale(3);          
            }
        } catch (Exception ex) {          
            return null;
        }
        return null;
    }

    @Override
    public String getState(int i) {
        String query = "select stanje from Porudzbina where idPor = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                return rs.getString(1);          
            }
        } catch (Exception ex) {          
            return null;
        }
        return null;
    }

    @Override
    public Calendar getSentTime(int i) {
        String query = "select datumPoslato from Porudzbina where idPor = ?";
        Calendar currentTime = Calendar.getInstance();

        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                Date date = rs.getDate(1);
                if (date != null) {
                    currentTime.setTimeInMillis(date.getTime());
                    return currentTime;
                }                 
            }
        } catch (Exception ex) {          
            return null;
        }
        return null;
    }

    @Override
    public Calendar getRecievedTime(int i) {
        String query = "select datumStigloDoKupca, stanje from Porudzbina where idPor = ?";
        Calendar currentTime = Calendar.getInstance();

        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){             
                if(rs.getString(2).equals("arrived")){
                    Date date = rs.getDate(1);
                    if (date != null) {
                        currentTime.setTimeInMillis(date.getTime());
                        return currentTime;
                    }    
                }
                else return null;
            }
        } catch (Exception ex) {          
            return null;
        }
        return null;
    }

    @Override
    public int getBuyer(int i) {
        String query = "select idK from Porudzbina where idPor = ?";
        
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                return rs.getInt(1);          
            }
        } catch (Exception ex) {          
            return -1;
        }
        return -1;
    }

    @Override
    public int getLocation(int i) {
        String query1 = "select stanje from Porudzbina where idPor = ?";
        String query2 = "select idGNajblizi from Porudzbina where idPor = ?";
        String query3 = "select datumStigloNajbliziGrad, idG from Porudzbina where idPor = ?";
        
        Calendar currentTime = this.generalOp.getCurrentTime();
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1, i);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next()) {
                if(rs1.getString(1).equals("created"))
                        return -1;
                else {
                    try(PreparedStatement ps3 = conn.prepareStatement(query3);){
                        ps3.setInt(1, i);
                        ResultSet rs3 = ps3.executeQuery();
                        Date d;
                        Calendar calendarDateRecieved = Calendar.getInstance();
                        if(rs3.next()){
                            d = rs3.getDate(1);
                            calendarDateRecieved.setTimeInMillis(d.getTime());
                            if(calendarDateRecieved.before(currentTime)) return rs3.getInt(2);
                            else {
                                try(PreparedStatement ps2 = conn.prepareStatement(query2);){ 
                                    ps2.setInt(1, i);
                                    ResultSet rs2 = ps2.executeQuery();
                                    if(rs2.next())
                                    return rs2.getInt(1);
                                }            
                            }
                        }
                    }            
                }
            }
        } catch (Exception e){
                return -1;
        }
        return -1;
    }
    
}

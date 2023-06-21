/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.*;
/**
 *
 * @author Luka
 */
public class sl190204_CityOperations implements CityOperations{
    
    static private Connection conn=DB.getInstance().getConnection();
    
    @Override
    public int createCity(String string) {
        String query1 = "insert into Grad(naziv) values(?)";
        String query2 = "select * from Grad where naziv = ?";
        
        try(PreparedStatement ps2 = conn.prepareStatement(query2)){
             ps2.setString(1, string);
             ResultSet rs2 = ps2.executeQuery();
             if(rs2.next()){
                return -1;
             }
             

            try(PreparedStatement ps = conn.prepareStatement(query1, PreparedStatement.RETURN_GENERATED_KEYS);){
                ps.setString(1, string);
                ps.executeUpdate();

                try (ResultSet rs1 = ps.getGeneratedKeys();) {
                        if (rs1.next()) {
                            return rs1.getInt(1);
                        }
               }catch(Exception ex) {
                    return -1;
               }

            } catch (Exception ex) {
                 return -1;
            }
        } catch (Exception ex) {
            return -1;
        }
        
        return -1;
    }

    @Override
    public List<Integer> getCities() {
       List<Integer> allCities = new ArrayList<>();
       String query = "select idG from Grad";
       
        try(Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);){
            while(rs.next()){
                allCities.add(rs.getInt(1));             
            }
            return allCities;
        } catch (Exception ex) {          
            return null;
        }
          
    }

    @Override
    public int connectCities(int i, int i1, int i2) {
        String query1 = "insert into Linija(idG1,idG2,razdaljinaUDanima) values (?,?,?)";
        String query2 = "select idL from Linija where (idG1 = ? and idG2 = ?) or (idg1 = ? and idG2 = ?)";
        
        try( PreparedStatement ps2 = conn.prepareStatement(query2);){
           ps2.setInt(1, i);
           ps2.setInt(2, i1);
           ps2.setInt(3, i1);
           ps2.setInt(4, i);
           ResultSet rs2 = ps2.executeQuery();
           
           if(rs2.next()){
               return -1;
           }              
        
           try(PreparedStatement ps1 = conn.prepareStatement(query1,PreparedStatement.RETURN_GENERATED_KEYS);){
               ps1.setInt(1, i);
               ps1.setInt(2, i1);
               ps1.setInt(3, i2);
               ps1.executeUpdate();
               
               try (ResultSet rs1 = ps1.getGeneratedKeys();) {
                    if (rs1.next()) {
                        return rs1.getInt(1);
                    }
                    
               }catch(Exception ex) {
                    
                    return -1;
               }
               
           }
           
        } catch (Exception ex) {     
            return -1;
        }
        
        return -1;
    }

    @Override
    public List<Integer> getConnectedCities(int i) {
      
            String query1 = "select idG1 from Linija where idG2 = ?";
            String query2 = "select idG2 from Linija where idG1 = ?";
            
            List<Integer> returnList = new ArrayList<>();
            
            try(PreparedStatement ps1 = conn.prepareStatement(query1);
                      PreparedStatement ps2 = conn.prepareStatement(query2);) {  
            
            ps1.setInt(1, i);
            ps2.setInt(1, i);
           
            ResultSet rs1 = ps1.executeQuery();
            ResultSet rs2 = ps2.executeQuery();
            
            while (rs1.next())
                returnList.add(rs1.getInt("idG1"));
           
            while (rs2.next())
                returnList.add(rs2.getInt("idG2"));
            
            return returnList;
        } catch (Exception ex) {
           return null;
        }
          
    }

    @Override
    public List<Integer> getShops(int i) {
       List<Integer> allShops = new ArrayList<>();
       String query = "select idPro from Prodavnica where idG = ?";
       
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                allShops.add(rs.getInt(1));             
            }
            return allShops;
        } catch (Exception ex) {          
            return null;
        }
    }
    
}

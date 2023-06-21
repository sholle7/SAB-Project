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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.*;
/**
 *
 * @author Luka
 */
public class sl190204_ShopOperations implements ShopOperations{

    static private Connection conn=DB.getInstance().getConnection();
    
    @Override
    public int createShop(String string, String string1) {
        String query1 = "insert into Prodavnica(idG,naziv,popust,profit) values(?,?,0,0)";
        String query2 = "select idG from Grad where naziv = ?";
        String query3 = "select * from Prodavnica where naziv = ?";
       
        int idG = 0;
        
        try(PreparedStatement ps3 = conn.prepareStatement(query3);){
            
            ps3.setString(1, string);
            ResultSet rs3 = ps3.executeQuery();
            if(rs3.next())
                return -1;
            
            try(PreparedStatement ps2 = conn.prepareStatement(query2);){
               
                ps2.setString(1, string1);
                ResultSet rs2 = ps2.executeQuery();
                
                if(rs2.next()){
                    idG = rs2.getInt(1);
                }
                else{
                    return -1;
                }
                try(PreparedStatement ps1 = conn.prepareStatement(query1,PreparedStatement.RETURN_GENERATED_KEYS);){
                    ps1.setInt(1, idG);
                    ps1.setString(2, string);
                    ps1.executeUpdate();
                    
                    try (ResultSet rs1 = ps1.getGeneratedKeys();) {
                        if (rs1.next()) {
                            return rs1.getInt(1);
                        }
                    }catch(Exception ex) {
                         return -1;
                    }
                }
            }
            
        } catch (Exception ex) {
            return -1;
        }
        return -1;
    }

    @Override
    public int setCity(int i, String string) {
        String query1 = "update Prodavnica set idG = ? where idPro = ?";
        String query2 = "select idG from Grad where naziv = ?";
        int idG;
        try(PreparedStatement ps2 = conn.prepareStatement(query2);){
            ps2.setString(1, string);
            ResultSet rs = ps2.executeQuery();
            if(rs.next()){
                idG = rs.getInt(1);
                 try(PreparedStatement ps1 = conn.prepareStatement(query1);){
                     ps1.setInt(1, idG);
                     ps1.setInt(2, i);
                     ps1.executeUpdate();
                     return 1;
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
    public int getCity(int i) {
        String query = "select idG from Prodavnica where idPro = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (Exception ex) {
           return -1;
        }
        return -1;
    }

    @Override
    public int setDiscount(int i, int i1) {
        String query = "update Prodavnica set popust = ? where idPro = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1, i1);
            ps.setInt(2, i);
            ps.executeUpdate();
            return 1;
        } catch (Exception ex) {
            return -1;
        }
    }

    @Override
    public int increaseArticleCount(int i, int i1) {
        String query1 = "update Artikal set kolicina = kolicina + ? where idA = ?";
        String query2 = "select kolicina from Artikal where idA = ?";
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1, i1);
            ps1.setInt(2, i);
            ps1.executeUpdate();
            
            try(PreparedStatement ps2 = conn.prepareStatement(query2);){
                ps2.setInt(1, i);
                ResultSet rs = ps2.executeQuery();
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
            
            
            return 1;
        } catch (Exception ex) {
            return -1;
        }
    }

    @Override
    public int getArticleCount(int i) {
        String query = "select kolicina from Artikal where idA = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (Exception ex) {
           return -1;
        }
        return -1;
    }

    @Override
    public List<Integer> getArticles(int i) {
        String query = "select idA from Artikal where idPro = ?";
        List<Integer> articlesList = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
               articlesList.add(rs.getInt(1));
            }
            return articlesList;
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public int getDiscount(int i) {
        String query = "select popust from Prodavnica where idPro = ?";
        try(PreparedStatement ps = conn.prepareStatement(query);){
            ps.setInt(1,i);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (Exception ex) {
           return -1;
        }
        return -1;
    }
    
}

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
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.*;
/**
 *
 * @author Luka
 */
public class sl190204_ArticleOperations implements ArticleOperations{
    
    static private Connection conn=DB.getInstance().getConnection();

    @Override
    public int createArticle(int i, String string, int i1) {
        
        String query = "insert into Artikal(idPro, cena, kolicina, naziv) values(?,?,0,?)";
      
        
        try(PreparedStatement ps = conn.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);){
            ps.setInt(1, i);
            ps.setInt(2, i1);
            ps.setString(3, string);
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
        return -1;
        
    }
    
    
    
}

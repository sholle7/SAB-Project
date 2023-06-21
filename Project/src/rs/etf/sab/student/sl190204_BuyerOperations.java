/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.math.BigDecimal;
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
public class sl190204_BuyerOperations implements BuyerOperations{
    
    static private Connection conn=DB.getInstance().getConnection();
    
    @Override
    public int createBuyer(String string, int i) {
        String query = "insert into Kupac(idG, ime, novac) values(?,?,0)";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, i);
            ps.setString(2, string);
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

    @Override
    public int setCity(int i, int i1) {
        String query = "update Kupac set idG = ? where idK = ?";
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, i1);
            ps.setInt(2, i);
            ps.executeUpdate();
            
            return 1;
        
        } catch (Exception ex) {
           return -1;
        }
    }

    @Override
    public int getCity(int i) {
        String query = "select idG from Kupac where idK = ?";
        try(PreparedStatement ps = conn.prepareStatement(query)){
           ps.setInt(1, i);
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
    public BigDecimal increaseCredit(int i, BigDecimal bd) {
        String query1 = "update Kupac set novac = novac + ? where idK = ?";
        String query2 = "select novac from Kupac where idK = ?";

        try(PreparedStatement ps1 = conn.prepareStatement(query1)){
          ps1.setBigDecimal(1, bd);
          ps1.setInt(2, i);
          ps1.executeUpdate();
          try(PreparedStatement ps2 = conn.prepareStatement(query2)){
              ps2.setInt(1, i);
              ResultSet rs = ps2.executeQuery();
              if(rs.next()){
                  return rs.getBigDecimal(1);
              }

          }catch (Exception ex) {
                 return null;
          }

        } catch (Exception ex) {
           return null;
        }
        return null;
    }

    @Override
    public int createOrder(int i) {
        String query = "insert into Porudzbina(idK, stanje) values(?, ?)";
        try(PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, i);
            ps.setString(2, "created");
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

    @Override
    public List<Integer> getOrders(int i) {
       String query = "select idPor from Porudzbina where idK = ?";
       List<Integer> allOrders = new ArrayList<>();
       try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                allOrders.add(rs.getInt(1));
            }
            return allOrders;

       } catch (Exception ex) {
          return null;
       }
    }

    @Override
    public BigDecimal getCredit(int i) {
       String query = "select novac from Kupac where idK = ?";
       try(PreparedStatement ps = conn.prepareStatement(query)){
              ps.setInt(1, i);
              ResultSet rs = ps.executeQuery();
              if(rs.next()){
                  return rs.getBigDecimal(1);
              }

          }catch (Exception ex) {
                 return null;
          }
       return null;
    }
    
}

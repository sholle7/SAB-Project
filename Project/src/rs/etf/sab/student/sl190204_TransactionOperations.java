/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class sl190204_TransactionOperations implements TransactionOperations{

    static private Connection conn=DB.getInstance().getConnection();
     
    @Override
    public BigDecimal getBuyerTransactionsAmmount(int i) {
        String query1 = "select novac from Transakcija where idK = ?";
        double sum = 0;
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
           ps1.setInt(1,i);
           ResultSet rs1 = ps1.executeQuery();
           while(rs1.next()){
              sum += rs1.getDouble(1);

           }         
           return new BigDecimal(sum).setScale(3);
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int i) {
        String query1 = "select novac from Transakcija where idPro = ?";
        double sum = 0;
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
           ps1.setInt(1,i);
           ResultSet rs1 = ps1.executeQuery();
           while(rs1.next()){
              sum += (rs1.getDouble(1));
           }

           return new BigDecimal(sum).setScale(3);
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public List<Integer> getTransationsForBuyer(int i) {
        String query1 = "select idT from Transakcija where idK = ?";
        List<Integer> transactionsList = new ArrayList<>();
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1,i);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
               transactionsList.add(rs1.getInt(1));
            }
            if(transactionsList.size() == 0) return null;
            else return transactionsList;
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public int getTransactionForBuyersOrder(int i) {
        String query1 = "select idT from Transakcija where idPor = ?";
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1,i);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next()){
                return rs1.getInt(1);
            }
            
        } catch (Exception ex) {
           return -1;
        }
        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int i, int i1) {
        String query1 = "select idT from Transakcija where idPor = ? and idPro = ?";
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1,i);
            ps1.setInt(2,i1);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next()){
                return rs1.getInt(1);
            }
            
        } catch (Exception ex) {
           return -1;
        }
        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int i) {
        String query1 = "select idT from Transakcija where idPro = ?";
        List<Integer> transactionsList = new ArrayList<>();
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1,i);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
               transactionsList.add(rs1.getInt(1));
            }
            if(transactionsList.size() == 0) return null;
            else return transactionsList;
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public Calendar getTimeOfExecution(int i) {
        String query1 = "select datum from Transakcija where idT = ?";
        
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
            ps1.setInt(1, i);
            ResultSet rs1 = ps1.executeQuery();
            Calendar calendar = Calendar.getInstance();
            
            if(rs1.next()){
                Date date = rs1.getDate(1);             
                calendar.setTimeInMillis(date.getTime());
                return calendar;               
            }
            
        } catch (Exception ex) {
            return null;
        }
        return null;        
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int i) {
        String query1 = "select novac from Transakcija where idPor = ?";
        double sum = 0;
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
           ps1.setInt(1,i);
           ResultSet rs1 = ps1.executeQuery();
           while(rs1.next()){
              sum += (rs1.getDouble(1));
           }
           return new BigDecimal(sum).setScale(3);
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int i, int i1) {
        String query1 = "select novac from Transakcija where idPro = ? and idPor = ?";
        double sum = 0;
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
           ps1.setInt(1,i);
           ps1.setInt(2,i1);
           ResultSet rs1 = ps1.executeQuery();
           
           if(rs1.next()){
              sum += (rs1.getDouble(1) * 0.95);
           }
           
           return new BigDecimal(sum).setScale(3);
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public BigDecimal getTransactionAmount(int i) {
        String query1 = "select novac from Transakcija where idT = ?";
        double sum = 0;
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){
           ps1.setInt(1,i);
           ResultSet rs1 = ps1.executeQuery();
           
           if(rs1.next()){
              sum += (rs1.getDouble(1));
           }

           return new BigDecimal(sum).setScale(3);
        } catch (Exception ex) {
           return null;
        }
    }

    @Override
    public BigDecimal getSystemProfit() {
        String query1 = "select coalesce(sum(t.sistem),0) from transakcija t join Porudzbina p on (t.idPor = p.idPor)\n" +
        "where p.stanje = 'arrived'";
        double sum = 0;
        try(PreparedStatement ps1 = conn.prepareStatement(query1);){        
           ResultSet rs1 = ps1.executeQuery();
           
           if(rs1.next()){
              sum += (rs1.getDouble(1));
           }
           
           return new BigDecimal(sum).setScale(3);
        } catch (Exception ex) {
           return null;
        }
    }
    
}

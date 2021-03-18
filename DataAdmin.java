package com.test;

import com.sun.nio.file.ExtendedWatchEventModifier;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DataAdmin {

    private static final String INSERT_CHAIN_SQL = "INSERT INTO public.hotelchain" +
            "  (chain_id,total_hotels,phone_number,email_address,chain_name,office_location) VALUES " +
            " (?,?,?,?,?,?);";
    private static final String INSERT_HOTEL_SQL = "INSERT INTO public.hotel" +
            "  (hotel_id,chain_id,phone_number,hotel_name, star_category, room_count, email_address,e_sin_number) VALUES " +
            " (?,?,?,?,?,?,?,?);";
    private static final String INSERT_EMP_SQL = "INSERT INTO public.employee" +
            "  (e_sin_number,position,full_name,address,salary) VALUES " +
            " (?,?,?,?,?);";
    private static final String INSERT_EMPS_SQL = "INSERT INTO public.employs" +
            "  (hotel_id,e_sin_number) VALUES " +
            " (?,?);";

    private Connection dbConn;

    public DataAdmin(){
        try {
            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25", "bwils088", "Wade150318!");
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void addHotelChain(int totHotels, String phoneNum, String email, String name, String location){
        try{
            Statement st = dbConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(chain_id) FROM public.hotelchain");
            int id = 0;
            while(rs.next()){
                id = rs.getInt(1)+1;
            }
            rs.close();
            st.close();
            PreparedStatement pst = dbConn.prepareStatement(INSERT_CHAIN_SQL);
            pst.setInt(1,id);
            pst.setInt(2,totHotels);
            pst.setString(3,phoneNum);
            pst.setString(4,email);
            pst.setString(5,name);
            pst.setString(6,location);
            pst.executeUpdate();
            pst.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void updateHotelChain(String attribute, int toUpdate, String value){
        String SQL = "UPDATE public.hotelchain "
                + "SET "+attribute+" = ? "
                + "WHERE chain_id = ?";
        try {
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.setString(1,value);
            pst.setInt(2,toUpdate);
            pst.executeUpdate();
            pst.close();
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public void addHotel(int chain_id, String phoneNum, String name, int category, int count, String email, int eSin){
        try{
            Statement st = dbConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(hotel_id) FROM public.hotel");
            int id = 0;
            while(rs.next()){
                id = rs.getInt(1)+1;
            }
            rs.close();
            st.close();
            PreparedStatement pst = dbConn.prepareStatement(INSERT_HOTEL_SQL);
            pst.setInt(1,id);
            pst.setInt(2,chain_id);
            pst.setString(3,phoneNum);
            pst.setString(4,name);
            pst.setInt(5,category);
            pst.setInt(6,count);
            pst.setString(7,email);
            pst.setInt(8,eSin);
            pst.executeUpdate();
            pst.close();
            PreparedStatement pst2 = dbConn.prepareStatement(INSERT_EMPS_SQL);
            pst2.setInt(1,id);
            pst2.setInt(2,eSin);
            pst2.executeUpdate();
            pst2.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void addEmployee(int eSin, String pos, String name, String address, int salary){
        try{
            PreparedStatement pst = dbConn.prepareStatement(INSERT_EMP_SQL);
            pst.setInt(1,eSin);
            pst.setString(2,pos);
            pst.setString(3,name);
            pst.setString(4,address);
            pst.setInt(5,salary);
            pst.executeUpdate();
            pst.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void updateEmployee(String attribute, int toUpdate, String value){
        String SQL = "UPDATE public.employee "
                + "SET "+attribute+" = ? "
                + "WHERE e_sin_number = ?";
        try {
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.setString(1,value);
            pst.setInt(2,toUpdate);
            pst.executeUpdate();
            pst.close();
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public void deleteEmployee(int e_sin_number){
        String SQL = "DELETE FROM public.employee WHERE e_sin_number = ?";
        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.setInt(1,e_sin_number);
            pst.executeUpdate();
            pst.close();
            String SQL2 = "DELETE FROM public.employs WHERE e_sin_number = ?";
            PreparedStatement pst2 = dbConn.prepareStatement(SQL2);
            pst2.setInt(1,e_sin_number);
            pst2.executeUpdate();
            pst2.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void addEmploys(int hotel_id, int eSin){
        try{
            PreparedStatement pst = dbConn.prepareStatement(INSERT_EMPS_SQL);
            pst.setInt(1,hotel_id);
            pst.setInt(2,eSin);
            pst.executeUpdate();
            pst.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }

}

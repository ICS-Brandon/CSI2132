package com.test;

import com.sun.nio.file.ExtendedWatchEventModifier;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ExecutionException;

/*
    TODO Add Logic Loops for input as well as checks to ensure valid input values
    TODO Implement functionality for Rooms and Customers databases
 */


public class DataAdmin {

    //Insertion Strings
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

    //Constructor for DataAdmin Actor
    public DataAdmin(){
        try {
            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25", "bwils088", "Wade150318!");
        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Add a hotel chain
    public void addHotelChain(int totHotels, String phoneNum, String email, String name, String location){

        //Try Catch
        try{

            //Create query to get max ID in table and increment
            Statement st = dbConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(chain_id) FROM public.hotelchain");
            int id = 0;
            while(rs.next()){
                id = rs.getInt(1)+1;
            }
            rs.close();
            st.close();

            //Create query and input values to push to database
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

    //Update a hotel chain
    public void updateHotelChain(String attribute, int toUpdate, String value){

        //Query string
        String SQL = "UPDATE public.hotelchain "
                + "SET "+attribute+" = ? "
                + "WHERE chain_id = ?";
        try {

            //Create Query and insert values to update entry in database
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.setString(1,value);
            pst.setInt(2,toUpdate);
            pst.executeUpdate();
            pst.close();

        } catch (Exception e){
            System.out.println(e);
        }
    }

    //Add a hotel
    public void addHotel(int chain_id, String phoneNum, String name, int category, int count, String email, int eSin){

        try{

            //Query to check if there exists an employee with the given sin number
            PreparedStatement checkSin = dbConn.prepareStatement("SELECT * FROM public.employee WHERE e_sin_number = ?");
            checkSin.setInt(1,eSin);
            ResultSet validSin = checkSin.executeQuery();

            //If the Sin number is valid then execute query
            if(validSin.next()){

                validSin.close();
                checkSin.close();

                //Create query to get id from table and increment
                Statement st = dbConn.createStatement();
                ResultSet rs = st.executeQuery("SELECT MAX(hotel_id) FROM public.hotel");
                int id = 0;
                if(rs.next()){
                    id = rs.getInt(1)+1;
                }
                else{
                    id = 1;
                }
                rs.close();
                st.close();

                //Create query to push addition to database
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

                //Create query to push addition (Relation) to database
                PreparedStatement pst2 = dbConn.prepareStatement(INSERT_EMPS_SQL);
                pst2.setInt(1,id);
                pst2.setInt(2,eSin);
                pst2.executeUpdate();
                pst2.close();

            } else{
                throw new Exception("error: employee sin number not found in database");
            }

        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Update a hotel
    public void updateHotel(int id, String toUpdate, String value){

        //Query String
        String SQL = "UPDATE public.hotel "
                + "SET "+toUpdate+" = ? "
                + "WHERE hotel_id = ?";
        try {

            //If a hotel with the given id exists execute the query
            if(checkHotelExists(id)) {

                //Create query to update row in database
                PreparedStatement pst = dbConn.prepareStatement(SQL);
                pst.setInt(2, id);
                pst.setString(1, value);
                pst.executeUpdate();
                pst.close();

            } else{
                throw new Exception("error: hotel does not exist");
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void deleteHotel(int id){

        //Query String
        String SQL = "SELECT * FROM public.hotel WHERE hotel_id  = ?";

        try{

            //Execute query if hotel exists with given id
            if(checkHotelExists(id)){

                //Query String
                SQL = "DELETE FROM public.employs WHERE hotel_id = ?";
                //Create query and insert values to delete from database
                PreparedStatement delEmps = dbConn.prepareStatement(SQL);
                delEmps.setInt(1,id);
                delEmps.executeUpdate();
                delEmps.close();

                //Query String
                SQL = "DELETE FROM public.hotel WHERE hotel_id = ?";
                //Create query and insert values to delete from database
                PreparedStatement delHot = dbConn.prepareStatement(SQL);
                delHot.setInt(1,id);
                delHot.executeUpdate();
                delHot.close();

            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    //Add an employee
    public void addEmployee(int eSin, String pos, String name, String address, int salary){

        try{

            //Create query and insert values to push addition to database
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

    //Update an employee
    public void updateEmployee(String attribute, int toUpdate, String value){

        //Query String
        String SQL = "UPDATE public.employee SET "+attribute+" = ? WHERE e_sin_number = ?";

        try {

            //Create query and insert information to update database
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.setString(1,value);
            pst.setInt(2,toUpdate);
            pst.executeUpdate();
            pst.close();

        } catch (Exception e){
            System.out.println(e);
        }
    }

    //Delete an Employee
    public void deleteEmployee(int e_sin_number){

        //Query String
        String SQL = "DELETE FROM public.employee WHERE e_sin_number = ?";
        String SQL2 = "DELETE FROM public.employs WHERE e_sin_number = ?";

        try{

            //Create query and insert value to delete entry from database
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.setInt(1,e_sin_number);
            pst.executeUpdate();
            pst.close();

            //Create query and insert value to delete entry from database
            PreparedStatement pst2 = dbConn.prepareStatement(SQL2);
            pst2.setInt(1,e_sin_number);
            pst2.executeUpdate();
            pst2.close();

        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Add an employs relation
    public void addEmploys(int hotel_id, int eSin){

        try{

            //Create query and insert values to push addition to database
            PreparedStatement pst = dbConn.prepareStatement(INSERT_EMPS_SQL);
            pst.setInt(1,hotel_id);
            pst.setInt(2,eSin);
            pst.executeUpdate();
            pst.close();

        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Update entry in employs relation
    public void updateEmploys(int hotelId, int eSin, String toUpdate, int value){

        try{

            //Execute query if the employs relation exists
            if(checkEmploysExists(hotelId,eSin)){

                //Create query and insert data to update entry in database
                PreparedStatement pst = dbConn.prepareStatement("UPDATE public.employs SET "+toUpdate+" = ? WHERE hotel_id = ? AND e_sin_number = ?");
                pst.setInt(1,value);
                pst.setInt(2,hotelId);
                pst.setInt(3,eSin);
                pst.executeUpdate();
                pst.close();

            } else {
                throw new Exception("error: employee sin number does not exist");
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Delete an employs relation
    public void deleteEmploys(int hotelId, int eSin){

        try{

            //Execute query if employs relation exists
            if(checkEmploysExists(hotelId,eSin)){

                //Create query and insert values to delete entry from database
                PreparedStatement delEmploys = dbConn.prepareStatement("DELETE FROM public.employs WHERE hotel_id = ? AND e_sin_number = ?");
                delEmploys.setInt(1,hotelId);
                delEmploys.setInt(2,eSin);
                delEmploys.executeUpdate();
                delEmploys.close();

            } else {
                throw new Exception("error: employs relation with values specified does not exist");
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Check if a given hotel exists
    public boolean checkHotelExists(int hotelId){

        //Query String
        String SQL = "SELECT hotel_id FROM public.hotel WHERE hotel_id = ?";

        try {

            //Execute query and store in ResultSet
            PreparedStatement hotelExists = dbConn.prepareStatement(SQL);
            hotelExists.setInt(1,hotelId);
            ResultSet validHotel  = hotelExists.executeQuery();

            //If ResultSet not empty return true, else false
            if(validHotel.next()){
                return true;
            } else{
                return false;
            }

        } catch (Exception e){
            System.out.println(e);
        }
        return false;
    }

    public boolean checkEmploysExists(int hotelId, int eSin){

        //Query String
        String SQL = "SELECT * FROM public.employs WHERE hotel_id = ? AND e_sin_number = ?";

        try {

            //Execute query and store in ResultSet
            PreparedStatement employsExists = dbConn.prepareStatement(SQL);
            employsExists.setInt(1,hotelId);
            employsExists.setInt(2,eSin);
            ResultSet validEmploys = employsExists.executeQuery();

            //If ResultSet not empty return true, else false
            if(validEmploys.next()){
                return true;
            } else{
                return false;
            }
            
        } catch(Exception e){
            System.out.println(e);
        }
        return false;
    }

}

package com.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Employee {


    private Connection dbConn;
    private int e_Sin_Number, hotelId;

    public Employee(int eSin, int hotId){
        try{
            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","","");
            e_Sin_Number = eSin;
            hotelId = hotId;
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public void roomsAvailable(){

        //search rooms and select only those with is_rented false

        String SQL = "SELECT * FROM public.room WHERE hotel_id = " + hotelId + " AND is_rented = false";

        try{

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            System.out.println("Room Number | Price | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
            while(rs.next()){
                System.out.println(rs.getInt(3) + "\t" + rs.getInt(4) + "\t" +
                        boolToString(rs.getBoolean(5)) + "\t" + boolToString(rs.getBoolean(6)) + "\t" +
                        boolToString(rs.getBoolean(7)) + "\t" + boolToString(rs.getBoolean(8)) + "\t" +
                        boolToString(rs.getBoolean(9)) + "\t" + rs.getInt(11) + "\t" + intToViewType(rs.getInt(12)));
            }

        } catch (Exception e){
            System.out.println(e);
        }

    }

    public void roomsBooked(){

        String SQL = "SELECT * FROM public.room WHERE hotel_id = "+hotelId+" AND is_rented = true";

        try {

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();

            while(rs.next()){
                ResultSet booking = getBooking(rs.getInt(1));
                //TODO print out relavent info
            }

        } catch(Exception e){
            System.out.println(e);
        }

    }

    public ResultSet getBooking(int roomId){
        try {
            String booking = "SELECT * FROM public.booking WHERE room_id = " + String.valueOf(roomId);
            PreparedStatement getBooking = dbConn.prepareStatement(booking);
            ResultSet validBookings = getBooking.executeQuery();
            return validBookings;
        } catch(Exception e){
            System.out.println(e);
        }
        return null;
    }



    public String boolToString(boolean bool){
        return bool == true ? "Yes" : "No";
    }

    public String intToViewType(int type){
        return type == 1 ? "Sea" : "mountain";
    }
}

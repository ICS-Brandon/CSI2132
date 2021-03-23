package com.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Employee {


    private Connection dbConn;
    private int e_Sin_Number, hotelId;

    private static final String INSERT_RENT_SQL = "INSERT INTO public.rental" +
            "  (rental_id,room_id,occupant_total,start_date,end_date,payment_id,is_cancelled,c_sin_number) VALUES " +
            " (?,?,?,?,?,?,?,?);";

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

    public void rentRoom(int bookingId, int cSin){

        String getRoom = "SELECT * FROM public.booking WHERE booking_id = "+String.valueOf(bookingId)+" AND c_sin_nuber = "+cSin;

        try{

            PreparedStatement getBooking = dbConn.prepareStatement(getRoom);
            ResultSet validBooking = getBooking.executeQuery();

            if(validBooking.next()){
                PreparedStatement createRental = dbConn.prepareStatement(INSERT_RENT_SQL);
                createRental.setInt(1,getMaxRentId());
                createRental.setInt(2,validBooking.getInt((2)));
                createRental.setInt(3,validBooking.getInt((3)));
                createRental.setDate(4,validBooking.getDate(4));
                createRental.setDate(5,validBooking.getDate(5));
                createRental.setInt(6,validBooking.getInt(6));
                createRental.setBoolean(7,validBooking.getBoolean((7)));
                createRental.setInt(8,cSin);

                deleteBooking(bookingId,cSin);
            }


        } catch (Exception e){
            System.out.println(e);
        }

    }

    public void deleteBooking(int bookingId, int cSin){

        String SQL = "DELETE FROM public.booking WHERE booking_id = "+String.valueOf(bookingId)+" AND c_sin_number = "+String.valueOf(cSin);

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.executeQuery();
        } catch(Exception e){
            System.out.println(e);
        }
    }



    public String boolToString(boolean bool){
        return bool == true ? "Yes" : "No";
    }

    public String intToViewType(int type){
        return type == 1 ? "Sea" : "mountain";
    }

    public int getMaxRentId(){
        String SQL = "SELECT MAX(rental_id) FROM public.rental";

        try{

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return rs.getInt(1)+1;
            }
            else{
                return 1;
            }

        } catch(Exception e){
            System.out.println(e);
        }

        return 1;
    }
}

package com.test;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Customer {

    private Connection dbConn;

    private static final String DATETEMPLATE = "dd/MM/yyyy";

    private static final String INSERT_BOOK_SQL = "INSERT INTO public.booking" +
            "  (booking_id,room_id,occupant_total,start_date,end_date,payment_id,is_cancelled,c_sin_number) VALUES " +
            " (?,?,?,?,?,?,?,?);";

    public Customer(){

        try{

            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","","");

        } catch (Exception e){
            System.out.println(e);
        }

    }

    public void rentRoom(String date, String dateTwo, int roomId,int people, int payId, boolean cancelled, int cSin){
        LocalDate start = getDateFromString(date);
        LocalDate end = getDateFromString(dateTwo);

        if(!checkBookingExists(roomId, start, end) && !checkRentalExists(roomId,start,end)){
            System.out.println("Room is not booked or rented for the date range you have chosen, book it?");
            //TODO Update Room to rented status
            try {
                PreparedStatement bookRoom = dbConn.prepareStatement(INSERT_BOOK_SQL);
                PreparedStatement maxId = dbConn.prepareStatement("SELECT MAX(booking_id) FROM public.booking");
                ResultSet rs = maxId.executeQuery();
                int id = 1;
                if(rs.next()){
                    id = rs.getInt(1) + 1;
                }
                bookRoom.setInt(1,id);
                bookRoom.setInt(2,roomId);
                bookRoom.setInt(3,people);
                bookRoom.setDate(4, Date.valueOf(start));
                bookRoom.setDate(5,Date.valueOf(end));
                bookRoom.setInt(6,payId);
                bookRoom.setBoolean(7,cancelled);
                bookRoom.setInt(8,cSin);
                bookRoom.executeUpdate();
                maxId.close();
                bookRoom.close();
                rs.close();
            } catch (Exception e){
                System.out.println(e);
            }
        }

    }

    public void searchByHotelName(String hName){

        try {
            PreparedStatement checkHotel = dbConn.prepareStatement("SELECT hotel_id FROM public.hotel WHERE hotel_name = " + hName);
            ResultSet validHotel = checkHotel.executeQuery();
            if(validHotel.next()){
                int hotelId = validHotel.getInt(1);
                validHotel.close();
                checkHotel.close();
                PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE hotel_id = "+hotelId);
                ResultSet roomList = getRooms.executeQuery();
                System.out.println("Results for room at: "+hName);
                System.out.println("Room Number | Price | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
                while(roomList.next()){
                    if(roomList.getBoolean(10) == false) {
                        System.out.println(roomList.getInt(3) + "\t" + roomList.getInt(4) + "\t" +
                        boolToString(roomList.getBoolean(5)) + "\t" + boolToString(roomList.getBoolean(6)) + "\t" +
                        boolToString(roomList.getBoolean(7)) + "\t" + boolToString(roomList.getBoolean(8)) + "\t" +
                        boolToString(roomList.getBoolean(9)) + "\t" + roomList.getInt(11) + "\t" + intToViewType(roomList.getInt(12)));
                    }
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }

    }

    public void searchByCapacity(int size){

        try{

            PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE room_capacity = "+ size);
            ResultSet roomList = getRooms.executeQuery();

            System.out.println("Results for rooms with a capacity of: "+size);
            System.out.println("Room Number | Price | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
            while(roomList.next()){
                if(roomList.getBoolean(10) == false) {
                    System.out.println(roomList.getInt(3) + "\t" + roomList.getInt(4) + "\t" +
                            boolToString(roomList.getBoolean(5)) + "\t" + boolToString(roomList.getBoolean(6)) + "\t" +
                            boolToString(roomList.getBoolean(7)) + "\t" + boolToString(roomList.getBoolean(8)) + "\t" +
                            boolToString(roomList.getBoolean(9)) + "\t" + intToViewType(roomList.getInt(12)));
                }
            }

        } catch (Exception e){
            System.out.println(e);
        }
    }

    public LocalDate getDateFromString(String input){
        DateTimeFormatter dTFormat = DateTimeFormatter.ofPattern(DATETEMPLATE);
        CharSequence text;
        LocalDate localDate = LocalDate.parse(input, dTFormat);
        return localDate;
    }

    public boolean checkBookingExists(int roomId, LocalDate date1, LocalDate date2){
        String SQL = "SELECT * FROM public.booking WHERE room_id = "+roomId+" AND (start_date >= "+date1+" AND start_date <="+date2+" AND end_date <="+date2+" AND end_date >="+date1+")";

        try{

            PreparedStatement checkBooking = dbConn.prepareStatement(SQL);
            ResultSet validBooking = checkBooking.executeQuery();

            if(validBooking.next()){
                return true;
            } else{
                return false;
            }

        } catch (Exception e){
            System.out.println(e);
        }
        return false;
    }

    public boolean checkRentalExists(int roomId,LocalDate date1, LocalDate date2){

        String SQL = "SELECT * FROM public.rental WHERE room_id = "+roomId+" AND (start_date >= "+date1+" AND start_date <="+date2+" AND end_date <="+date2+" AND end_date >="+date1+")";

        try{

            PreparedStatement checkRental = dbConn.prepareStatement(SQL);
            ResultSet validRental = checkRental.executeQuery();

            if(validRental.next()){
                return true;
            } else{
                return false;
            }

        } catch (Exception e){
            System.out.println(e);
        }
        return false;
    }

    public String boolToString(boolean bool){
        return bool == true ? "Yes" : "No";
    }

    public String intToViewType(int type){
        return type == 0 ? "Sea" : "Mountain";
    }

}

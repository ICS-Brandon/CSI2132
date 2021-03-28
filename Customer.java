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

    private static final String INSERT_PAY_SQL = "INSERT INTO public.paymentinformation"+
            " (payment_id, payment_type, amount_paid, date_paid, booking_id) VALUES " +
            "(?,?,?,?,?)";

    public Customer(){

        try{

            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","","");

        } catch (Exception e){
            System.out.println(e);
        }

    }

    //Function used by customer to book a room, needs several inputs
    //Undecided if the inputs will come before function call or during
    public void bookRoom(String date, String dateTwo, int roomId, int people, int price, boolean cancelled, int cSin){

        //Get start and end date based on a format
        LocalDate start = getDateFromString(date);
        LocalDate end = getDateFromString(dateTwo);

        //Check if a booking or rental exists for the desired rental period
        if(!checkBookingExists(roomId, start, end) && !checkRentalExists(roomId,start,end)){
            System.out.println("Room is not booked or rented for the date range you have chosen, book it?");
            try {
                //Get a prepared statement to insert a booking into its table
                PreparedStatement bookRoom = dbConn.prepareStatement(INSERT_BOOK_SQL);
                //Get a prepared statement to get max booking id from the table
                PreparedStatement maxId = dbConn.prepareStatement("SELECT MAX(booking_id) FROM public.booking");
                ResultSet rs = maxId.executeQuery();
                //Default value of id if query returns null
                int id = 1;
                if(rs.next()){
                    id = rs.getInt(1) + 1;
                }
                //Get payment id from customer
                //Then insert all values into booking table and execute
                int payId = getUserPayment(price,id);
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

    //Search for rooms based on the hotel name
    public void searchByHotelName(String hName){

        try {
            //Prepare statement to get all hotel ids for hotels with the name given by the user
            PreparedStatement checkHotel = dbConn.prepareStatement("SELECT hotel_id FROM public.hotel WHERE hotel_name = " + hName);
            ResultSet validHotel = checkHotel.executeQuery();
            //If query does not return null get all rooms for all hotel ids
            if(validHotel.next()){
                int hotelId = validHotel.getInt(1);
                validHotel.close();
                checkHotel.close();
                PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE hotel_id = "+hotelId);
                ResultSet roomList = getRooms.executeQuery();
                //Display room results to customer
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

    //Search for rooms based on the number of possible tenants
    public void searchByCapacity(int size){

        try{
            //Prepared statement to get all rooms with a given capacity
            PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE room_capacity = "+ size);
            ResultSet roomList = getRooms.executeQuery();
            //Dispaly room results to customer
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

    //Get formatted date from string input of customer
    public LocalDate getDateFromString(String input){
        DateTimeFormatter dTFormat = DateTimeFormatter.ofPattern(DATETEMPLATE);
        CharSequence text;
        LocalDate localDate = LocalDate.parse(input, dTFormat);
        return localDate;
    }

    //Check if a booking exists during a given time window
    public boolean checkBookingExists(int roomId, LocalDate date1, LocalDate date2){
        String SQL = "SELECT * FROM public.booking WHERE room_id = "+roomId+" AND (start_date >= "+date1+" AND start_date <="+date2+" AND end_date <="+date2+" AND end_date >="+date1+")";

        try{
            //Prepared statement to see if any bookings exists
            PreparedStatement checkBooking = dbConn.prepareStatement(SQL);
            ResultSet validBooking = checkBooking.executeQuery();

            //If exists return true, else false
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

    //Check if a rental exists during a given time window
    public boolean checkRentalExists(int roomId,LocalDate date1, LocalDate date2){

        String SQL = "SELECT * FROM public.rental WHERE room_id = "+roomId+" AND (start_date >= "+date1+" AND start_date <="+date2+" AND end_date <="+date2+" AND end_date >="+date1+")";

        try{
            //Prepared statement to see if any booking exists
            PreparedStatement checkRental = dbConn.prepareStatement(SQL);
            ResultSet validRental = checkRental.executeQuery();
            //If exists return true, else false
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

    //Converts boolean value to string value to easily display to customer
    public String boolToString(boolean bool){
        return bool == true ? "Yes" : "No";
    }

    //Converts integer value to string value to easily display to customer
    public String intToViewType(int type){
        return type == 0 ? "Sea" : "Mountain";
    }

    //Gets the max paymentinformation id in the table and returns it
    public int getMaxPayId(){
        String SQL = "SELECT MAX(payment_id) FROM public.paymentinformation";
        try{
            PreparedStatement psql = dbConn.prepareStatement(SQL);
            ResultSet rs = psql.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            else{
                return 1;
            }
        } catch(Exception e){
            System.out.println(e);
        }

        return 1;
    }

    //Create user payment on booking
    public int getUserPayment(int price, int bookingId){

        String payType = "Debit";

        try{
            PreparedStatement insertQuery = dbConn.prepareStatement(INSERT_PAY_SQL);
            int payId = getMaxPayId();
            LocalDate currentDate = getCurrentDate();
            insertQuery.setInt(1,payId);
            insertQuery.setString(2,payType);
            insertQuery.setInt(3,price);
            insertQuery.setDate(4,Date.valueOf(currentDate));
            insertQuery.setInt(5,bookingId);
        } catch(Exception e){
            System.out.println(e);
        }

        return 0;
    }

    //Get the current formatted date value
    public LocalDate getCurrentDate(){
        DateTimeFormatter dTFormat = DateTimeFormatter.ofPattern(DATETEMPLATE);
        CharSequence text;
        LocalDate now = LocalDate.now();
        return now;
    }

}

package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class Employee {


    private Connection dbConn;
    private int e_Sin_Number, hotelId;
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private static final String INSERT_RENT_SQL = "INSERT INTO public.rental" +
            "  (rental_id,room_id,occupant_total,start_date,end_date,payment_id,is_cancelled,c_sin_number) VALUES " +
            " (?,?,?,?,?,?,?,?);";

    private static final String INSERT_PAY_SQL = "INSERT INTO public.paymentinformation"+
            " (payment_id, payment_type, amount_paid, date_paid, booking_id) VALUES " +
            "(?,?,?,?,?)";

    private static final String DATETEMPLATE = "dd/MM/yyyy";

    public Employee(int eSin, int hotId){
        try{
            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","","");
            e_Sin_Number = eSin;
            hotelId = hotId;
        } catch (Exception e){
            System.out.println(e);
        }
    }

    //Searches for rooms available at the employee's hotel
    public void roomsAvailable(){

        //search rooms and select only those with is_rented false

        String SQL = "SELECT * FROM public.room WHERE hotel_id = " + hotelId + " AND is_rented = false";

        try{

            //Get results and display
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            System.out.println("Room Number | Price | End Date | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
            while(rs.next()){
                String endDate = getEndBookingDate(rs.getInt(1));
                System.out.println(rs.getInt(3) + "\t" + rs.getInt(4) + "\t" + endDate + "\t" +
                        boolToString(rs.getBoolean(5)) + "\t" + boolToString(rs.getBoolean(6)) + "\t" +
                        boolToString(rs.getBoolean(7)) + "\t" + boolToString(rs.getBoolean(8)) + "\t" +
                        boolToString(rs.getBoolean(9)) + "\t" + rs.getInt(11) + "\t" + intToViewType(rs.getInt(12)));
            }

        } catch (Exception e){
            System.out.println(e);
        }

    }

    //Displays lists of booked/rented rooms
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

    //Gets all bookings for a given room
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

    //Rent a room based on bookingId and customer's sin number (terrible practice for actual program, please don't do this)
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

    //Delete a given booking
    public void deleteBooking(int bookingId, int cSin){

        String SQL = "DELETE FROM public.booking WHERE booking_id = "+ bookingId +" AND c_sin_number = "+ cSin;

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.executeQuery();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void createRental() throws IOException {

        System.out.println("Input the end date of the customer's stay in the format dd/mm/yyyy");
        String date = reader.readLine().trim();
        LocalDate endDate = getDateFromString(date);
        int roomId = 0;

        roomsAvailable();

        System.out.println("Input the roomId of the room that the user has selected");
        roomId = Integer.parseInt(reader.readLine().trim());

        try{
            if(!checkBookingExists(roomId,getCurrentDate(),endDate) && !checkRentalExists(roomId,getCurrentDate(),endDate)){
                PreparedStatement pst = dbConn.prepareStatement(INSERT_RENT_SQL);
                int rentId = getMaxRentId();
                if (rentId == 1) {
                    rentId = 1;
                } else {
                    rentId++;
                }
                int price = getRoomPrice(roomId);
                int payId = getUserPayment(price,rentId);
                pst.setInt(1,rentId);
                pst.setInt(2,roomId);
                pst.setInt(3,4);
                pst.setDate(4,Date.valueOf(getCurrentDate()));
                pst.setDate(5,Date.valueOf(endDate));
                pst.setInt(6,payId);
                pst.setBoolean(7,false);
                pst.setInt(8,123456789);
                pst.executeUpdate();
            }

        } catch(Exception e){

        }
    }


    //Convert boolean to string to easily display to employee
    public String boolToString(boolean bool){
        return bool == true ? "Yes" : "No";
    }

    //Convert int to string to easily display to employee
    public String intToViewType(int type){
        return type == 1 ? "Sea" : "mountain";
    }

    //Get the maximum rental id from table
    public int getMaxRentId() {
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

    public LocalDate getDateFromString(String input){
        DateTimeFormatter dTFormat = DateTimeFormatter.ofPattern(DATETEMPLATE);
        CharSequence text;
        LocalDate localDate = LocalDate.parse(input, dTFormat);
        return localDate;
    }


    public LocalDate getCurrentDate(){
        DateTimeFormatter dTFormat = DateTimeFormatter.ofPattern(DATETEMPLATE);
        CharSequence text;
        LocalDate now = LocalDate.now();
        return now;
    }

    public int getUserPayment(int price, int bookingId) throws IOException {

        System.out.println("User Payment type: ");
        String payType = reader.readLine().trim();

        try{
            PreparedStatement insertQuery = dbConn.prepareStatement(INSERT_PAY_SQL);
            int payId = getMaxPayId();
            if(payId > 1){
                payId++;
            }
            LocalDate currentDate = getCurrentDate();
            insertQuery.setInt(1,payId);
            insertQuery.setString(2,payType);
            insertQuery.setInt(3,price);
            insertQuery.setDate(4, Date.valueOf(currentDate));
            insertQuery.setInt(5,bookingId);
            insertQuery.executeUpdate();
            return payId;
        } catch(Exception e){
            System.out.println(e);
        }

        return 0;
    }

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

    public int getRoomPrice(int roomId){

        String SQL = "SELECT price from public.room WHERE room_id = "+roomId;

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            } else{
                return 0;
            }
        } catch(Exception e){
            System.out.println(e);
        }

        return 0;
    }

    public void cancelRental(int rentalId){
        String SQL = "UPDATE public.rental SET is_cancelled = 1 WHERE rental_id = "+rentalId;

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            pst.executeUpdate();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public String getEndBookingDate(int roomId){

        String SQL = "SELECT end_date FROM public.bookings WHERE room_id = " + roomId;

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return rs.getDate(1).toString();
            }
        } catch(Exception e){
            System.out.println(e);
        }

        return null;
    }

    public void employeeMainLoop() throws IOException {

        boolean validCredentials = false;
        boolean breakDecisions = false;

        while(!validCredentials){
            System.out.println("Welcome! Please input your sin number to login.");
            int input = Integer.parseInt(reader.readLine().trim());
            validCredentials = validEmpCredentials(input);
        }

        while(!breakDecisions){
            int choice = displayChoices();
            switch (choice){
                case -1: System.out.println("error: invalid selection.");
                        break;
                case 1: roomsAvailable();
                        break;
                case 2: //todo
                        break;
                case 3: rentRoom(7,3); //Placeholders to figure out what to do, probably move code that gets the info to the function itself rather than pass it
                        break;
                case 4: createRental();
                        break;
            }
        }
    }

    public boolean validEmpCredentials(int eSin){

        String SQL = "SELECT * FROM pubilc.employee WHERE e_sin_number = "+eSin;

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                PreparedStatement pst2 = dbConn.prepareStatement("SELECT hotel_id FROM public.employs WHERE e_sin_number = "+rs.getInt(1));
                ResultSet rs2 = pst2.executeQuery();
                if(rs2.next()){
                    e_Sin_Number = rs.getInt(1);
                    hotelId = rs2.getInt(1);
                    System.out.println("Valid credentials... now logging in");
                    return true;
                } else{
                    System.out.println("error:employee is not currently employed");
                }
            } else{
                System.out.println("error: employee sin number not found");
                return false;
            }
        } catch(Exception e){
            System.out.println(e);
        }


        return false;
    }

    public int displayChoices() throws IOException {

        System.out.println("To select an option please input the number associated with it\nWould you like to");
        System.out.println("1. Search for available rooms\n2.Search for rooms that are not available\n3. Transform a customer booking to rental\n4. Create a rental for a customer");

        int input = Integer.parseInt(reader.readLine().trim());

        if(input >=1 && input <= 4){
            return input;
        } else{
            return -1;
        }
    }

}

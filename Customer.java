package lab5;

import com.sun.jdi.IntegerType;

import java.awt.print.Book;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class Customer {

    private Connection dbConn;
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private int cSinNum;
    private String fullName;
    private String address;
    private BookingHelper bookHelper;

    private static final String DATETEMPLATE = "dd/MM/yyyy";

    private static final String INSERT_BOOK_SQL = "INSERT INTO public.booking" +
            "  (booking_id,room_id,occupant_total,start_date,end_date,payment_id,is_cancelled,c_sin_number) VALUES " +
            " (?,?,?,?,?,?,?,?);";

    private static final String INSERT_PAY_SQL = "INSERT INTO public.paymentinformation"+
            " (payment_id, payment_type, amount_paid, date_paid) VALUES " +
            "(?,?,?,?)";

    public Customer(){

        try{

            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","","");

        } catch (Exception e){
            System.out.println(e);
        }

    }

    //Function used by customer to book a room, needs several inputs
    //Undecided if the inputs will come before function call or during
    //Fairly certain it inputs will come from within
    public void bookRoom() throws IOException {

        bookHelper = getBookHelper();

        //Check if a booking or rental exists for the desired rental period
        if(!checkBookingExists(bookHelper.getRoomId(), bookHelper.getStartDate(), bookHelper.getEndDate()) && !checkRentalExists(bookHelper.getRoomId(),bookHelper.getStartDate(),bookHelper.getEndDate())){
            System.out.println("Room is not booked or rented for the date range you have chosen, book it?");
            boolean validInput = false;
            while(!validInput){
                String input = reader.readLine().trim().toLowerCase();
                if(input.equals("y")) {
                    try {

                        //PreparedStatement roomInfo = dbConn.prepareStatement("SELECT  * FROM public.room WHERE room_id = " + bookHelper.roomId);


                        //Get a prepared statement to insert a booking into its table
                        PreparedStatement bookRoom = dbConn.prepareStatement(INSERT_BOOK_SQL);
                        //Get a prepared statement to get max booking id from the table
                        PreparedStatement maxId = dbConn.prepareStatement("SELECT MAX(booking_id) FROM public.booking");
                        ResultSet rs = maxId.executeQuery();
                        //Default value of id if query returns null
                        int id = 1;
                        if (rs.next()) {
                            id = rs.getInt(1) + 1;
                        }
                        //Get payment id from customer
                        //Then insert all values into booking table and execute

                        int payId = getUserPayment(bookHelper.getPrice(), id);
                        bookRoom.setInt(1, id);
                        bookRoom.setInt(2, bookHelper.getRoomId());
                        bookRoom.setInt(3, bookHelper.getOccupancy());
                        bookRoom.setDate(4, Date.valueOf(bookHelper.getStartDate()));
                        bookRoom.setDate(5, Date.valueOf(bookHelper.getEndDate()));
                        if(payId > -1){
                            bookRoom.setInt(6, payId);
                        }
                        bookRoom.setBoolean(7, false);
                        bookRoom.setInt(8, cSinNum);
                        bookRoom.executeUpdate();
                        maxId.close();
                        bookRoom.close();
                        rs.close();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    validInput = true;
                    break;
                } else if(input.equals("n")){
                    validInput = true;
                    break;
                } else{
                    System.out.println("error, invalid input.");
                    System.out.println("The given room is not booked, would you like to book it? (Y/N)");
                }
            }

        } else{
            System.out.println("Sorry, that room is currently either booked or rented for the time window you have specified");
        }

    }

    //Search for rooms based on the hotel name
    public void searchByHotelName(String hName){

        try {
            //Prepare statement to get all hotel ids for hotels with the name given by the user

            //PreparedStatement checkHotel = dbConn.prepareStatement("SELECT hotel_id FROM public.hotel WHERE LOWER(hotel_name) = " + hName.toLowerCase());
            //ResultSet validHotel = checkHotel.executeQuery();
            String stm="SELECT hotel_id FROM public.hotel WHERE LOWER(hotel_name) = ?";
            PreparedStatement checkHotel = dbConn.prepareStatement(stm);
             checkHotel.setString(1, hName.toLowerCase());
             ResultSet validHotel = checkHotel.executeQuery();
            //If query does not return null get all rooms for all hotel ids
            if(validHotel.next()){
                int hotelId = validHotel.getInt(1);
                validHotel.close();
                checkHotel.close();
                PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE hotel_id = "+hotelId+"AND is_rented = false");
                ResultSet roomList = getRooms.executeQuery();
                //Display room results to customer
                System.out.println("Results for room at: "+hName);
                while(roomList.next()){
                    displayRooms(roomList);
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
            PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE room_capacity = "+ size + "AND is_rented = false");
            ResultSet roomList = getRooms.executeQuery();
            //Display room results to customer
            System.out.println("Results for rooms with a capacity of: "+size);
            displayRooms(roomList);

        } catch (Exception e){
            System.out.println(e);
        }
    }

    public void searchByDates() throws IOException {
        System.out.println("Please input the id of the chain you wish to search at, and the start and end dates for your stay separated by a space.\n" +
                "The date should be formatted as 'dd/MM/yyyy'");
        String input = reader.readLine().trim().toLowerCase();
        ArrayList<String> inputList = new ArrayList<>(Arrays.asList(input.split(" ")));
        int chainId = Integer.parseInt(inputList.get(0));
        String SQL = "SELECT hotel_id FROM public.hotel WHERE chain_id = "+chainId;
        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            LocalDate startD = getDateFromString(inputList.get(1));
            LocalDate endD = getDateFromString(inputList.get(2));
            boolean hasResults = false;
            while(rs.next()){
                PreparedStatement getRooms = dbConn.prepareStatement("SELECT * FROM public.room WHERE hotel_id = "+rs.getInt(1));
                ResultSet rooms = getRooms.executeQuery();
                while(rooms.next()){
                    if(!checkBookingExists(rooms.getInt(1),startD,endD) && !checkRentalExists(rooms.getInt(1),startD,endD)){
                        if(rooms.getBoolean(10) == false) {
                            hasResults = true;
                            String roomNumber = String.format("%-14d",rooms.getInt(3));
                            String roomPrice = String.format("%-8d",rooms.getInt(4));
                            String roomTv = String.format("%-5s", boolToString(rooms.getBoolean(5)));
                            String roomAc = String.format("%-5s",boolToString(rooms.getBoolean(6)));
                            String roomFridge = String.format("%-9s",boolToString(rooms.getBoolean(7)));
                            String roomSnack = String.format("%-11s",boolToString(rooms.getBoolean(8)));
                            String roomExtend = String.format("%-13s",boolToString(rooms.getBoolean(9)));
                            String roomCap = String.format("%-11d",rooms.getInt(11));
                            String roomView = intToViewType(rooms.getInt(12));
                            System.out.println(roomNumber  + roomPrice + roomTv + roomAc + roomFridge + roomSnack + roomExtend + roomCap + roomView);
                        }
                    }
                }
            }
            if(!hasResults){
                System.out.println("No results were found for the date range that you specified");
            }
        } catch(Exception e){
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
        String d1 = date1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String d2 = date2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String SQL = "SELECT * FROM public.booking WHERE room_id = "+roomId+" AND ((start_date >= '"+ d1 +"' AND start_date <= '"+d2+"') OR (end_date <= '"+d2+"' AND end_date >= '"+d1+"'))";

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

        String d1 = date1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String d2 = date2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.println(d1+"\n"+d2);

        String SQL = "SELECT * FROM public.booking WHERE room_id = "+roomId+" AND ((start_date >= '"+ d1 +"' AND start_date <= '"+d2+"') OR (end_date <= '"+d2+"' AND end_date >= '"+d1+"'))";

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

    //Checks if a room with the given room id exists
    public BookingRoomInfo checkRoomExists(int rId){

        boolean validRoomId = false;
        while(!validRoomId){

            String SQL = "SELECT price,room_capacity,room_id FROM public.room WHERE room_id = "+String.valueOf(rId);

            try {
                BookingRoomInfo bInfo;
                PreparedStatement pst = dbConn.prepareStatement(SQL);
                ResultSet rs = pst.executeQuery();

                if(rs.next()) {
                    bInfo = new BookingRoomInfo(rs.getInt(1), rs.getInt(2), rs.getInt(3));
                    return bInfo;
                } else{
                    System.out.println("error, no room found with that room id");
                    System.out.println("Please input a valid room id");
                    rId = Integer.parseInt(reader.readLine().trim());
                }
            } catch(Exception e){
                System.out.println(e);
            }
        }

        return null;
    }

    //Checks if a hotel with the given hotel name exists (currently obsolete)
    public boolean checkHotelExists(String hName){

        //String SQL = "SELECT hotel_id, chain_id, hotel_name, star_category, room_count, email_address, e_sin_number, phone_number FROM public.hotel WHERE LOWER(hotel_name) LIKE 'metropolitan'";
        try{
        	  String stm="SELECT hotel_id, chain_id, hotel_name, star_category, room_count, email_address, e_sin_number, phone_number FROM public.hotel WHERE LOWER(hotel_name)=?";
              PreparedStatement pst= dbConn.prepareStatement(stm);
               pst.setString(1, hName);


            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Success Somehow");
                rs.close();
                pst.close();
                return true;
            } else{
                return false;
            }
        } catch(Exception e){
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
    public int getMaxPayId() throws IOException {

        System.out.println("Would you like to pay for your room now? (Y/N)");
        String input = reader.readLine().trim().toLowerCase();
        if(input.equals("y")){
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
        } else if(input.equals("n")){
            return -1;
        } else{
            System.out.println("error");
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

    //Checks if the customer is valid
    //If customer is in DB load relavent information into class
    public boolean validCustomer(int cSin){

        String SQL = "SELECT * FROM public.customer WHERE c_sin_number = "+String.valueOf(cSin);

        try{

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();

            if(rs.next()){
                cSinNum = rs.getInt(1);
                fullName = rs.getString(2);
                address = rs.getString(3);
                return true;
            }else {

                return false;
            }

        } catch(Exception e){
            System.out.println(e);
        }

        return false;
    }

    //Displays the options for the user
    public int displayOptions() throws IOException {
        System.out.println("What would you like to do? (Input number of desired option)");
        System.out.println("Option 1: Search for rooms\nOption 2: Book a room");

        String input = reader.readLine().trim();
        int num = Integer.parseInt(input);
        if(num == 1 || num == 2){
            return Integer.parseInt(input);
        } else{
            System.out.println("Sorry, that is an invalid input. Please type '1' or '2'");
            displayOptions();
        }

        return 0;
    }

    //Checks if the user wants to continue the decision loop
    public boolean contLoop() throws IOException {
        System.out.println("Would you like to choose an option or exit? (C/E)");
        boolean validInput = false;
        while(!validInput){
            String option = reader.readLine().toLowerCase();
            if(option.equals("c")){
                validInput = true;
                return false;
            } else if(option.equals("e")){
                validInput = true;
                return true;
            } else{
                System.out.println("Sorry, that is an invalid input. Please type 'C' or 'E'");
            }
        }

        return false;
    }

    //Main decision loop for the customer
    public void mainCustomerLoop() throws IOException {
        boolean validCSin = false;
        while(!validCSin){
            System.out.println("Welcome! Please login using your Sin Number: ");
            int cSin = Integer.parseInt(reader.readLine());
            if(validCustomer(cSin)){
                System.out.println("Welcome, "+ fullName);
                validCSin = true;
                break;
            }
            else{
                System.out.println("Sorry, that is not a valid Sin Number.");
            }
        }

        boolean noExit = false;

        while(!noExit){
            int optionChose = displayOptions();

            switch (optionChose){
                case 1: searchRooms();
                        break;
                case 2: bookRoom();
                        break;
                default: System.out.println("Error, invalid option");
                        break;
            }

            noExit = contLoop();
        }
    }

    //Method to search rooms based on the option that the user selects
    public void searchRooms() throws IOException {
        System.out.println("Would you like to search for rooms by any value? (Y/N)");
        String byValue = reader.readLine().trim().toLowerCase();

        if(byValue.equals("y")){
            boolean validInput = false;
            while(!validInput){
                System.out.println("Which value do you want to search by? (Input number of desired option)");
                System.out.println("Option 1: Search By Hotel Name\nOption 2: Search By Capacity\nOption 3: Search By Hotel Chain Id");
                String option = reader.readLine().trim();
                int num = Integer.parseInt(option);
                if(num == 1){
                    boolean validHotel = false;
                    validInput = true;
                    while(!validHotel){
                        System.out.println("Input the name of the hotel you wish to search by");
                        String hName = reader.readLine().toLowerCase();
                        //TODO find out why this doesn't work when called from within the customer
                        validHotel = this.checkHotelExists(hName);
                        if(validHotel){
                            searchByHotelName(hName);
                            break;
                        } else{
                            System.out.println("Sorry, that's not a valid hotel name. No hotels exist with the name: "+hName);
                        }
                    }
                    break;
                } else if(num == 2){
                    boolean validNumber = false;
                    validInput = true;
                    while(!validNumber){
                        System.out.println("Input the capacity you wish to search for");
                        int capacity = Integer.parseInt(reader.readLine().trim());
                        if(capacity > 0){
                            validNumber = true;
                            searchByCapacity(capacity);
                            break;
                        } else{
                            System.out.println("Sorry, that's not a valid capacity. The minimum occupancy of a room is 1.");
                        }
                    }
                } else if(num == 3){
                    searchByDates();
                    validInput = true;
                    break;
                } else{
                    System.out.println("Sorry, that is not a valid input. Please input the number of the desired option.");
                }
            }
        } else if(byValue.equals("n")){
            searchAllRooms();
        } else{
            System.out.println("Sorry, that is not a valid input.");
            searchRooms();
        }
    }

    //Gets all needed information for a specific booking and stores it in a helper class
    public BookingHelper getBookHelper() throws IOException {
        boolean validInput = false;
        while(!validInput){
            System.out.println("Do you wish to see a list of available rooms? (Y/N)");
            String input = reader.readLine().trim().toLowerCase();
            if(input.equals("y")){
                searchAllRooms();
                validInput = true;
                break;
            } else if(input.equals("n")){
                validInput = true;
                break;
            } else{
                System.out.println("Error");
            }
        }
        System.out.println("Input the room ID for the room you wish to book");
        int rId = Integer.parseInt(reader.readLine().trim());
        BookingRoomInfo bInfo = checkRoomExists(rId);
        if(bInfo != null){
            boolean dateLoop = false;
            while(!dateLoop){
                System.out.println("Input the desired start date and end date for the booking. Please separate the start date and end date by a space Format is 'dd/mm/yyyy'");
                String[] input = reader.readLine().trim().split(" ");
                LocalDate start = getDateFromString(input[0]);
                LocalDate end = getDateFromString(input[1]);
                if(end.compareTo(start) >0){
                    dateLoop = true;
                    boolean occLoop = false;
                    while(!occLoop){
                        System.out.println("Input the number of occupants for this room. The maxmium is "+bInfo.getMaxSize());
                        int occTotal = Integer.parseInt(reader.readLine().trim());
                        if(occTotal > 0 && occTotal <= bInfo.getMaxSize()){
                            occLoop = true;
                            return new BookingHelper(start,end,rId,occTotal,bInfo.getPrice());
                        } else{
                            System.out.println("Sorry, that occupancy total is invalid. The minimum number of occupants is 0, and the max is "+bInfo.getMaxSize());
                        }
                    }
                } else{
                    System.out.println("Sorry that date range is not valid. Make sure that the end date is larger than the start date.");
                }
            }
        }

        return null;
    }

    public void searchAllRooms(){

        String SQL = "SELECT * FROM public.room WHERE is_rented = false";

        try{

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet roomList = pst.executeQuery();
            displayRooms(roomList);

        } catch(Exception e){
            System.out.println(e);
        }

    }

    //Gets a list of all hotel_ids with a given hotel name (currently obsolete)
    public ArrayList<Integer> getHotelIds(String hotelName){

        String SQL = "SELECT hotel_id FROM public.hotel WHERE hotel_name = " + hotelName.toLowerCase();

        ArrayList<Integer> returnList = new ArrayList<>();

        try{

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();

            while(rs.next()){
                returnList.add(rs.getInt(1));
            }
            return returnList;
        } catch(Exception e){
            System.out.println(e);
        }


        return null;
    }

    public void displayRooms(ResultSet roomList) throws SQLException {
        System.out.println("Room Number | Price | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
        while(roomList.next()){
            if(roomList.getBoolean(10) == false) {
                String roomNumber = String.format("%-14d",roomList.getInt(3));
                String roomPrice = String.format("%-8d",roomList.getInt(4));
                String roomTv = String.format("%-5s", boolToString(roomList.getBoolean(5)));
                String roomAc = String.format("%-5s",boolToString(roomList.getBoolean(6)));
                String roomFridge = String.format("%-9s",boolToString(roomList.getBoolean(7)));
                String roomSnack = String.format("%-11s",boolToString(roomList.getBoolean(8)));
                String roomExtend = String.format("%-13s",boolToString(roomList.getBoolean(9)));
                String roomCap = String.format("%-11d",roomList.getInt(11));
                String roomView = intToViewType(roomList.getInt(12));
                System.out.println(roomNumber  + roomPrice + roomTv + roomAc + roomFridge + roomSnack + roomExtend + roomCap + roomView);
            }
        }
    }

}

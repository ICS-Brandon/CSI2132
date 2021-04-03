package DBMS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Employee {


    private Connection dbConn;
    private int e_Sin_Number, hotelId;
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private static final String INSERT_RENT_SQL = "INSERT INTO public.rental" +
            "  (rental_id,room_id,occupant_total,start_date,end_date,payment_id,is_cancelled,c_sin_number) VALUES " +
            " (?,?,?,?,?,?,?,?);";

    private static final String INSERT_PAY_SQL = "INSERT INTO public.paymentinformation"+
            " (payment_id, payment_type, amount_paid, date_paid) VALUES " +
            "(?,?,?,?)";

    private static final String DATETEMPLATE = "dd/MM/yyyy";

    public Employee(){
        try{
            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","user","password");


        } catch (Exception e){

            System.out.println(e);
        }
    }

    //Searches for rooms available at the employee's hotel
    public void roomsAvailable() throws IOException {

        //search rooms and select only those with is_rented false

        System.out.println("Input the start date and end date in the format of 'dd/MM/yyyy', the start and end dates being separated by a space.");
        ArrayList<String> dates = new ArrayList<String>(Arrays.asList(reader.readLine().trim().split(" ")));

        LocalDate start = getDateFromString(dates.get(0));
        LocalDate end = getDateFromString(dates.get(1));

        String SQL = "SELECT * FROM public.room WHERE hotel_id = " + hotelId;

        try{



            //Get results and display
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet roomList = pst.executeQuery();
            System.out.println("Room Id | Room Number | Price | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
            while(roomList.next()){
                if(!checkBookingExists(roomList.getInt(1),start,end) && !checkRentalExists(roomList.getInt(1),start,end)) {
                    String roomId = String.format("%-10d",roomList.getInt(1));
                    String roomNumber = String.format("%-14d",roomList.getInt(3));
                    String roomPrice = String.format("%-8d",roomList.getInt(4));
                    String roomTv = String.format("%-5s", boolToString(roomList.getBoolean(5)));
                    String roomAc = String.format("%-5s",boolToString(roomList.getBoolean(6)));
                    String roomFridge = String.format("%-9s",boolToString(roomList.getBoolean(7)));
                    String roomSnack = String.format("%-11s",boolToString(roomList.getBoolean(8)));
                    String roomExtend = String.format("%-13s",boolToString(roomList.getBoolean(9)));
                    String roomCap = String.format("%-11d",roomList.getInt(11));
                    String roomView = intToViewType(roomList.getInt(12));
                    System.out.println(roomId + roomNumber + roomPrice + roomTv + roomAc + roomFridge + roomSnack + roomExtend + roomCap + roomView);
                }
            }

            System.out.println();
        } catch (Exception e){
            System.out.println(e);
        }

    }

    //Displays lists of booked/rented rooms
    public void roomsBooked() throws IOException {

        System.out.println("Input the start date and end date in the format of 'dd/MM/yyyy', the start and end dates being separated by a space.");
        ArrayList<String> dates = new ArrayList<String>(Arrays.asList(reader.readLine().trim().split(" ")));

        LocalDate start = getDateFromString(dates.get(0));
        LocalDate end = getDateFromString(dates.get(1));


        String SQL = "SELECT * FROM public.room WHERE hotel_id = "+hotelId;

        try {

            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();

            System.out.println("\nInformation is in the order of: room ID / Room Number / End Date / Has TV / Has AC / Has Snackbar / Is Extendable / Room Capacity / View Type");

            while(rs.next()){
                if(checkBookingExists(rs.getInt(1),start,end) || checkRentalExists(rs.getInt(1),start,end)){
                    String endDate = getEndBookingDate(rs.getInt(1));
                    System.out.println(rs.getInt(1) + " / " + rs.getInt(3) + " / " + rs.getInt(4) + " / " + endDate + " / " +
                            boolToString(rs.getBoolean(5)) + " / " + boolToString(rs.getBoolean(6)) + " / " +
                            boolToString(rs.getBoolean(7)) + " / " + boolToString(rs.getBoolean(8)) + " / " +
                            boolToString(rs.getBoolean(9)) + " / " + rs.getInt(11) + " / " + intToViewType(rs.getInt(12)));
                }
            }
            System.out.println();

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

        String getRoom = "SELECT * FROM public.booking WHERE booking_id = "+String.valueOf(bookingId)+" AND c_sin_number = "+cSin;

        try{

            Statement getBooking = dbConn.createStatement();
            ResultSet validBooking = getBooking.executeQuery(getRoom);

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
                Statement st=dbConn.createStatement();
                ResultSet rs=st.executeQuery("select r.price from room r, booking b where b.booking_id="+bookingId+" and r.room_id=b.room_id");
                if (rs.next()) {
                	getUserPayment2(rs.getInt(1), bookingId, validBooking.getInt(6));
                }

                createRental.executeUpdate();
                changeRentalStatus(bookingId);
                deleteBooking(bookingId,cSin);


            }


        } catch (Exception e){
        	System.out.println("error from rentroom method");
            System.out.println(e);
        }

    }

    //changes rental status of a room
    public void changeRentalStatus(int rentalID) {
    	try {
    		Statement st=dbConn.createStatement();
    		st.executeUpdate("update room set is_rented=true where room_id=(select r.room_id from room r, rental rl where r.room_id=rl.room_id and rl.rental_id="+rentalID+")");

    	}catch (Exception e) {
    		System.out.println(e);
    	}


    }

    //Delete a given booking
    public void deleteBooking(int bookingId, int cSin){



        try{
            Statement pst = dbConn.createStatement();
            pst.executeUpdate("DELETE FROM booking WHERE booking_id = "+ bookingId +" AND c_sin_number = "+ cSin);
        } catch(Exception e){
        	System.out.println("error in deleting booking");
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
        System.out.println(roomId);

        System.out.println("Input the sin number of the customer");
        int maxCapacity = getMaxOccupancy(roomId);
        int cSin = Integer.parseInt(reader.readLine().trim());

        System.out.println("Input the number of occupants, the max is: "+maxCapacity);
        int occupancy = Integer.parseInt(reader.readLine().trim());

        System.out.println("Input the number of occupants for the room.");

        try{
            if(!checkBookingExists(roomId,getCurrentDate(),endDate) && !checkRentalExists(roomId,getCurrentDate(),endDate)){
                PreparedStatement pst = dbConn.prepareStatement(INSERT_RENT_SQL);
                String start = getCurrentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String end = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                int rentId = getMaxRentId();
                int price = getRoomPrice(roomId);
                int payId = getUserPayment(price,rentId);
                pst.setInt(1,rentId);
                pst.setInt(2,roomId);
                pst.setInt(3,occupancy);
                pst.setDate(4, Date.valueOf(start));
                pst.setDate(5,Date.valueOf(end));
                pst.setInt(6,payId);
                pst.setBoolean(7,false);
                pst.setInt(8,cSin);
                System.out.println(pst);
                pst.executeUpdate();
                changeRentalStatus(rentId);
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
        return type == 1 ? "Sea" : "Mountain";
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

    public boolean checkRentalExists(int roomId,LocalDate date1, LocalDate date2){
        String d1 = date1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String d2 = date2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
            if(payId >= 1){
                payId++;
            }
            LocalDate currentDate = getCurrentDate();
            insertQuery.setInt(1,payId);
            insertQuery.setString(2,payType);
            insertQuery.setInt(3,price);
            insertQuery.setDate(4, Date.valueOf(currentDate));

            insertQuery.executeUpdate();
            return payId;
        } catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    //For rentRoom()
    public int getUserPayment2(int price, int bookingId, int payID) throws IOException {

        System.out.println("User Payment type: ");
        String payType = reader.readLine().trim();

        try{
            PreparedStatement insertQuery = dbConn.prepareStatement(INSERT_PAY_SQL);
            int payId = payID;

            LocalDate currentDate = getCurrentDate();
            insertQuery.setInt(1,payId);
            insertQuery.setString(2,payType);
            insertQuery.setInt(3,price);
            insertQuery.setDate(4, Date.valueOf(currentDate));

            insertQuery.executeUpdate();
            return payId;
        } catch(Exception e){
        	System.out.println("error from payment method");
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

        String SQL = "SELECT end_date FROM public.booking WHERE room_id = " + roomId;

        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                String toRet = rs.getDate(1).toString();
                if(toRet.equals("null")){
                    return new String("N/A");
                } else{
                    return toRet;
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }

        return new String("N/A");
    }

    //Validates that given booking id exists in booking table
    public boolean validateBookingId(int bookingID) {
    	try {

			Statement st=dbConn.createStatement();
			ResultSet rs=st.executeQuery("SELECT * FROM public.booking where booking_id="+bookingID);

			if (rs.next()) {
				return true;
			}else {
				return false;
			}

		}catch(Exception e){

			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
    }

    public void employeeMainLoop() throws IOException {

        boolean validEmpId = false;
        boolean breakDecisions = false;
        int input=0;
        while(!validEmpId){
            System.out.println("Welcome! Please input your sin number to login.");
            input = Integer.parseInt(reader.readLine().trim());
            validEmpId = validEmpCredentials(input);


        }
        try {
        	Statement s=dbConn.createStatement();
       		ResultSet r=s.executeQuery("SELECT hotel_id FROM public.employs where e_sin_number="+input);
       		r.next();
       		hotelId=r.getInt(1);

        }catch(Exception e) {
        	System.out.println(e);
        }


        while(!breakDecisions){
            int choice = displayChoices();
            switch (choice){
                case -1: System.out.println("error: invalid selection.");
                        break;
                case 1: roomsAvailable();
                        break;
                case 2: roomsBooked();
                        break;
                case 3: //need to alter booking table to continue, also remember to fix the EmpValidation methos
                	try {

                		//prints out all the bookings
                		Statement st=dbConn.createStatement();
            			ResultSet rs=st.executeQuery("SELECT * FROM public.booking");

            			System.out.println("Booking ID |  Room ID | Occupant Total | Start Date | End Date | Payment ID | Is Cancelled? | Customer Sin Number");
            			while (rs.next()) {
            				for (int i=1; i<=8;++i) {
            					System.out.print(rs.getString(i)+"\t\t");
            				}

            				System.out.println();
            			}
            			rs.close();
            			st.close();

                    	System.out.println("Enter the booking id to transform");
                    	int bookingId=Integer.parseInt(reader.readLine().trim());
                    	while(!validateBookingId(bookingId)) {
                    		System.out.println("Please enter valid booking id");
                        	bookingId=Integer.parseInt(reader.readLine().trim());

                    	}
                    	System.out.println("Enter the customer sin number to transform");
                    	int cSin=Integer.parseInt(reader.readLine().trim());

                    	rentRoom(bookingId, cSin);
                    	deleteBooking(bookingId,cSin);
                	}catch(Exception e) {
                		System.out.println(e);
                	}

                        break;
                case 4: createRental();
                        break;
                case 5: breakDecisions = true;
                        break;


            }
        }
    }



    public boolean validEmpCredentials(int eSin){

        String SQL = "SELECT * FROM employee WHERE e_sin_number = "+eSin;

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
        System.out.println("1. Search for available rooms\n2. Search for rooms that are not available\n3. Transform a customer booking to rental\n4. Create a rental for a customer\n5. Exit the application");

        int input = Integer.parseInt(reader.readLine().trim());

        if(input >=1 && input <= 5){
            return input;
        } else{
            return -1;
        }
    }

    public void displayRooms(ResultSet roomList) throws SQLException {
        System.out.println("Room Id | Room Number | Price | TV | AC | Fridge | Snackbar | Extendable | Capacity | View Type");
        while(roomList.next()){
            if(roomList.getBoolean(10) == false) {
                String roomId = String.format("%-10d",roomList.getInt(1));
                String roomNumber = String.format("%-14d",roomList.getInt(3));
                String roomPrice = String.format("%-8d",roomList.getInt(4));
                String roomTv = String.format("%-5s", boolToString(roomList.getBoolean(5)));
                String roomAc = String.format("%-5s",boolToString(roomList.getBoolean(6)));
                String roomFridge = String.format("%-9s",boolToString(roomList.getBoolean(7)));
                String roomSnack = String.format("%-11s",boolToString(roomList.getBoolean(8)));
                String roomExtend = String.format("%-13s",boolToString(roomList.getBoolean(9)));
                String roomCap = String.format("%-11d",roomList.getInt(11));
                String roomView = intToViewType(roomList.getInt(12));
                System.out.println(roomId + roomNumber + roomPrice + roomTv + roomAc + roomFridge + roomSnack + roomExtend + roomCap + roomView);
            }
        }
    }

    public int getMaxOccupancy(int roomId){
        String SQL = "SELECT room_capacity FROM public.room WHERE room_id = "+roomId;
        try{
            PreparedStatement pst = dbConn.prepareStatement(SQL);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

}

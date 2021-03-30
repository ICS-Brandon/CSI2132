package lab5;

import java.sql.*;
public class Main {

    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
            //ConnectionDriver connDriver = new ConnectionDriver();
            //connDriver.addHotelChain(6,"519-442-1985","test2@gmail.com","HotelsRUs","Ottawa");
            //connDriver.updateHotelChain("phone_number",1,"Testing");
            Customer test = new Customer();
            test.mainCustomerLoop();
            /*ResultSet test = connDriver.selectAll("hotelchain");
            while(test.next()){
                System.out.println(test.getString(1));
            }
             */
        } catch(Exception e){
        	e.printStackTrace();
            System.out.println(e);
        }
    }
}

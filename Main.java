package com.test;

public class Main {

    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
            ConnectionDriver connDriver = new ConnectionDriver();
            //connDriver.addHotelChain(6,"519-442-1985","test2@gmail.com","HotelsRUs","Ottawa");
            //connDriver.updateHotelChain("phone_number",1,"Testing");
            DataAdmin test = new DataAdmin();
            test.deleteEmployee(122222222);
            /*ResultSet test = connDriver.selectAll("hotelchain");
            while(test.next()){
                System.out.println(test.getString(1));
            }
             */
        } catch(Exception e){
            System.out.println(e);
        }
    }
}

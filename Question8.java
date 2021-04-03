package DBMS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Question8 {
	private Connection dbConn;
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public Question8(){

        try{

            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","user","password");

        } catch (Exception e){
            System.out.println(e);
        }

    }
    
    public void answer() {
    	boolean run=true;
    	while(run) {
	    	try {
		    	System.out.println("Which query would you like to see (1-8, 9 to exit)? ");
		    	String option = reader.readLine().trim();
		    	int num = Integer.parseInt(option);
		    	if (num==1) {
		    		System.out.println("Choose hotel ID from the following list:");
		    		Statement st=dbConn.createStatement();
					ResultSet rs1=st.executeQuery("select h.hotel_id, h.hotel_name from hotel h");
					while (rs1.next()) { 
		 				for (int i=1; i<=2;++i) {
		 					System.out.print(rs1.getString(i)+"\t\t");
		 				}
		 				 
		 				System.out.println(); 
		 			} 
		 			rs1.close();
			    	int hID = Integer.parseInt(reader.readLine().trim());
			    	try {
			    		 String stm="select c.full_name, view_type, price, start_date, hc.chain_name from rental rl, customer c, room r, hotel h, hotelchain hc where rl.c_sin_number=c.c_sin_number and rl.room_id=r.room_id and h.chain_id=hc.chain_id and h.hotel_id=? order by r.price asc, rl.start_date desc";
			    		 PreparedStatement pst= dbConn.prepareStatement(stm);
			             pst.setInt(1, hID);
			             ResultSet rs = pst.executeQuery();
			             System.out.println("Customer Name	| View Type		| Price		| Start Date 	| Hotel Chain	");
			             while (rs.next()) { 
			 				for (int i=1; i<=5;++i) {
			 					System.out.print(rs.getString(i)+"\t\t");
			 				}
			 				 
			 				System.out.println(); 
			 			} 
			 			rs.close(); 
			    	}catch(Exception f) {
			    		System.out.println(f.getMessage());
			    	}
		    		
		    	}else if(num==2){
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("create view CustomerListView as select c.*, hc.chain_name from customer c, rental rl, room r, hotel h, hotelchain hc where rl.c_sin_number=c.c_sin_number and rl.room_id=r.room_id and r.hotel_id=h.hotel_id and h.chain_id=hc.chain_id order by hc.chain_id");
					while (rs.next()) { 
						for (int i=1; i<=6;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close();
		    	}else if(num==3){
		    		
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select * from room where price=(select min(r.price) as cheapestprice from room r)");
					while (rs.next()) { 
						for (int i=1; i<=12;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close();
					
		    	}else if(num==4){
		    		
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select r.*, r.price, h.star_category from room r, hotel h, hotelchain hc where r.hotel_id=h.hotel_id and h.chain_id=hc.chain_id and hc.office_location='Ottawa' order by h.star_category, r.price");
		
					while (rs.next()) { 
						for (int i=1; i<=14;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close(); 
		    		
		    	}else if(num==5){
		    		
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select * from (select * from room r inner join rental rl on r.room_id=rl.room_id) as rrl where extract(day from rrl.start_date)=10 and extract(month from rrl.start_date)=10");
					
					while (rs.next()) { 
						for (int i=1; i<=12;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close(); 
		    		
		    	}else if(num==6){
		    		Statement st=dbConn.createStatement();
					st.execute("update customer set phone_number='6136136133' where full_name='John Doe'");
					
		    		
		    	}else if(num==7){
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select tt.star_category from (select h.star_category, count(*) from hotel h, rental rl, booking b, room r where(rl.room_id=r.room_id or b.room_id=r.room_id) and r.hotel_id=h.hotel_id group by star_category) as tt join (select max(count) as maxcount from (select h.star_category, count(*) from hotel h, rental rl, booking b, room r where(rl.room_id=r.room_id or b.room_id=r.room_id) and r.hotel_id=h.hotel_id group by star_category) as k) as t on tt.count=t.maxcount");
		
					while (rs.next()) { 
						for (int i=1; i<=1;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close(); 
		    		
		    	}else if(num==8){
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select max(salary) AS salary from employee where salary < (select max(salary) from employee)");
					
					while (rs.next()) { 
						for (int i=1; i<=1;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close(); 
		    	}else if(num==9){
		    		run=false;
		    	}else {
		    		System.out.println("Must be a number from 1-8.");
		    		answer();
		    	}
	    	}catch(Exception e){
	    
	            System.out.println(e);
	        }
    	}
    	
    }

}
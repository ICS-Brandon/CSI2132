package lab5;

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

            dbConn = DriverManager.getConnection("jdbc:postgresql://web0.site.uottawa.ca:15432/group_a07_g25","","");

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
		    		System.out.println("Choose hotel from the following list:");
		    		Statement st=dbConn.createStatement();
					ResultSet rs1=st.executeQuery("select h.hotel_name from hotel h");
					while (rs1.next()) { 
		 				for (int i=1; i<=1;++i) {
		 					System.out.print(rs1.getString(i)+"\t\t");
		 				}
		 				 
		 				System.out.println(); 
		 			} 
		 			rs1.close();
			    	String hName = reader.readLine().trim();
			    	try {
			    		 String stm="select * from (select full_name, view_type, price, start_date, hc.chain_name from hotelchain hc, (select * from hotel h join (select * from customer c  join (select * from room r  join rental rl on rl.room_id=r.room_id) as rrl on c.c_sin_number=rrl.c_sin_number) as crrl on crrl.hotel_id=h.hotel_id) as hcrrl where hcrrl.chain_id=hc.chain_id and hotel_name=? order by price asc) as finaltable order by start_date desc";
			    		 PreparedStatement pst= dbConn.prepareStatement(stm);
			             pst.setString(1, hName);
			             ResultSet rs = pst.executeQuery();
			             System.out.println("Customer Name | View Type | Price | Start Date | Hotel Chain");
			             while (rs.next()) { 
			 				for (int i=1; i<=3;++i) {
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
					System.out.println("success");
					while (rs.next()) { 
						for (int i=1; i<=12;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close();
		    	}else if(num==3){
		    		
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select * from room where price=(select min(r.price) as cheapestprice from room r)");
					System.out.println("success");
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
					ResultSet rs=st.executeQuery("select * from room r, hotel h, hotelchain hc where r.hotel_id=h.hotel_id and h.chain_id=hc.chain_id and hc.office_location='Ottawa' order by h.star_category, r.price");
					System.out.println("success");
					while (rs.next()) { 
						for (int i=1; i<=12;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close(); 
		    		
		    	}else if(num==5){
		    		
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select * from (select * from room r inner join rental rl on r.room_id=rl.room_id) as rrl where extract(day from rrl.start_date)=10 and extract(month from rrl.start_date)=10");
					System.out.println("success");
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
					ResultSet rs=st.executeQuery("update customer set phone_number='6136136133' where full_name='John Doe'");
					System.out.println("success");
					while (rs.next()) { 
						for (int i=1; i<=12;++i) {
							System.out.print(rs.getString(i)+"\t\t");
						}
						 
						System.out.println(); 
					} 
					rs.close(); 
					st.close(); 
		    		
		    	}else if(num==7){
		    		Statement st=dbConn.createStatement();
					ResultSet rs=st.executeQuery("select *, max(amount) from (select hrcrl.star_category, count(*) as amount from (select * from (select * from (select * from customer c join rental rl on c.c_sin_number=rl.c_sin_number) as crl join room r on crl.room_id=r.room_id) as rcrl join hotel h on h.hotel_id=rcrl.hotel_id) as hrcrl group by hrcrl.star_category) as t group by t.star_category, t.amount");
					System.out.println("success");
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
					System.out.println("success");
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
	    		System.out.println("Must be a number from 1-8.");
	            //System.out.println(e);
	        }
    	}
    	
    }

}

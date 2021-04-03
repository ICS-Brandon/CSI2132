package lab5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;

public class Main {
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
            
           
            boolean validChoice=false;
            while (!validChoice) {
            	 System.out.println("Select an option to test.");
                 System.out.println("1. Employee Functionality\n2. Customer Functionality\n3. Question 8 Queries\n4. DataAdmin \n5. Exit");
				int choice=Integer.parseInt(reader.readLine().trim());
				switch(choice) {
					case 1:
						validChoice=true;
						Employee test = new Employee();
			            test.employeeMainLoop();
			            break;
			        case 2:
			        	validChoice=true;
						Customer test2 = new Customer();
			            test2.mainCustomerLoop();
			            break;
			        case 3:
			        	validChoice=true;
			        	Question8 test3=new Question8();
			        	test3.answer();
			        	break;
			        case 4:
			        	validChoice=true;
			        	DataAdmin test4=new DataAdmin();
			        	test4.mainAdminLoop();
			  
			        	break;
			        case 5:
			        	validChoice=true;
			  
			        	break;
			        default:
			        	System.out.println("Select valid option.");
			        	
			        	
				}
            }
            
          
        } catch(Exception e){
        	e.printStackTrace();
            System.out.println(e);
        }
    }
}

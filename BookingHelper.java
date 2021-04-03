package DBMS;

import java.time.LocalDate;

public class BookingHelper {

    private LocalDate startDate, endDate;
    int roomId, occupancy, price;

    public BookingHelper(LocalDate sD, LocalDate eD, int id, int occ, int p){
        startDate = sD;
        endDate = eD;
        roomId = id;
        occupancy = occ;
        price = p;
    }

    public BookingHelper(){
        startDate = null;
        endDate = null;
        roomId = 0;
        occupancy = 0;
        price = 0;
    }

    public void setStartDate(LocalDate date){
        startDate = date;
    }

    public void setEndDate(LocalDate date){
        endDate = date;
    }

    public void setRoomId(int id){
        roomId = id;
    }

    public void setOccupancy(int occ){
        occupancy = occ;
    }

    public void setPrice(int p){
        price = p;
    }

    public LocalDate getStartDate(){
        return startDate;
    }

    public LocalDate getEndDate(){
        return endDate;
    }

    public int getRoomId(){
        return roomId;
    }

    public int getOccupancy(){
        return occupancy;
    }

    public int getPrice(){
        return price;
    }
}

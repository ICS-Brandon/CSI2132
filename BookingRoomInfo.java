package com.test;

public class BookingRoomInfo {

    private int roomId, maxSize, price;

    public BookingRoomInfo(int rId, int size, int p){
        roomId = rId;
        maxSize = size;
        price = p;
    }

    public BookingRoomInfo(){
        roomId = 0;
        maxSize = 0;
        price = 0;
    }

    public void setRoomId(int r){
        roomId = r;
    }

    public void setMaxSize(int s){
        maxSize = s;
    }

    public void setPrice(int p){
        price = p;
    }

    public int getRoomId(){
        return roomId;
    }

    public int getMaxSize(){
        return maxSize;
    }

    public int getPrice(){
        return price;
    }
}

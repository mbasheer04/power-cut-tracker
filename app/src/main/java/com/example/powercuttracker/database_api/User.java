package com.example.powercuttracker.database_api;

public class User {

    private int ID;
    private double home_lat;
    private double home_long;
    private String android_id;

    public int getID() {
        return this.ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public double getHome_lat() {
        return this.home_lat;
    }

    public void setHome_lat(double home_lat) {
        this.home_lat = home_lat;
    }

    public double getHome_long() {
        return this.home_long;
    }

    public void setHome_long(double home_long) {
        this.home_long = home_long;
    }

    public String getAndroid_id() {
        return this.android_id;
    }

    public void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

}

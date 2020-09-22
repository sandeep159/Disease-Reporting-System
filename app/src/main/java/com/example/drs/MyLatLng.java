package com.example.drs;

public class MyLatLng {
    private double latitude;
    private double longitude;
    private String AreaName;
    private String Description;
    public MyLatLng(){
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAreaName(String areaName) {
        this.AreaName = areaName;
    }

    public String getAreaName() {
        return AreaName;
    }

    public void setDescription(String description) {
        this.Description = description;
    }

    public String getDescription() {
        return Description;
    }
}


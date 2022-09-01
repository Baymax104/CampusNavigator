package com.example.campusnavigator.model;

import com.amap.api.maps.model.LatLng;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 8:06
 * @Version 1
 */
public class Position {
    private int id;
    private double lat;
    private double lng;
    private String name;

    public Position() {
    }

    public Position(int id, double lat, double lng, String name) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public Position(int id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }

    public Position(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
}

package com.example.campusnavigator.model;

import androidx.annotation.Nullable;

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
    private String markerId;

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

    public int getId() {
        return id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Position) {
            Position other = (Position) obj;
            return id == other.id && lat == other.lat && lng == other.lng;
        }
        return false;
    }

    @Override
    public int hashCode() {
        long lat = Double.doubleToLongBits(this.lat);
        long lng = Double.doubleToLongBits(this.lng);
        int code = 17;
        code = 31 * code + id;
        code = 31 * code + (int) (lat ^ (lat >>> 32));
        code = 31 * code + (int) (lng ^ (lng >>> 32));
        return code;
    }
}

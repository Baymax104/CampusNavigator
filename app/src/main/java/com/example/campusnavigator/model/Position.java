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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Position) {
            Position other = (Position) obj;
            /*
            1.用于判断连接点输入是否重复(name = null)
            2.用于查找时判断键值Position是否相同(name != null)
             */
            return id == other.id && lat == other.lat && lng == other.lng;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 17;
        long lat = Double.doubleToLongBits(this.lat);
        long lng = Double.doubleToLongBits(this.lng);
        code = 31 * code + id;
        code = 31 * code + (int) (lat ^ (lat >>> 32));
        code = 31 * code + (int) (lng ^ (lng >>> 32));
        return code;
    }
}

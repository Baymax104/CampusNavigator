package com.example.campusnavigator.domain;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 8:09
 * @Version 1
 */
public class Path {
    private double length;
    private double spend;
    private int from;
    private int to;

    public Path() {}
    public Path(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public Path(int from, int to,double length) {
        this.length = length;
        this.from = from;
        this.to = to;
    }

    public Path(double spend, int from, int to) {
        this.spend = spend;
        this.from = from;
        this.to = to;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getSpend() {
        return spend;
    }

    public void setSpend(double spend) {
        this.spend = spend;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}

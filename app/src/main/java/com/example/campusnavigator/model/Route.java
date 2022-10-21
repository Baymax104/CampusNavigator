package com.example.campusnavigator.model;

import com.example.campusnavigator.utility.structures.List;

import java.util.Objects;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/18 20:21
 * @Version 1
 */
public class Route implements Comparable<Route> {
    private List<Position> route;
    private Double time;
    private Double dist;

    public Route(List<Position> route, Double time, Double dist) {
        this.route = route;
        this.time = time;
        this.dist = dist;
    }

    public List<Position> getRoute() {
        return route;
    }

    public Double getTime() {
        return time;
    }

    public Double getDist() {
        return dist;
    }

    public static List<Double> extractTime(List<Route> results) {
        List<Double> times = new List<>();
        for (Route result : results) {
            times.push(result.getTime());
        }
        return times;
    }

    public static List<Double> extractDist(List<Route> results) {
        List<Double> distances = new List<>();
        for (Route result : results) {
            distances.push(result.getDist());
        }
        return distances;
    }

    public static List<List<Position>> extractRoute(List<Route> results) {
        List<List<Position>> routes = new List<>();
        for (Route result : results) {
            routes.push(result.getRoute());
        }
        return routes;
    }

    public static List<Position> combineRoute(List<Route> results) {
        List<Position> routes = new List<>();
        for (Route result : results) {
            List<Position> route = result.getRoute();
            for (Position p : route) {
                routes.push(p);
            }
        }
        return routes;
    }

    @Override
    public int compareTo(Route o) {
        if (!Objects.equals(this.time, o.time)) {
            return Double.compare(this.time, o.time);
        }
        if (!Objects.equals(this.dist, o.dist)) {
            return Double.compare(this.dist, o.dist);
        }
        return 0;
    }
}

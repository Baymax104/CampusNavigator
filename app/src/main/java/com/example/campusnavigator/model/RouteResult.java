package com.example.campusnavigator.model;

import com.example.campusnavigator.utility.structures.List;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/18 20:21
 * @Version 1
 */
public class RouteResult {
    private List<Position> route;
    private Double time;
    private Double dist;

    public RouteResult(List<Position> route, Double time, Double dist) {
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

    public static List<Double> extractTime(List<RouteResult> results) {
        List<Double> times = new List<>();
        for (RouteResult result : results) {
            times.add(result.getTime());
        }
        return times;
    }

    public static List<Double> extractDist(List<RouteResult> results) {
        List<Double> distances = new List<>();
        for (RouteResult result : results) {
            distances.add(result.getDist());
        }
        return distances;
    }

    public static List<List<Position>> extractRoute(List<RouteResult> results) {
        List<List<Position>> routes = new List<>();
        for (RouteResult result : results) {
            routes.add(result.getRoute());
        }
        return routes;
    }

    public static List<Position> combineRoute(List<RouteResult> results) {
        List<Position> routes = new List<>();
        for (RouteResult result : results) {
            List<Position> route = result.getRoute();
            for (Position p : route) {
                routes.add(p);
            }
        }
        return routes;
    }
}

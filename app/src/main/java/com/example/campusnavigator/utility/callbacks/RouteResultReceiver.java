package com.example.campusnavigator.utility.callbacks;

import com.example.campusnavigator.model.Route;
import com.example.campusnavigator.utility.structures.List;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 10:02
 * @Version 1
 */
public interface RouteResultReceiver {
    void onSingleRouteReceive(List<Route> results);
    void onMultiRouteReceive(List<Route> results);
}

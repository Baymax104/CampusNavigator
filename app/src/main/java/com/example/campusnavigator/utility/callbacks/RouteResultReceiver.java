package com.example.campusnavigator.utility.callbacks;

import com.example.campusnavigator.model.RouteResult;
import com.example.campusnavigator.utility.structures.List;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 10:02
 * @Version 1
 */
public interface RouteResultReceiver {
    void onSingleRouteReceive(List<RouteResult> results);
    void onMultiRouteReceive(List<RouteResult> results);
}

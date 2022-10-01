package com.example.campusnavigator.utility.callbacks;

import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Tuple;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 10:02
 * @Version 1
 */
public interface RouteResultCallback {
    void onSuccess(List<List<Tuple<Position, Position>>> results, List<Double> distances, List<Double> times, boolean isMultiSpot);
    void onError(Exception e);
}

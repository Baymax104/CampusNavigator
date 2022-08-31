package com.example.campusnavigator.model;

import com.amap.api.maps.AMapUtils;
import com.example.campusnavigator.domain.Path;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 14:38
 * @Version 1
 */
public class PathProvider {
    private Path[] paths;
    private PositionProvider provider = new PositionProvider();

    public PathProvider() {
        paths = new Path[] {
                new Path(7,13,AMapUtils.calculateLineDistance(provider.getPosById(7).getLatLng(),provider.getPosById(13).getLatLng())),
                new Path(12,13, AMapUtils.calculateLineDistance(provider.getPosById(12).getLatLng(),provider.getPosById(13).getLatLng())),
                new Path(13,14, AMapUtils.calculateLineDistance(provider.getPosById(13).getLatLng(),provider.getPosById(14).getLatLng())),
                new Path(12,15, AMapUtils.calculateLineDistance(provider.getPosById(12).getLatLng(),provider.getPosById(15).getLatLng())),
                new Path(15,16, AMapUtils.calculateLineDistance(provider.getPosById(15).getLatLng(),provider.getPosById(16).getLatLng())),
        };
    }
}

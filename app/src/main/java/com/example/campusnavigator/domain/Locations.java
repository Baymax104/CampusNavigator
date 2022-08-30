package com.example.campusnavigator.domain;

import com.amap.api.maps.model.LatLng;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/30 20:10
 * @Version 1
 */
public class Locations {
    // 初始
    private static final double DEFAULT_LAT = 39.8751;
    private static final double DEFAULT_LNG = 116.48134;
    // 美食园
    private static final double MEI_SHI_YUAN_LAT = 39.879667;
    private static final double MEI_SHI_YUAN_LNG = 116.487371;
    // 北门
    private static final double BEI_MEN_LAT = 39.879296;
    private static final double BEI_MEN_LNG = 116.481886;
    // 奥运餐厅
    private static final double AO_YUN_LAT = 39.873738;
    private static final double AO_YUN_LNG = 116.481441;
    // 西门
    private static final double XI_MEN_LAT = 39.877381;
    private static final double XI_MEN_LNG = 116.477648;
    // 南门
    private static final double NAN_MEN_LAT = 39.871657;
    private static final double NAN_MEN_LNG = 116.479716;
    // 东南门
    private static final double DONG_NAN_LAT = 39.871737;
    private static final double DONG_NAN_LNG = 116.485834;
    // 东门
    private static final double DONG_MEN_LAT = 39.877972;
    private static final double DONG_MEN_LNG = 116.483863;
    // 逸夫图书馆
    private static final double LIBRARY_LAT = 39.874924;
    private static final double LIBRARY_LNG = 116.479639;

    public static LatLng getLatLng(String loc) {
        switch (loc) {
            case "XI_MEN":
                return new LatLng(XI_MEN_LAT, XI_MEN_LNG);
            case "BEI_MEN":
                return new LatLng(BEI_MEN_LAT, BEI_MEN_LNG);
            case "NAN_MEN":
                return new LatLng(NAN_MEN_LAT, NAN_MEN_LNG);
            case "DONG_MEN":
                return new LatLng(DONG_MEN_LAT, DONG_MEN_LNG);
            case "DONG_NAN_MEN":
                return new LatLng(DONG_NAN_LAT, DONG_NAN_LNG);
            case "MEI_SHI_YUAN":
                return new LatLng(MEI_SHI_YUAN_LAT, MEI_SHI_YUAN_LNG);
            case "AO_YUN_CAN_TING":
                return new LatLng(AO_YUN_LAT, AO_YUN_LNG);
            case "LIBRARY":
                return new LatLng(LIBRARY_LAT, LIBRARY_LNG);
            case "DEFAULT_LOC":
                return new LatLng(DEFAULT_LAT, DEFAULT_LNG);
            default:
                break;
        }
        return null;
    }
}

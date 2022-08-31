package com.example.campusnavigator.model;

import com.example.campusnavigator.domain.Position;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class PositionProvider {
    private static Position[] positions;

    public PositionProvider() {
        positions = new Position[]{
                new Position(1, Locations.XI_MEN_LAT,Locations.XI_MEN_LNG, "西门"),
                new Position(2, Locations.DONG_MEN_LAT, Locations.DONG_MEN_LNG, "东门"),
                new Position(3, Locations.DONG_NAN_LAT, Locations.DONG_NAN_LNG, "东南门"),
                new Position(4, Locations.BEI_MEN_LAT, Locations.BEI_MEN_LNG, "北门"),
                new Position(5, Locations.AO_YUN_LAT, Locations.AO_YUN_LNG, "奥运餐厅"),
                new Position(6, Locations.LIBRARY_LAT, Locations.LIBRARY_LNG, "逸夫图书馆"),
                new Position(7, Locations.NAN_MEN_LAT, Locations.NAN_MEN_LNG, "南门"),
                new Position(8, Locations.MEI_SHI_YUAN_LAT, Locations.MEI_SHI_YUAN_LNG, "美食园"),
                new Position(9, Locations.TIAN_TIAN_LAT,Locations.TIAN_TIAN_LNG, "天天餐厅"),
                new Position(10, Locations.XIN_XI_LAT, Locations.XIN_XI_LNG, "信息楼"),
                new Position(11, Locations.LAN_QIU_LAT, Locations.LAN_QIU_LNG, "篮球场"),
                new Position(12, Locations.TURN_1_LAT, Locations.TURN_1_LNG),
                new Position(13, Locations.TURN_2_LAT, Locations.TURN_2_LNG),
                new Position(14, Locations.TURN_3_LAT, Locations.TURN_3_LNG),
                new Position(15, Locations.TURN_4_LAT, Locations.TURN_4_LNG),
                new Position(16, Locations.TURN_5_LAT, Locations.TURN_5_LNG),
                new Position(17, Locations.TURN_6_LAT, Locations.TURN_6_LNG),
                new Position(18, Locations.TURN_7_LAT, Locations.TURN_7_LNG),
                new Position(19, Locations.TURN_8_LAT, Locations.TURN_8_LNG),
                new Position(20, Locations.TURN_9_LAT, Locations.TURN_9_LNG),
                new Position(21, Locations.TURN_10_LAT, Locations.TURN_10_LNG),
                new Position(22, Locations.TURN_11_LAT, Locations.TURN_11_LNG),
                new Position(23, Locations.TURN_12_LAT, Locations.TURN_12_LNG),
                new Position(24, Locations.TURN_13_LAT, Locations.TURN_13_LNG),
                new Position(25, Locations.TURN_14_LAT, Locations.TURN_14_LNG),
                new Position(26, Locations.TURN_15_LAT, Locations.TURN_15_LNG),
                new Position(27, Locations.TURN_16_LAT, Locations.TURN_16_LNG),
                new Position(28, Locations.TURN_17_LAT, Locations.TURN_17_LNG),
                new Position(29, Locations.TURN_18_LAT, Locations.TURN_18_LNG),
                new Position(30, Locations.TURN_19_LAT, Locations.TURN_19_LNG),
                new Position(31, Locations.TURN_20_LAT, Locations.TURN_20_LNG),
                new Position(32, Locations.TURN_21_LAT, Locations.TURN_21_LNG),
                new Position(33, Locations.TURN_22_LAT, Locations.TURN_22_LNG),
                new Position(34, Locations.TURN_23_LAT, Locations.TURN_23_LNG),
                new Position(35, Locations.TURN_24_LAT, Locations.TURN_24_LNG),
                new Position(36, Locations.TURN_25_LAT, Locations.TURN_25_LNG),
                new Position(37, Locations.TURN_26_LAT, Locations.TURN_27_LNG),
                new Position(38, Locations.TURN_27_LAT, Locations.TURN_27_LNG),
                new Position(39, Locations.TURN_28_LAT, Locations.TURN_28_LNG),
                new Position(40, Locations.TURN_29_LAT, Locations.TURN_29_LNG),
                new Position(41, Locations.TURN_30_LAT, Locations.TURN_30_LNG),
                new Position(42, Locations.TURN_31_LAT, Locations.TURN_31_LNG),
                new Position(43, Locations.TURN_32_LAT, Locations.TURN_32_LNG),
                new Position(44, Locations.TURN_33_LAT, Locations.TURN_33_LNG),
                new Position(45, Locations.TURN_34_LAT, Locations.TURN_34_LNG),
                new Position(46, Locations.TURN_35_LAT, Locations.TURN_35_LNG),
                new Position(47, Locations.TURN_36_LAT, Locations.TURN_36_LNG),
                new Position(48, Locations.TURN_37_LAT, Locations.TURN_37_LNG),
                new Position(49, Locations.TURN_38_LAT, Locations.TURN_38_LNG),
                new Position(50, Locations.TURN_39_LAT, Locations.TURN_39_LNG),
                new Position(51, Locations.TURN_40_LAT, Locations.TURN_40_LAT),
                new Position(52, Locations.TURN_41_LNG, Locations.TURN_41_LNG),
                new Position(53, Locations.TURN_42_LAT, Locations.TURN_42_LNG),
                new Position(54, Locations.TURN_43_LAT, Locations.TURN_43_LNG),
                new Position(55, Locations.TURN_44_LAT, Locations.TURN_44_LNG),
                new Position(56, Locations.TURN_45_LAT, Locations.TURN_45_LNG),
                new Position(57, Locations.TURN_46_LAT, Locations.TURN_46_LNG),
                new Position(58, Locations.TURN_47_LAT, Locations.TURN_47_LNG),
                new Position(59, Locations.TURN_48_LAT, Locations.TURN_48_LNG)
        };
    }

    public Position[] getPositions() {
        return positions;
    }

    public void setPositions(Position[] positions) {
        PositionProvider.positions = positions;
    }

    public Position getPosByName(String name) {
        for (Position pos : positions) {
            if (pos.getName() != null && pos.getName().equals(name)) {
                return pos;
            }
        }
        return null;
    }

    public Position getPosById(int id) {
        return positions[id-1];
    }
}

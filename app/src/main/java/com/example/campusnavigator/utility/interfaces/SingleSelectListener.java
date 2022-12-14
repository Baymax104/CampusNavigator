package com.example.campusnavigator.utility.interfaces;

import com.example.campusnavigator.model.Position;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 20:27
 * @Version 1
 */
public interface SingleSelectListener {
    void onSingleSelect();
    void onDestReceive(Position dest);
}

package com.adellica.thumbkeyboard;

import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adellica.thumbkeyboard.Layout.TAG;

/**
 * Created by klm on 10/15/17.
 */
public class Config {

    public static int s2c(String color) {
        if(color == null) return 0;
        int c = 0;
        try {
            c = Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error parsing color: " + color);
        }
        return c;
    }

    public int colorBackgroundHolding = s2c("#ffa000a0");
    public int colorBackgroundNonIdle = s2c("#40404040");
    public int colorBackgroundIdle =  s2c("#40404040");
    public int colorLabel = s2c("#ff8080");
    public boolean showLabelsAlways = false;
}

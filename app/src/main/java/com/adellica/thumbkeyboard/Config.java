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
    Map<String, String> cmap = new HashMap<String, String>();

    public String get(final String key) {
        return cmap.get(key);
    }
    public void put(String key, String val) {
        cmap.put(key, val);
        save();
    }

    public String filename() {
        return android.os.Environment.getExternalStorageDirectory()
                + File.separator + "thumb-keyboard" + File.separator + "config";
    }
    public void save() {
        List<String> l = new ArrayList<String>(cmap.keySet());
        Collections.sort(l); Collections.reverse(l);
        Log.i(TAG, "saving layout to " + filename());
        try {
            PrintWriter out = new PrintWriter(filename());
            for (final String key : cmap.keySet()) {
                final String value = cmap.get(key);
                out.println("config " + key + " " + value);
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int s2c(String color) {
        if(color == null) return 0;
        int c = 0;
        try {
            c = Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error parsing color: " + color);
        }
        return c;
    }


    public void handleColor(String input) {
        handle("color." + input);
    }

    // input is eg "config.color #ff00ff00"
    public void handle(String input) {
        Log.e(TAG, "handling config " + input);
        final String key = ThumbkeyboardView.cmd(input);
        put(key, ThumbkeyboardView.value(input));
    }



    public int colorBackgroundHolding() {return s2c(get("color.holding"));}
    public void colorBackgroundHolding(String c) {put("color.holding", c);}
    public int colorBackgroundNonIdle() {return s2c(get("color.active"));}
    public void colorBackgroundNonIdle(String c) {put("color.active", c);}
    public int colorBackgroundIdle() {return s2c(get("color.background"));}
    public void colorBackgroundIdle(String c) {put("color.background", c);}
    public int colorLabel() {return s2c(get("color.label"));}
    public void colorLabel(String c) {cmap.put("color.label", c);}
    public boolean showLabelsAlways() {return "true".equals(cmap.get("label.show"));}
    public void showLabelsAlways(boolean v) {cmap.put("label.show", v ? "true" : "");}
}

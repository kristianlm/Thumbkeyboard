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

    public int colorBackgroundHolding() {return s2c("#a000a0");}
    public int colorBackgroundNonIdle() {return s2c("#404040");}
    public int colorBackgroundIdle() {return s2c("#404040");}
    public int colorLabel() {return s2c("#ff8080");}
    public boolean showLabelsAlways() {return true; }
}

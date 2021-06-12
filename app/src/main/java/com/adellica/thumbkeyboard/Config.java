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

import static com.adellica.thumbkeyboard.FileCopier.TAG;

/**
 * Created by klm on 10/15/17.
 */
public class Config {
    public final int colorBackgroundHolding = s2c("#e0747f80");

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

    public final int colorBackgroundNonIdle = s2c("#e0d7d8d9");
    public final int colorSub = s2c("#e0798591");
    final Map<String, String> cmap = new HashMap<>();
    public int colorBackgroundIdle = s2c("#e0d7d8d9");
    public int colorLabel = s2c("#e0182633");

    public void save() {
        List<String> l = new ArrayList<>(cmap.keySet());
        Collections.sort(l);
        Collections.reverse(l);
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
    public boolean showLabelsAlways = true;

    public static int s2c(String color) {
        if (color == null) return 0;
        int c = 0;
        try {
            c = Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error parsing color: " + color);
        }
        return c;
    }
}

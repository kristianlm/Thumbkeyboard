package com.adellica.thumbkeyboard;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by klm on 12/26/16.
*/
public class Layout {
    public static final String TAG = "TKEY";

    final public String name;
    final Map<String, String> map;
    public Layout(String name) {
        this(name, new HashMap<String, String>());
    }
    public Layout(String name, Map<String, String> dict) {
        this.name = name;
        this.map = dict;
    }

    public String put(String key, String value) {
        // obviously, we need to do this before we save:
        final String result = map.put(key, value);

        List<String> l = new ArrayList<String>(map.keySet());
        Collections.sort(l); Collections.reverse(l);
        String filename = ThumbkeyboardView.layoutname2path(name);
        Log.i(TAG, "saving layout to " + filename);
        try {
            PrintWriter out = new PrintWriter(filename);
            for(String stroke : l) {
                out.println(stroke + map.get(stroke));
                Log.i(TAG, "saved: " + stroke + map.get(stroke));
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String get(String key) {
        return map.get(key);
    }

    public List<String> keys() {
        List<String> l = new ArrayList<String>(map.keySet());
        Collections.sort(l); Collections.reverse(l);
        return l;
    }

    public static Layout fromFile(final String name) {
        final String filename = ThumbkeyboardView.layoutname2path(name);
        Log.i(TAG, "loading layout from file " + filename);
        try {
            String line;
            Map<String, String> map = new HashMap<String, String>();
            InputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                final String pair[] = ThumbkeyboardView.Stroke.parse(line);
                map.put(pair[0], pair[1]);
            }
            return new Layout(name, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    static public Map<String, Layout> loadLayouts() {
        Map<String, Layout> layouts = new HashMap<String, Layout>();

        final File directory = new File(ThumbkeyboardView.layoutnamedir());
        final File[] files = directory.listFiles();
        if(files == null){
            Log.e(TAG, "files is null, no layouts fonud");
            return layouts;
        }
        Log.d(TAG, "Loading config files " + Arrays.asList(files));
        for (int i = 0; i < files.length; i++)
        {
            final String filename = files[i].getName();
            if(filename.endsWith(".chords")) {
                final String name = filename.substring(0, filename.length() - 7);
                Log.i(TAG, "loading chords file " + ThumbkeyboardView.layoutname2path(name) + " as " + name);
                final Layout layout = fromFile(name);
                layouts.put(name, layout);
            }
        }

        return layouts;
    }

}

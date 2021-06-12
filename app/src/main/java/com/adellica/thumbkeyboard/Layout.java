package com.adellica.thumbkeyboard;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Layout is a mapping from a set of Strokes (taps, swipes etc)
 * to an action (eg "key S" or "repeat").
 * Created by klm on 12/26/16.
 */
public class Layout {
    public static final String TAG = "TKEY";

    final public String name;
    final Map<String, String> map;

    public Layout(String name) {
        this(name, new HashMap<>());
    }

    public Layout(String name, Map<String, String> dict) {
        this.name = name;
        this.map = dict;
    }

    static private boolean copyFdToFile(AssetManager am, final String name, final String dir) {
        boolean ok = false;
        InputStream in = null;
        OutputStream out = null;
        Log.i(TAG, "copying " + name + " to " + dir);
        try {
            in = am.open(name);
            layoutname2path(name); // <-- HACK: ensure parent dirs exits
            out = new FileOutputStream(new File(dir + name));
            copyStream(in, out);
            ok = true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + name + " to " + dir, e);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

    public static String layoutname2path(final String name) {
        final String dir = ThumbkeyboardView.configDir();
        new File(dir).mkdirs();
        return dir + name + ".chords";
    }

    public String get(String key) {
        return map.get(key);
    }

    static private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static Layout fromFile(final String name) {
        final String filename = layoutname2path(name);
        Log.i(TAG, "loading layout from file " + filename);
        try {
            String line;
            Map<String, String> map = new HashMap<>();
            InputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                final String stroke = Stroke.parse(line);
                map.put(stroke, "unknown");
            }
            return new Layout(name, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public void ensureExists(AssetManager am, String name) {
        final File file = new File(ThumbkeyboardView.configDir() + name);

        if (file.exists()) return;
        copyFdToFile(am, name, ThumbkeyboardView.configDir());
    }

    static public Map<String, Layout> loadLayouts(AssetManager am) {
        Map<String, Layout> layouts = new HashMap<>();

        ensureExists(am, "default.chords");
        ensureExists(am, "num.chords");

        final File directory = new File(ThumbkeyboardView.configDir());
        File[] files = directory.listFiles();
        if (files == null) files = new File[]{};

        Log.d(TAG, "Loading config files " + Arrays.asList(files));
        for (File file : files) {
            final String filename = file.getName();
            if (filename.endsWith(".chords")) {
                final String name = filename.substring(0, filename.length() - 7);
                Log.i(TAG, "loading chords file " + layoutname2path(name) + " as " + name);
                final Layout layout = fromFile(name);
                layouts.put(name, layout);
            }
        }

        return layouts;
    }

    public String put(String key, String value) {
        // obviously, we need to do this before we save:
        final String result = map.put(key, value);

        List<String> l = new ArrayList<>(map.keySet());
        Collections.sort(l);
        Collections.reverse(l);
        String filename = layoutname2path(name);
        Log.i(TAG, "saving layout to " + filename);
        try {
            PrintWriter out = new PrintWriter(filename);
            for (String stroke : l) {
                out.println(stroke + map.get(stroke));
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> keys() {
        List<String> l = new ArrayList<>(map.keySet());
        Collections.sort(l);
        Collections.reverse(l);
        return l;
    }

}

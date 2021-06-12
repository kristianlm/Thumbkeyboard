package com.adellica.thumbkeyboard;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopier {
    public static final String TAG = "TKEY";

    static private boolean copyFdToFile(AssetManager am, final String name, final String dir) {
        boolean ok = false;
        InputStream in = null;
        OutputStream out = null;
        Log.i(TAG, "copying " + name + " to " + dir);
        try {
            in = am.open(name);
            new File(ThumbkeyboardView.configDir()).mkdirs();
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

    static private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    static public void ensureExists(AssetManager am, String name) {
        final File file = new File(ThumbkeyboardView.configDir() + name);

        if (file.exists()) return;
        copyFdToFile(am, name, ThumbkeyboardView.configDir());
    }
}

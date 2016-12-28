package com.adellica.thumbkeyboard;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by klm on 9/27/16.
 */
public class ThumbkeyboardView extends View {
    private static final String TAG = "TKEY";
    public ThumbkeyboardIME Ime;
    private boolean showHelp = false;
    private final int MAX_DELAY_DOUBLE_COMBO = 60; // ms
    private static final int BLOB_RADIUS = 40; // dpi
    private static final int BLOB_BORDER = 4; // dpi


    // utils
    String readBackwardsUntil(String p, boolean eof) {
        final InputConnection ic = Ime.getCurrentInputConnection();
        if(ic == null) return null;
        int size = 32;
        String c = null;
        while(size < 4096) {
            c = ic.getTextBeforeCursor(size, 0).toString();
            int idx = c.lastIndexOf(p);
            if(idx >= 0) { return c.substring(idx + 1); }
            size *= 2;
        }
        return eof ? c : null;
    }
    String readForwardsUntil(String p, boolean eof) {
        final InputConnection ic = Ime.getCurrentInputConnection();
        if(ic == null) return null;
        int size = 32;
        String c = null;
        while(size < 4096) {
            c = ic.getTextAfterCursor(size, 0).toString();
            int idx = c.indexOf(p);
            if(idx >= 0) { return c.substring(0, idx); }
            size *= 2;
        }
        return eof ? c : null;
    }

    private static class MutableDouble {
        public double value;

        public MutableDouble(double i) {
            this.value = i;
        }
    }

    private static class Modifiers {
        public final static int
                Shift = 0x01,
                Ctrl = 0x02,
                Alt = 0x04,
                Meta = 0x08; // I don't now what this one is but it's in KeyEvent. Maybe win/option/command
    }

    private int _ModifierMask = 0;
    private void modifiersClear() {
        _ModifierMask = 0;
    }

    private void modShift(boolean down_p) { _ModifierMask = down_p?(_ModifierMask|Modifiers.Shift):(_ModifierMask&~Modifiers.Shift); }
    private void modCtrl(boolean down_p)  { _ModifierMask = down_p?(_ModifierMask|Modifiers.Ctrl):(_ModifierMask&~Modifiers.Ctrl); }
    private void modAlt(boolean down_p)   { _ModifierMask = down_p?(_ModifierMask|Modifiers.Alt):(_ModifierMask&~Modifiers.Alt); }
    private void modMeta(boolean down_p)  { _ModifierMask = down_p?(_ModifierMask|Modifiers.Meta):(_ModifierMask&~Modifiers.Meta); }
    private  boolean modMeta()  { return (_ModifierMask & Modifiers.Meta) != 0;  }
    private  boolean modAlt()   { return (_ModifierMask & Modifiers.Alt) != 0;  }
    private  boolean modCtrl()  { return (_ModifierMask & Modifiers.Ctrl) != 0;  }
    private  boolean modShift() { return (_ModifierMask & Modifiers.Shift) != 0;  }

    public ThumbkeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "WDOIWAJDOWAUHDWADWAOIDJWAODHWAODJWAOIDJOWAIJDWAOIDJWAOIJDOWA");
        layouts = Layout.loadLayouts(getContext().getAssets());
    }

    class Blob {
        // dpi coordinates (negative counts from right/bottom)
        final private int col;
        final private int row;
        final private int bid;
        public boolean holding = false;
        public boolean tapping = false;


        final Paint fill = new Paint();

        Blob(int bid, int col, int row) {
            this.bid = bid;
            this.col = col;
            this.row = row;
            this.fill.setStyle(Paint.Style.FILL);
        }
        public double dist2(double x, double y) {
            return  (this.x() - x) * (this.x() - x) +
                    (this.y() - y) * (this.y() - y);
        }

        // screen coordinates
        public float x() { return (col < 0 ? getWidth() : 0) + pixels(col * (BS * 2) + BS); }
        public float y() { return anchorY() + (pixels(row * (BS * 2) + BS)); }

        public int bid() { return bid; }

        public void draw(Canvas canvas, boolean idle, final String label) {
            if(idle)
                if(holding) fill.setColor(Color.argb(0xB0, 0x00, 0x80, 0xff));
                else        fill.setColor(Color.argb(0x40, 0x00, 0xff, 0xff));
            else            fill.setColor(Color.argb(0x30, 0x00, 0xff, 0xff));

            final int S = pixels(BLOB_RADIUS - BLOB_BORDER);
            if(bid() == 1)
                canvas.drawCircle(x(), y(), pixels(BS), fill);
            else
                canvas.drawRect(x()-S, y()-S, x()+S, y()+S, fill);


            final TextPaint p = new TextPaint();
            final int PBS = pixels(BS);
            final int y = Math.min(canvas.getWidth(), canvas.getHeight());

            final Paint background = new Paint();
            background.setColor(Color.argb(0, 0, 0, 0));

            StaticLayout textLayout = new StaticLayout(label, p, PBS * 2,
                    android.text.Layout.Alignment.ALIGN_CENTER, 1.8f, 0.0f, false);
            p.setTypeface(Typeface.MONOSPACE);
            p.setTextSize(y / 36);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(0xC0, 0xff, 0x00, 0x00));
            canvas.save();
            canvas.translate(x() - PBS, y());
            textLayout.draw(canvas);
            canvas.restore();
        }
    }




    private int __anchorY = -1;
    // input: y screen coordinate of top of keyboard
    private void anchorY(int newValue) {
        __anchorY = newValue;
        postInvalidate();
    }
    // screen coordinates of top of top-most button
    private int anchorY() {
        if(__anchorY < 0)
            return getHeight() - pixels(BS*2*3);
        else
            return __anchorY;
    }

    final int BS = BLOB_RADIUS;
    static final int BB = BLOB_BORDER * 2; // wall margin
    // negative positions means right/bottom-aligned
    Blob [] _blobs = new Blob [] {
            //       idx col row
            new Blob( 0,  0, 0),
            new Blob( 1,  1, 0),
            new Blob( 2, -2, 0),
            new Blob( 3, -1, 0),
            new Blob( 4,  0, 1),
            new Blob( 5,  1, 1),
            new Blob( 6, -2, 1),
            new Blob( 7, -1, 1),
            new Blob( 8,  0, 2),
            new Blob( 9,  1, 2),
            new Blob(10, -2, 2),
            new Blob(11, -1, 2),
    };
    private Blob [] blobs () { return _blobs; }


    static class Stroke {
        public int taps[];
        public int ups[];
        public int downs[];
        public int lefts[];
        public int rights[];

        public Stroke(int len) {
            taps = new int[len];
            ups = new int[len];
            downs = new int[len];
            lefts = new int[len];
            rights = new int[len];

        }
        public void clear() {
            for(int i = 0 ; i < taps.length ; i++) {
                taps[i] = 0;
                ups[i] = 0;
                downs[i] = 0;
                lefts[i] = 0;
                rights[i] = 0;
            }
        }

        @Override
        public String toString() {
            String s = "";
            for(int j = 0 ; j < taps.length ; j += 4) {
                s += ""
                    + taps[j + 0] + lefts[j+0] + ups[j+0] + downs[j+0] + rights[j+0] + "-"
                    + taps[j + 1] + lefts[j+1] + ups[j+1] + downs[j+1] + rights[j+1]
                    + ":"
                    + taps[j + 2] + lefts[j+2] + ups[j+2] + downs[j+2] + rights[j+2] + "-"
                    + taps[j + 3] + lefts[j+3] + ups[j+3] + downs[j+3] + rights[j+3]
                    + " ";
            }
            return s;
        }
        // read a line like 00000-00000:00000-00000 00000-00000:00000-00000 00100-00000:00000-00000 key DPAD_UP
        // and return it's stroke part and its token-part. This is tightly connected to Stroke.toString implementation.
        public static String[] parse(final String line) {
            if(line.length() >= 72) {
                String stroke = line.substring(0, 72);
                String token = line.substring(72);
                return new String [] {stroke, token};
            }
            return null;
        }

        public void copyFrom(Stroke stroke) {
            if(stroke.taps.length != taps.length)
                throw new RuntimeException("stroke size mismatch");
            for(int i = 0 ; i < taps.length ; i++) {
                taps[i] = stroke.taps[i];
                lefts[i] = stroke.lefts[i];
                ups[i] = stroke.ups[i];
                downs[i] = stroke.downs[i];
                rights[i] = stroke.rights[i];
            }
        }
    }
    Stroke stroke = new Stroke(blobs().length);

    private int touch2blob(double x, double y, MutableDouble closestDist) {
        int nearest = 0; // index
        double dist2 = blobs()[nearest].dist2(x, y);
        for(int bid = 1 ; bid < blobs().length ; bid++) {
            double dist = blobs()[bid].dist2(x, y);
            if(dist < dist2) {
                dist2 = dist;
                nearest = bid;
            }
        }
        if(closestDist != null)
            closestDist.value = Math.sqrt(dist2);
        return nearest;
    }

    private int touch2blob(double x, double y) {
        return touch2blob(x, y, null);
    }

    /**
     * The superlayout is read-only
     * @return
     */
    public Layout superLayout() {
        HashMap m = new HashMap<String, String>();
        m.put("10000-00000:00000-00000 00000-00000:00000-00000 00000-00000:00000-00000 ", "stroke write");
        m.put("00000-00000:00000-00000 10000-00000:00000-00000 00000-00000:00000-00000 ", "stroke record");
        m.put("00000-00000:00000-00000 00000-00000:00000-00000 10000-00000:00000-00000 ", "dump raw actions");
        m.put("00000-00000:00000-00000 00000-00000:00000-00000 00000-10000:00000-00000 ", "dump layout");
        m.put("00000-00000:00000-10000 00000-00000:00000-00000 00000-00000:00000-00000 ", "layout default");
        m.put("00000-00000:00000-00000 00000-00000:10000-00000 00000-00000:00000-00000 ", "layout num");
        m.put("00000-00000:00000-00000 00000-00000:00000-10000 00000-00000:00000-00000 ", "layout term");
        m.put("00000-00000:00000-00000 00000-00000:00000-00000 00000-00000:10000-00000 ", "layout user1");
        m.put("00000-00000:00000-00000 00000-00000:00000-00000 00000-00000:00000-10000 ", "layout user2");
        return new Layout("supert", m);
    }

    boolean __write_stroke = false;
    boolean __stroke_record = false;
    boolean __superlayout = false;

    void _write_stroke(boolean setting) { __write_stroke = setting; postInvalidate(); };
    boolean _write_stroke() { return __write_stroke; };

    void _stroke_record(boolean setting) { __stroke_record = setting; postInvalidate(); };
    boolean _stroke_record() { return __stroke_record; };

    void _superlayout(boolean setting) { __superlayout = setting; postInvalidate(); };
    boolean _superlayout() { return __superlayout; };

    private void handlePattern(final String p) {
        if(p == null) return;
        // super-button (puts into superlayout)
        if("00000-00000:10000-00000 00000-00000:00000-00000 00000-00000:00000-00000 ".equals(p)) {
            _superlayout(!_superlayout());
        } else if(_write_stroke()) {
            _write_stroke(false);
            final Layout layout = currentLayout();
            final String token = layout.get(p);
            handleInput("\n" + token); // spit out stroke action
        } else if(_stroke_record()) {
            _stroke_record(false);
            final String line = readBackwardsUntil("\n", true) + readForwardsUntil("\n", true);
            Log.d(TAG, "storing " + p + " as \"" + line + "\"");
            currentLayout().put(p, line);
        } else {
            final Layout layout = _superlayout() ? superLayout() : currentLayout();
            _superlayout(false);
            final String token = layout.get(p);
            Log.i(TAG, "handling: " + token);
            if(token != null)
                handleToken(token);
        }
    }

    /**
     * @param token
     * @return just the command part of a token
     */
    private String cmd(String token) {
        int idx = token.indexOf(' ');

        if(idx >= 0)
            return token.substring(0, idx);
        else
            // no space means we've got an unparamterized command
            return token;
    }

    /**
     *
     * @param token
     * @return just the arguments/value part of a token
     */
    private String value(String token) {
        int idx = token.indexOf(' ');
        if(idx >= 0)
            return token.substring(idx + 1);
        else
            // no space means we've got an unparameterized command
            return null;
    }


    private String lastToken;
    private void handleToken(String t) {
        String cmd = cmd(t);

        if("shift".equals(cmd)) {
            handleShift(value(t));
        } else if("ctrl".equals(cmd)) {
            handleCtrl(value(t));
        } else if("alt".equals(cmd)) {
            handleAlt(value(t));
        } else if("meta".equals(cmd)) {
            handleMeta(value(t));
        } else if ("help".equals(cmd)) {
            showHelp = !showHelp;
            postInvalidate();
        } else if("repeat".equals(cmd)) {
            if(lastToken != null) {
                Log.d(TAG, "repeating with " + lastToken);
                if (!"repeat".equals(cmd(lastToken))) {
                    handleToken(lastToken);
                } else
                    Log.e(TAG, "error! trying to repeat the repeat command!");
            }
        } else if ("key".equals(cmd)) {
            handleKey(value(t));
        } else if ("input".equals(cmd)) {
            handleInput(value(t));
        } else if ("stroke".equals(cmd)) {
            if ("write".equals(value(t))) {
                _write_stroke(true);
            } else if ("read".equals(value(t))) {
                String line = readBackwardsUntil("\n", true) + readForwardsUntil("\n", true);
                Log.i(TAG, "<<< " + line);
                String[] pair = Stroke.parse(line);
                currentLayout().put(pair[0], pair[1]);
            } else if ("record".equals(value(t))) {
                _stroke_record(true);
            } else {
                Log.i(TAG, "Don't know how to handle " + t);
            }
        } else if("dump".equals(cmd)) {
            final String cmd2 = cmd(value(t));
            Log.i(TAG, "dump: '" + value(t) + "' or   '" + value(value(t)) + "'");
            if("raw".equals(cmd2)) {
                dumpAsset(value(value(t)));
            } else if("layout".equals(cmd2)) {
                Log.e(TAG, "TODO: dump layout");
            }
        } else if("layout".equals(cmd)){
            currentLayoutName(value(t));
        } else if("delete".equals(cmd)) {
            if("line".equals(value(t))) {
                if(deleteSurroundingUntil("\n", true, "\n", true, false, true).isEmpty()) {
                    // we didn't actually delete anything
                    InputConnection ic = Ime.getCurrentInputConnection();
                    if(ic != null)
                        ic.deleteSurroundingText(1, 0); // line is empty, delete up to previous line
                }
            } else if("word".equals(value(t))) {
                // what a mess:
                if(readBackwardsUntil(" ", true).isEmpty()) //     ,------,--- delete ending space
                    deleteSurroundingUntil(" ", true, " ", true, false, true);
                else //                                            ,------,--- delete starting space
                    deleteSurroundingUntil(" ", true, " ", true, true, false);

            }
        } else if("debug".equals(cmd)) {
            Layout layout = currentLayout();
            for(String stroke : layout.keys()) {
                Log.i(TAG, "??? " + stroke + layout.get(stroke));
            }
        }

        if (!"repeat".equals(cmd)) // avoid infinite recursion
            lastToken = t;
    }

    /**
     * Dump filename (from asset folder) into current editing session.
     * @param filename
     */
    private void dumpAsset(String filename) {
        try {
            Log.i(TAG, "dumping asset '" + filename + "' to edit session");
            BufferedReader in = new BufferedReader(new InputStreamReader(getContext().getAssets().open(filename)));
            String line;
            while((line = in.readLine()) != null) {
                handleInput(line + "\n");
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean deleteSurroundingText(int before, int after) {
        final InputConnection ic = Ime.getCurrentInputConnection();
        if(ic != null) {
            ic.deleteSurroundingText(before, after);
            return true;
        }
        return false;
    }

    /**
     * This guy isn't great. You can't specify SPACE|ENTER. So, for example,
     * a word boundary can only be " " and not " |\n|\t" which is very limiting.
     * @return The string that was deleted
     */
    private String deleteSurroundingUntil(final String pre, boolean bof, final String post, boolean eof, boolean trimLeft, boolean trimRight) {
        final String preline = readBackwardsUntil(pre, bof);
        final String postline = readForwardsUntil(post, eof);
        if(deleteSurroundingText(
                preline == null ? 0 : preline.length()   + (trimLeft  ? 1 : 0),
                postline == null ? 0 : postline.length() + (trimRight ? 1 : 0)))
            return preline + postline;
        return "";
    }

    String _currentLayoutName = "default";
    public String currentLayoutName() { return _currentLayoutName; }
    public void currentLayoutName(String name) {
        Log.i(TAG, "current layout is now \"" + name + "\" ( was \"" + _currentLayoutName + "\")");
        _currentLayoutName = name;
        postInvalidate();
    }

    // never return null because that would force nullpointer-checks
    // on every other line in this file.
    private Layout currentLayout() {
        final Layout l = layouts.get(currentLayoutName());
        // layout name not found, create it!
        if(l == null) {
            Layout layout = new Layout(currentLayoutName());
            layouts.put(currentLayoutName(), layout);
            return layout;
        } else
            return l;
    }

    private void handleShift(String param) {
        if(param == null || "".equals(param))
            modShift(!modShift());
        else {
            boolean old = modShift();
            modShift(true);
            handleToken("key " + param);
            modShift(old);
        }
    }

    private void handleCtrl(String param) {
        if(param == null || "".equals(param))
            modCtrl(!modCtrl());
        else {
            boolean old = modCtrl();
            modCtrl(true);
            handleToken("key " + param);
            modCtrl(old);
        }
    }

    private void handleAlt(String param) {
        if(param == null || "".equals(param))
            modAlt(!modAlt());
        else {
            boolean old = modAlt();
            modAlt(true);
            handleToken("key " + param);
            modAlt(old);
        }
    }

    private void handleMeta(String param) {
        if(param == null || "".equals(param))
            modMeta(!modMeta());
        else {
            boolean old = modMeta();
            modMeta(true);
            handleToken("key " + param);
            modMeta(old);
        }
    }

    private void handleInput(String input) {
        if(modShift())
            Ime.getCurrentInputConnection().commitText(input.toUpperCase(), 0);
        else
            Ime.getCurrentInputConnection().commitText(input, 0);

        modifiersClear();
    }

    private int getMetaState() {
        int meta = 0;
        if (modShift()) meta |= KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        if (modCtrl())  meta |= KeyEvent.META_CTRL_ON  | KeyEvent.META_CTRL_LEFT_ON;
        if (modAlt())   meta |= KeyEvent.META_ALT_ON   | KeyEvent.META_ALT_LEFT_ON;
        if (modMeta())  meta |= KeyEvent.META_META_ON  | KeyEvent.META_META_LEFT_ON;
        return meta;
    }

    private void handleKey(String key) {

        final int keycode = ThumboardKeycodes.string2keycode(key);
        int meta = getMetaState();
        if (keycode != 0) {
            long now = System.currentTimeMillis();
            final InputConnection ic = Ime.getCurrentInputConnection();
            if(ic != null) {
                ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0, meta));
                ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,   keycode, 0, meta));
            } else Log.e(TAG, "obs: current input connection is null");
        }

        modifiersClear();
    }

    boolean holding = false;
    Blob [] fingerTouches = new Blob [ 4 ]; // who'se got 4 thumbs anyway?


    Map<String, Layout> layouts = new HashMap<String, Layout>();

    private boolean hidden = false;
    private void hide() {
        // Ime.requestHideSelf(0); // <-- does a silly animation
        Ime.setCandidatesViewShown(false);
    }
    private void show() {
        Log.i(TAG, "SHOWING");
        Ime.setCandidatesViewShown(true);
    }

    // used to indicate (when >= 0) whether we're sliding
    // the keyboard placement (anchor) up/down.
    private int anchorFinger = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int i = event.getActionIndex(); // index of finger that caused the down event

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: holding = true; break;
            case MotionEvent.ACTION_UP: holding = false; break;
        }

        if(event.getPointerId(i) >= fingerTouches.length) {
            Log.e(TAG, "only up to " + fingerTouches.length + " simultanious fingers supported ...");
            return false;
        }

        MutableDouble d = new MutableDouble(-1);
        touch2blob(event.getX(i), event.getY(i), d);
        // we're pressing "outside" of keyboard, hide it
        if(anchorFinger < 0 && d.value > pixels((int)(BLOB_RADIUS * 1.5))) {
            flushStroke();
            postInvalidate();
            hide();
        }

        switch(event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: { // primary finger down!
                final int btn = touch2blob(event.getX(i), event.getY(i));
                if(btn == 1) { // anchor button. no layouts for this guy
                    anchorFinger = i;
                } else if(btn >= 0) {
                    blobs()[btn].tapping = true;
                    blobs()[btn].holding = true;
                    postInvalidate();
                }
                break; }

            case MotionEvent.ACTION_POINTER_DOWN: { // another finger down while holding one down
                if(anchorFinger >= 0) break; // moving anchor, cancel
                final int btn = touch2blob(event.getX(i), event.getY(i));
                if(btn >= 0) {
                    blobs()[btn].tapping = true;
                    blobs()[btn].holding = true;
                    postInvalidate();
                }
                break; }

            case MotionEvent.ACTION_POINTER_UP: { // finger up, still holding one down
                if(anchorFinger >= 0) break;
                final int btn = touch2blob(event.getX(i), event.getY(i));
                if(btn >= 0) {
                    if (blobs()[btn].tapping)
                        stroke.taps[btn]++;
                    blobs()[btn].holding = false;
                    postInvalidate();
                }
                break; }
            case MotionEvent.ACTION_MOVE: {
                if(anchorFinger >= 0) {
                    anchorY((int)event.getY(anchorFinger) - pixels(BS));
                    break;
                }
                for( int j = 0 ; j < event.getPointerCount() ; j++) {
                    final Blob btn = blobs()[touch2blob(event.getX(j), event.getY(j))]; // <-- going to
                    if(btn.bid() >= 0) {
                        if (btn != fingerTouches[event.getPointerId(j)]) {
                            final int fid = event.getPointerId(j);
                            final Blob old = fingerTouches[fid]; // <-- coming from
                            if (old != null) {
                                old.holding = false;
                                btn.holding = true;

                                final int bid = old.bid();
                                int ox = old.bid() % 4, oy = old.bid() / 4;
                                int nx = btn.bid() % 4, ny = btn.bid() / 4;
                                int dx = nx - ox, dy = ny - oy;
                                Log.i(TAG, "swipe on " + bid + ": " + dx + "," + dy);
                                int table[] = (dx == 0
                                        ? (dy == 1 ? stroke.downs : stroke.ups)
                                        : (dx == 1 ? stroke.rights : stroke.lefts));
                                table[bid]++;
                                old.tapping = false; // this is no longer a tap
                                postInvalidate();
                            }
                            fingerTouches[fid] = btn;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: { // all fingers up! obs: last finger up ?≠ first finger down
                final int btn = touch2blob(event.getX(i), event.getY(i));
                if(btn >= 0) {
                    if (blobs()[btn].tapping)
                        stroke.taps[btn]++;
                    String pattern = stroke.toString();
                    Log.i(TAG, "xxx " + pattern);
                    for (String p : pattern.split(" ")) {
                        Log.i(TAG, "#   " + p);
                    }
                    flushStroke();
                    handlePattern(pattern);
                    postInvalidate();
                }
                anchorFinger = -1;
                show();
                break; }
            default:
                //Log.d(TAG, "missed event " + event.getActionMasked());
                break;
        }
        return true;
    }

    private void flushStroke() {
        stroke.clear();
        for(int j = 0 ; j < fingerTouches.length ; j++) fingerTouches[j] = null;
        for(int j = 0 ; j < blobs().length ; j++) {
            blobs()[j].tapping = false;
            blobs()[j].holding = false;
        }
    }

    private int pixels(int dpi) {
        Resources r = getResources();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, r.getDisplayMetrics());
    }


    public Layout shownLayout() {
        if(_superlayout()) return superLayout();
        else return currentLayout();
    }

    /**
     * Tricky business! Going from Blob index to the Blob index
     * of a nextdoor Blob (from delta).
     * @param bid index of button
     * @param dx delta to move in x direction (should be 0 if dy ≠ 0)
     * @param dy delta to move in y direction (should be 0 if dx ≠ 0)
     * @return -1 if outside 4x3 Blob grid
     */
    public static int Blobdelta(int bid, int dx, int dy) {
        int x = (bid % 4) + dx;
        int y = (bid / 4) + dy;
        //Log.i(TAG, "bid " + bid + " + " + dx + "," + dy + "  = " + x + "," + y + "  ");
        if(y >= 0 && y <= 2)
            if(x >= 0 && x <= 3)
                return (y * 4) + x;
        return -1;
    }

    /**
     * This is supertricky and a complete mess.
     * @param bid the index of the Blob that you're looking at (the one we're trying to label)
     * @param original the unmodified stroke
     * @param stroke our temp stroke
     * @param i array of inverse direction of dx,dy
     * @param dx on Blob grid, +1 is right, -1 is left
     * @param dy on Blob grid, +1 is down, -1 is up
     * @return
     */
    public String strokeTry(int bid, Stroke original, Stroke stroke, int [] i, int dx, int dy) {
        stroke.copyFrom(original);
        int j = Blobdelta(bid, dx, dy);
        if(j >= 0 && blobs()[j].holding) {
            i[j]++;
            return shownLayout().get(stroke.toString());
        }
        return null;
    }

    Stroke tempStroke = new Stroke(blobs().length);
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Paint cp1 = new Paint(), cp2 = new Paint();
        cp1.setColor(Color.argb(0x80, 0, 0, 0));
        cp1.setPathEffect(new DashPathEffect(new float[]{7, 7}, 0));
        cp1.setStyle(Paint.Style.STROKE);
        cp1.setStrokeWidth(4);
        cp2.setColor(Color.argb(0x80, 0xff, 0xff, 0xff));
        cp2.setPathEffect(new DashPathEffect(new float[]{7, 7}, 7));
        cp2.setStyle(Paint.Style.STROKE);
        cp2.setStrokeWidth(4);

        Blob bs [] = blobs();
        boolean any = false; // <-- anybody being pressed?
        for (int i = 0 ; i < bs.length ; i++) {
            any |= bs[i].holding;
        }

        for (int i = 0 ; i < bs.length ; i++) {
            tempStroke.copyFrom(stroke);
            tempStroke.taps[i] ++; // <-- pretend we tapped current
            for(int j = 0 ; j < blobs().length ; j++)
                if(blobs()[j].tapping)
                    tempStroke.taps[j]++; // <-- pretend we tapped held buttons

            String token = shownLayout().get(tempStroke.toString());
            if(any) { // try surrounding swipes
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.downs,    0,-1);
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.ups,  0, 1);
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.rights, -1, 0);
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.lefts, 1, 0);
            }
            if(i == 2 && _write_stroke()) token = "✏"; // pencil icon
            bs[i].draw(canvas, any, token == null ? "" : prettify(token));

            if(i == 2 && token == null) {
                if(_stroke_record()) {
                    final Paint red = new Paint();
                    red.setStyle(Paint.Style.FILL);
                    red.setColor(Color.argb(0xe0, 0xff, 0, 0));
                    canvas.drawCircle(bs[i].x(), bs[i].y(), pixels(BS / 4), red);
                } else if(_superlayout()) {
                    final Paint red = new Paint();
                    red.setStyle(Paint.Style.FILL);
                    red.setColor(Color.argb(0xe0, 0, 0xff, 0xff));
                    canvas.drawCircle(bs[i].x(), bs[i].y(), pixels(BS / 4), red);
                } else {
                    final Paint red = new Paint();
                    red.setStyle(Paint.Style.STROKE);
                    red.setStrokeWidth(pixels(3));
                    red.setColor(Color.argb(0xe0, 0, 0xff, 0xff));
                    canvas.drawCircle(bs[i].x(), bs[i].y(), pixels(BS / 4), red);
                }
            }
        }

    }

    // describe a token to the user (lower case letter? special command?).
    // we can make this as pretty as we want.
    private String prettify(final String token) {
        if("key SPACE".equals(token)) return "␣";
        if("repeat".equals(token)) return "↺";
        if("key DEL".equals(token)) return "⇐";
        if("key ENTER".equals(token)) return "⏎";
        if("key DPAD_LEFT".equals(token)) return "←";
        if("key DPAD_UP".equals(token)) return "↑";
        if("key DPAD_RIGHT".equals(token)) return "→";
        if("key DPAD_DOWN".equals(token)) return "↓";
        if(token.startsWith("key ")) {
            final String key = token.substring(4);
            return key.length() == 1
                    ? (modShift() ? key.toUpperCase() : key.toLowerCase()) // eg "key S"
                    : key; // eg "key DEL"
        }
        return token; // eg "input å"
    }

}

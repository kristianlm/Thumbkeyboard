package com.adellica.thumbkeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.adellica.thumbkeyboard.tsm.Keycodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
    private static final int BLOB_RADIUS = 38; // dpi
    private static final int BLOB_BORDER = 4; // dpi

    public boolean overlay = false;

    public static String configDir() {
        return android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + "thumb-keyboard"
                + File.separator;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(!overlay) {
            // having a height while being overlay causes it to raise other content.
            // when we don't have a height we can be overlayd properly because our height is the full screen
            // or something like that. I don't really get this stuff but I don't think you're supposed to.
            if (getLayoutParams() != null)
                getLayoutParams().height = pixels(BLOB_RADIUS * 2/*diameter*/ * 3/*rows*/);
        }
    }

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
        Log.i(TAG, "no config here");
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

        public void draw(Canvas canvas, final String label) {
            if(holding) fill.setColor(Ime.config.colorBackgroundHolding);
            else        fill.setColor(Ime.config.colorBackgroundNonIdle);

            final int S = pixels(BLOB_RADIUS - BLOB_BORDER);
            canvas.drawRect(x()-S, y()-S, x()+S, y()+S, fill);

            if(label != null) {
                final TextPaint p = new TextPaint();
                final int PBS = pixels(BS);
                final int y = Math.min(canvas.getWidth(), canvas.getHeight());

                p.setTypeface(Typeface.MONOSPACE);
                p.setAntiAlias(true);
                p.setTextSize(y / 16);

                p.setStyle(Paint.Style.FILL);
                p.setColor(Ime.config.colorLabel);
                canvas.save();
                canvas.translate(x(), y()); // anchor to center of rectangle

                float txtWidth = p.measureText(label);
                if (txtWidth > PBS * 2) { // text is too big for button!
                    p.setTextScaleX(0.9f / (txtWidth / (PBS * 2))); // fit width
                }
                canvas.drawText(label, -(txtWidth * p.getTextScaleX()) / 2, 0, p);
                canvas.restore();
            }
        }
    }




    private int __anchorY = -1;
    // input: y screen coordinate of top of keyboard
    private void anchorY(int newValue) {
        __anchorY = newValue;
        removeCallbacks(redraw);
        postDelayed(redraw, draw_delay);
        //postInvalidateDelayed(1000);
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

    private void handlePattern(final Stroke stroke) {
        final Stroke t = new Stroke(stroke.lefts.length);
        t.copyFrom(stroke);
        Ime.handleStroke(t);
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

    boolean holding = false;
    Blob [] fingerTouches = new Blob [ 4 ]; // who'se got 4 thumbs anyway?


    Map<String, Layout> layouts = new HashMap<String, Layout>();

    private boolean hidden = false;
    private void hide() {
        // Ime.requestHideSelf(0); // <-- does a silly animation
        hidden = true;
        Ime.setCandidatesViewShown(!hidden);
    }
    private void show() {
        Log.i(TAG, "SHOWING");
        hidden = false;
        Ime.setCandidatesViewShown(!hidden);
    }
    private boolean hidden() { return hidden ; }

    // used to indicate (when >= 0) whether we're sliding
    // the keyboard placement (anchor) up/down.

    private Runnable redraw = () -> postInvalidate();
    private long draw_delay = 50;
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
        if(d.value > pixels((int)(BLOB_RADIUS * 1.5))) {
            flushStroke();
            removeCallbacks(redraw);
            postDelayed(redraw, draw_delay);
            hide();
        }

        switch(event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: { // primary finger down!
                final int btn = touch2blob(event.getX(i), event.getY(i));
                if(btn >= 0) {
                    blobs()[btn].tapping = true;
                    blobs()[btn].holding = true;
                    removeCallbacks(redraw);
                    postDelayed(redraw, draw_delay);
                    //postInvalidateDelayed(draw_delay);
                }
                break; }// another finger down while holding one down

            case MotionEvent.ACTION_POINTER_UP: { // finger up, still holding one down
                final int btn = touch2blob(event.getX(i), event.getY(i));
                if(btn >= 0) {
                    if (blobs()[btn].tapping)
                        stroke.taps[btn]++;
                    blobs()[btn].holding = false;
                    removeCallbacks(redraw);
                    postDelayed(redraw, draw_delay);
                    //postInvalidateDelayed(draw_delay);
                }
                break; }
            case MotionEvent.ACTION_MOVE: {
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
                                int[] table = (dx == 0
                                        ? (dy == 1 ? stroke.downs : stroke.ups)
                                        : (dx == 1 ? stroke.rights : stroke.lefts));
                                table[bid]++;
                                old.tapping = false; // this is no longer a tap
                                removeCallbacks(redraw);
                                postDelayed(redraw, draw_delay);
                                //postInvalidateDelayed(draw_delay);
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
                    Log.i(TAG, "" + pattern);
                    handlePattern(stroke);
                    flushStroke();
                    removeCallbacks(redraw);
                    post(redraw); // done swiping, update view immediately
                }
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
            final Object o = Ime.layout.get(stroke);
            return o == null ? null : o.toString();
        }
        return null;
    }

    Stroke tempStroke = new Stroke(blobs().length);
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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

            final Object _token = Ime.layout.get(tempStroke);
            String token = _token == null ? null : _token.toString();
            if(any) { // try surrounding swipes
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.downs,    0,-1);
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.ups,  0, 1);
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.rights, -1, 0);
                if(token == null) token = strokeTry(i, stroke, tempStroke, tempStroke.lefts, 1, 0);
            }

            final boolean show_labels_maybe = true;
            final boolean show_labels = Ime.config.showLabelsAlways ? true : show_labels_maybe;
            bs[i].draw(canvas, show_labels
                    ? (token == null ? "" : prettify(token))
                    : "");

            if(false && i == 2 && token == null) { // nevermind that dumb thing
                final Paint red = new Paint();
                red.setStyle(Paint.Style.STROKE);
                red.setStrokeWidth(pixels(3));
                red.setColor(Color.argb(0x10, 0, 0xff, 0xff));
                canvas.drawCircle(bs[i].x(), bs[i].y(), pixels(BS / 4), red);
            }
        }

    }

    // describe a token to the user (lower case letter? special command?).
    // we can make this as pretty as we want.
    private String prettify(final String token) {
        if("repress".equals(token)) return "↺";
        if(":space".equals(token)) return "␣";
        if(":backspace".equals(token)) return "⇐";
        if(":enter".equals(token)) return "⏎";
        if(":dpad_left".equals(token)) return "←";
        if(":dpad_up".equals(token)) return "↑";
        if(":dpad_right".equals(token)) return "→";
        if(":dpad_down".equals(token)) return "↓";
        if(token.startsWith(":")) {
            final String key = token.substring(1);
            return key.length() == 1
                    ? (modShift() ? key.toUpperCase() : key.toLowerCase()) // eg "key S"
                    : key; // eg "key DEL"
        }
        return token; // eg "input å"
    }

}

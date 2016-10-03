package com.adellica.thumbkeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MutableDouble;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by klm on 9/27/16.
 */
public class ThumbkeyboardView extends View {
    private static final String TAG = "TKEY";
    public ThumbkeyboardIME Ime;
    private boolean showHelp = false;
    private final int MAX_DELAY_DOUBLE_COMBO = 60; // ms
    private static final int BLOB_RADIUS = 40; // dpi
    private static final int BLOB_BORDER = 2; // dpi

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

    }

    class Blob {
        // dpi coordinates (negative counts from right/bottom)
        final private double x;
        final private double y;
        final private String name;
        final private int bid;

        Blob(int bid, String name, double x, double y) {
            this.bid = bid;
            this.name = name;
            this.x = x;
            this.y = y;
        }
        public double dist2(double x, double y) {
            return  (this.x() - x) * (this.x() - x) +
                    (this.y() - y) * (this.y() - y);
        }

        // screen coordinates
        public float x() { return x < 0 ? (getWidth()  + pixels((int)x)) : pixels((int)x); }
        public float y() { return y < 0 ? (anchorY() + pixels((int)y)) : pixels((int)y); }

        @Override
        public String toString() { return  "[Blob " + x() + " " + y() + "]";}
        public String name() { return name; }
        public int bid() { return bid; }
    }


    private int _anchorY = -100;
    private int anchorY() {
        if(_anchorY >= 0)
            return _anchorY;
        else
            return getHeight() + _anchorY;
    }

    final int BS = BLOB_RADIUS;
    static final int BB = BLOB_BORDER * 2; // wall margin
    // negative positions means right/bottom-aligned
    Blob [] _blobs = new Blob [] {
            //        ,-- label   ,-- dpi
            new Blob(0, "W",    BS+BB, -5*BS), // left hand
            new Blob(1, "A",    BS+BB, -3*BS),
            new Blob(2, "C",    BS+BB,   -BS),
            new Blob(3, "B",  3*BS+BB, -3*BS),
            new Blob(4, "D",  3*BS+BB,   -BS),
            new Blob(5, "G", -3*BS-BB,   -BS), // right hand
            new Blob(6, "E", -3*BS-BB, -3*BS),
            new Blob(7, "H",   -BS-BB,   -BS),
            new Blob(8, "F",   -BS-BB, -3*BS),
            new Blob(9, "Z",   -BS-BB, -5*BS),
    };
    private Blob [] blobs () { return _blobs; }

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

    static class Press {
        PressType type;
        Blob [] pressing;
        long ms; // milliseconds after previous press
        Press(PressType type, Blob [] btn, long when) { this.type = type; this.pressing = btn.clone(); this.ms = when; }
        public boolean [] toState () {
            boolean [] state = new boolean [10];
            for(int i = 0 ; i < pressing.length ; i++) {
                int bid = pressing[i] == null ? -1 : pressing[i].bid();
                if(bid >= 0) // no need to handle fingers not touching anything
                    state[bid] = true;
            }
            return state;
        }
    }

    static final int MAX_PRESSES = 512;
    Press [] press = new Press [MAX_PRESSES];
    int presses = 0;
    long timeLastPress;

    private void pressStart(long when) {
        pressedFlush(presses);
        processedPresses = presses = 0;
        timeLastPress = when;
    }
    private void pressComplete() {
        for(int i = 0 ; i < buttonStates.length ; i++)
            buttonStates[i] = false;
    }

    int processedPresses = 0;
    private Runnable processInvokeCallback = new Runnable() {
        @Override
        public void run() {
            pressInvoke(presses);
        }
    };
    private void pressedFlush(int presses) {
        Log.d(TAG, "flusing presses " + processedPresses + "~" + presses);
        for (int i = processedPresses + 1; i <= presses ; i++) {
            pressInvoke(i);
        }
    }

    enum PressType {
        DOWN, UP, SWIPE
    }

    private void press(PressType type, Blob [] bleh, long when) {
        if(presses >= MAX_PRESSES) return;
        pressedFlush(presses - 1);

        long elapsed = when - timeLastPress;
        timeLastPress = when;
        press[presses] = new Press(type, bleh, elapsed);
        buttonStates = press[presses].toState();
        presses++;

        removeCallbacks(processInvokeCallback);
        // need >60m guarantees so we don't process simultanious combinations as two strokes
        postDelayed(processInvokeCallback, MAX_DELAY_DOUBLE_COMBO + 10);
        postInvalidate();
    }

    private boolean [] buttonStates = new boolean[] {false,false,false,false,false,   false,false,false,false,false };


    // eg map [false, true, false, false] => ".x.."
    private String state2str(boolean [] state) {
        String s = "";
        for(int i = 0 ; i < state.length ; i++) {
            boolean b = state[i];
            if(i == state.length / 2) s+= " "; // add space in between
            s += b ? "x" : ".";
        }
        return s;
    }



    private String pressPattern(int presses) {
        if(presses == 0) return "";// avoid nullpointer ref on press[0] which may not be initialized. this is getting hacky

        boolean [] state = press[0].toState();
        String pattern = "";

        for ( int i = 1 ; i < presses ; i++ ) {

            boolean squashPress = (press[i - 1].type == press[i].type) && // we can only squash two down or two up events
                    (press[i].ms < MAX_DELAY_DOUBLE_COMBO); // and only if at almost the same time

            if(!squashPress) {
                pattern += state2str(state) + "\n";
            }

            state = press[i].toState();
        }
        // this is safe because squashPress would always be false
        // here, since we're postDelayed >60ms after the event
        // actually happened
        pattern += state2str(state) + "\n";
        return pattern;
    }


    private void pressInvoke(int presses) {
        String pattern = pressPattern(presses);
        handlePattern(pattern);
        Log.d(TAG, "===== these " + presses + " CHORDs make " + ThumboardLayout.parse(pattern));
        Log.d(TAG, pattern);
        processedPresses = presses;
    }

    private void handlePattern(String p) {
        if(p != null) {
            String token = ThumboardLayout.parse(p);
            if(token != null)
                handleToken(token);
        }
    }
    private String cmd(String token) {
        int idx = token.indexOf(' ');

        if(idx >= 0)
            return token.substring(0, idx);
        else
            // no space means we've got an unparamterized command
            return token;
    }
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

        if("shift".equals(cmd))
            modShift(!modShift());
        else if("ctrl".equals(cmd))
            modCtrl(!modCtrl());
        else if("alt".equals(cmd)) {
            modAlt(!modAlt());
        } else if("meta".equals(cmd))
            modMeta(!modMeta());
        else if ("help".equals(cmd)) {
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
        }

        if (!"repeat".equals(cmd)) // avoid infinite recursion
            lastToken = t;
    }

    private void handleInput(String input) {
        if(modShift())
            Ime.getCurrentInputConnection().commitText(input.toUpperCase(), 0);
        else
            Ime.getCurrentInputConnection().commitText(input, 0);
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
        Log.d(TAG, "keystroke " + key + " shift = " + modShift() + " meta: " + meta);
        if (keycode != 0) {
            long now = System.currentTimeMillis();
            Ime.getCurrentInputConnection().sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0, meta));
            Ime.getCurrentInputConnection().sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,   keycode, 0, meta));
        }

        modifiersClear();
    }

    boolean holding = false;
    Blob [] fingerTouches = new Blob [ 4 ]; // who'se got 4 thumbs anyway?

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
        if(d.value > pixels((int)(BLOB_RADIUS * 1.5))) { // min diameter to register a button click. let's be tolerant!
            _anchorY = (int)event.getY(i);
            postInvalidate();
            return true;
        }

        switch(event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: { // primary finger down!
                pressStart(event.getEventTime());
                int btn = touch2blob(event.getX(i), event.getY(i));
                int fid = event.getPointerId(i);

                fingerTouches[fid] = blobs()[btn];
                press(PressType.DOWN, fingerTouches, event.getEventTime());
                break; }

            case MotionEvent.ACTION_POINTER_DOWN: { // another finger down while holding one down
                int btn = touch2blob(event.getX(i), event.getY(i));
                int fid = event.getPointerId(i);
                fingerTouches[fid] = blobs()[btn];
                press(PressType.DOWN, fingerTouches, event.getEventTime());
                break; }

            case MotionEvent.ACTION_POINTER_UP: { // finger up, still holding one down
                int fid = event.getPointerId(i);
                fingerTouches[fid] = null;
                press(PressType.UP, fingerTouches, event.getEventTime());
                break; }
            case MotionEvent.ACTION_MOVE: {
                for( int j = 0 ; j < event.getPointerCount() ; j++) {
                    Blob btn = blobs()[touch2blob(event.getX(j), event.getY(j))];
                    if(btn != fingerTouches[event.getPointerId(j)]) {
                        int fid = event.getPointerId(j);
                        fingerTouches[fid] = btn;
                        press(PressType.SWIPE, fingerTouches, event.getEventTime());
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: { // all fingers up! obs: last finger up ?≠ first finger down
                int fid = event.getPointerId(i);
                fingerTouches[fid] = null;
                press(PressType.UP, fingerTouches, event.getEventTime());
                break; }
            default:
                //Log.d(TAG, "missed event " + event.getActionMasked());
                break;
        }
        return true;
    }

    private int pixels(int dpi) {
        Resources r = getResources();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, r.getDisplayMetrics());
    }

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
        final Paint fill = new Paint();
        fill.setStyle(Paint.Style.FILL);
        boolean any = false;
        for (int i = 0 ; i < bs.length ; i++) { any = any || buttonStates[i]; }

        for (int i = 0 ; i < bs.length ; i++) {
            boolean holding = buttonStates[i];
            if(any)
                if(holding) fill.setColor(Color.argb(0xB0, 0xff, 0xff, 0x00));
                else        fill.setColor(Color.argb(0x40, 0xff, 0xff, 0x00));
            else            fill.setColor(Color.argb(0x40, 0xff, 0xff, 0x00));
            //canvas.drawCircle((float)bs[i].x, (float)bs[i].y, pixels(BLOB_RADIUS), fill);
            final Blob b = bs[i];
            final int S = pixels(BLOB_RADIUS - BLOB_BORDER);
            canvas.drawRect(b.x()-S, b.y()-S, b.x()+S, b.y()+S, fill);
        }

        if(!showHelp) return;

        final TextPaint p = new TextPaint();
        final int w = canvas.getWidth();
        final int h = canvas.getHeight();
        final int y = Math.min(w, h);

        final Paint background = new Paint();
        background.setColor(Color.argb(0xC0, 20, 20, 20));


        StaticLayout textLayout = new StaticLayout(ThumboardLayout.help(), p, canvas.getWidth() / 3, Layout.Alignment.ALIGN_NORMAL, 1.8f, 0.0f, false);

        p.setTypeface(Typeface.MONOSPACE);
        p.setTextSize(y / 36);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(255, 255, 255));
        canvas.save();
        canvas.translate(450, 0);
        canvas.drawRect(0, 0, textLayout.getWidth() + 100, textLayout.getHeight() + 100, background);
        canvas.translate(50, 20);
        textLayout.draw(canvas);
        canvas.restore();


    }

}

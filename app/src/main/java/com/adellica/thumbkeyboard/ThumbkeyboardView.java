package com.adellica.thumbkeyboard;

import android.content.Context;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by klm on 9/27/16.
 */
public class ThumbkeyboardView extends View {
    private static final String TAG = "TKEY";
    public ThumbkeyboardIME Ime;
    private boolean showHelp = false;

    public ThumbkeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void reset() {

    }

    static class Blob {
        public double x;
        public double y;

        Blob(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public double dist2(double x, double y) {
            return  (this.x - x) * (this.x - x) +
                    (this.y - y) * (this.y - y);
        }

        @Override
        public String toString() { return  "[Blob " + x + " " + y + "]";}
    }

    // we keep the points constant and rebuild the blobs because
    // getWidth and getHeight might change.
    // there is probably a lot better way of doing this...
    private Blob [] blobs () {
        Blob [] blobs = new Blob [blobPoints.length];
        for(int i = 0 ; i < blobPoints.length ; i++) {
            Blob b = blobs[i] = new Blob(blobPoints[i].x, blobPoints[i].y);
            if(b.x < 0) b.x = getWidth()  + b.x;
            if(b.y < 0) b.y = getHeight() + b.y;
        }
        return blobs;
    }

    // negative positions means right/bottom-aligned
    Point [] blobPoints = new Point[] {
            new Point( 110, -500), // 0
            new Point( 160, -300), // 1
            new Point( 260, -100), // 2
            new Point(-260, -100), // 3
            new Point(-160, -300), // 4
            new Point(-110, -500), // 5
    };

    private int touch2blob(double x, double y) {
        int nearest = 0;
        double dist2 = blobs()[0].dist2(x, y);
        for(int bid = 1 ; bid < blobPoints.length ; bid++) {
            double dist = blobs()[bid].dist2(x, y);
            if(dist < dist2) {
                dist2 = dist;
                nearest = bid;
            }
        }
        return nearest;
    }

    static class Press {
        boolean down_p; // true for down events, false otherwise
        int btn; // 0-3 where 0 is left-most button or whatever
        long ms; // milliseconds after previous press
        Press(boolean down_p, int btn, long when) { this.down_p = down_p; this.btn = btn; this.ms = when; }
    }

    static final int MAX_PRESSES = 512;
    Press [] press = new Press [MAX_PRESSES];
    int presses = 0;
    long timeLastPress;

    private void pressStart(long when) {
        presses = 0;
        timeLastPress = when;
    }
    private void press(boolean down_p, int bid, long when) {
        if(presses >= MAX_PRESSES) return;

        long elapsed = when - timeLastPress;
        timeLastPress = when;
        press[presses] = new Press(down_p, bid, elapsed);
        presses++;
    }
    private void pressDown(int bid, long when) {
        press(true, bid, when);
    }
    private void pressUp(int bid, long when) {
        press(false, bid, when);
    }

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

    private void pressComplete() {
        boolean [] state = { false, false, false, false , false, false};
        if(state.length != blobPoints.length) throw new RuntimeException("button state.length is wrong ");

        List<String> pattern = new ArrayList<String>();

        state[press[0].btn] = press[0].down_p;
        for ( int i = 1 ; i < presses ; i++ ) {

            boolean squashPress = (press[i - 1].down_p == press[i].down_p) && // we can only squash two down or two up events
                    (press[i].ms < 60); // and only if at almost the same time

            if(!squashPress) {
                pattern.add(state2str(state));
                Log.d(TAG, state2str(state));
            }

            state[press[i].btn] = press[i].down_p;
            //Log.d(TAG, "squash: " + squashPress + "  " + (press[i].down_p?"down ":"up   ") + press[i].btn + " " + press[i].ms);
        }
        Log.d(TAG, "===== CHORD pattern is " + ThumboardLayout.parse(pattern.toArray(new String[]{})));
        handlePattern(pattern.toArray(new String[]{}));
    }

    private String lastInput;
    private void sendInput(String s) {
        if("REPEAT".equals(s))
            if(!"REPEAT".equals(lastInput))
                sendInput(lastInput);
            else
                Log.e(TAG, "error! trying to repeat the repeat command!");
        else
            Ime.sendDownUpKeyEvents(ThumboardKeycodes.string2keycode(s));

        if(!"REPEAT".equals(s))
            lastInput = s;
    }

    private void handlePattern(String [] p) {
        String result = ThumboardLayout.parse(p);

        if("TOGGLE HELP".equals(result)) {
            showHelp = !showHelp;
            postInvalidate();
        }
        else if(result != null) {
            sendInput(result);
        }
        else {
            String pat = ""; for(String j : p) pat+=j + "\n"; // how can I else map across p like this?
            Toast.makeText(getContext(), "unknown chords: \n" + pat, Toast.LENGTH_LONG).show();
            Log.d(TAG, "unknown chord: " + Arrays.asList(p));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int i = event.getActionIndex(); // index of finger that caused the down event

        switch(event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: { // primary finger down!
                pressStart(event.getEventTime());
                double  x0 = event.getX(i), y0 = event.getY(i);
                pressDown(touch2blob(x0, y0), event.getEventTime());
                break; }

            case MotionEvent.ACTION_POINTER_DOWN: { // another finger down while holding one down
                double x = event.getX(i), y = event.getY(i);
                pressDown(touch2blob(x, y), event.getEventTime());
                break; }

            case MotionEvent.ACTION_POINTER_UP: { // finger up, still holding one down
                double x = event.getX(i), y = event.getY(i);
                pressUp(touch2blob(x, y), event.getEventTime());
                break; }

            case MotionEvent.ACTION_UP: { // all fingers up! obs: last finger up ?≠ first finger down
                double  x0 = event.getX(i), y0 = event.getY(i);
                pressUp(touch2blob(x0, y0), event.getEventTime());
                pressComplete();
                break; }
            default:
                //Log.d(TAG, "missed event " + event.getActionMasked());
                break;
        }
        return true;
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
        for (int i = 0 ; i < bs.length ; i++) {
            canvas.drawCircle((float)bs[i].x, (float)bs[i].y, 100, cp1);
            canvas.drawCircle((float)bs[i].x, (float)bs[i].y, 100, cp2);
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

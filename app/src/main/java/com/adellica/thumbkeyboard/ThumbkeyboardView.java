package com.adellica.thumbkeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.util.Arrays;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by klm on 9/27/16.
 */
public class ThumbkeyboardView extends View {
    public static final int LENGTH = 12;
    private static final String TAG = "TKEY";
    public ThumbkeyboardIME Ime;
    private static final int BLOB_RADIUS = 38; // dpi
    private static final int BLOB_BORDER = 4; // dpi

    public boolean overlay = false;

    public static String configDir() {
        return android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + "thumb-keyboard"
                + File.separator;
    }

    // negative positions means right/bottom-aligned
    final KeyboardState keyboardState = new KeyboardState(new boolean[LENGTH],
            new Blob[]{
                    //       idx col row
                    new Blob(0, 0, 0),
                    new Blob(1, 1, 0),
                    new Blob(2, -2, 0),
                    new Blob(3, -1, 0),
                    new Blob(4, 0, 1),
                    new Blob(5, 1, 1),
                    new Blob(6, -2, 1),
                    new Blob(7, -1, 1),
                    new Blob(8, 0, 2),
                    new Blob(9, 1, 2),
                    new Blob(10, -2, 2),
                    new Blob(11, -1, 2),
            }, new Stroke(LENGTH), new int[]{-1, -1, -1, -1});

    /**
     * Tricky business! Going from Blob index to the Blob index
     * of a nextdoor Blob (from delta).
     *
     * @param bid index of button
     * @param dx  delta to move in x direction (should be 0 if dy ≠ 0)
     * @param dy  delta to move in y direction (should be 0 if dx ≠ 0)
     * @return -1 if outside 4x3 Blob grid
     */
    public static int Blobdelta(int bid, int dx, int dy) {
        int x = (bid % 4) + dx;
        int y = (bid / 4) + dy;
        //Log.i(TAG, "bid " + bid + " + " + dx + "," + dy + "  = " + x + "," + y + "  ");
        if (y >= 0 && y <= 2)
            if (x >= 0 && x <= 3)
                return (y * 4) + x;
        return -1;
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

    private boolean modShift() {
        return (ThumbkeyboardIME.m.dict.get("shift*") == Boolean.TRUE);
    }

    private boolean modCtrl() {
        return (ThumbkeyboardIME.m.dict.get("ctrl*") == Boolean.TRUE);
    }

    private boolean modAlt() {
        return (ThumbkeyboardIME.m.dict.get("alt*") == Boolean.TRUE);
    }

    private boolean modMeta() {
        return (ThumbkeyboardIME.m.dict.get("meta*") == Boolean.TRUE);
    }

    public ThumbkeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "no config here");
    }

    final String[] tokens = new String[LENGTH];


    private int __anchorY = -1;

    // input: y screen coordinate of top of keyboard
    private void anchorY(int newValue) {
        __anchorY = newValue;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!overlay) {
            // having a height while being overlay causes it to raise other content.
            // when we don't have a height we can be overlayd properly because our height is the full screen
            // or something like that. I don't really get this stuff but I don't think you're supposed to.
            if (getLayoutParams() != null)
                getLayoutParams().height = pixels(BLOB_RADIUS * 2/*diameter*/ * 3/*rows*/);
        }
    }

    final int BS = BLOB_RADIUS;

    // screen coordinates of top of top-most button
    private int anchorY() {
        if (__anchorY < 0)
            return getHeight() - pixels(BS * 2 * 3);
        else
            return __anchorY;
    }

    private int touch2blob(double x, double y) {
        return touch2blob(x, y, null);
    }

    private void handlePattern(final Stroke stroke) {
        final Stroke t = new Stroke(stroke.lefts.length);
        t.copyFrom(stroke);
        Ime.handleStroke(t);
    }

    final String[][] subTokens = new String[LENGTH][LENGTH];

    private void hide() {
        Ime.requestHideSelf(0); // <-- does a silly animation
        Ime.setCandidatesViewShown(false);
    }

    private void vibrate(int milliseconds) {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) this.getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) this.getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(milliseconds);
        }
    }

    private int touch2blob(double x, double y, MutableDouble closestDist) {
        int nearest = 0; // index
        double dist2 = keyboardState.blobs[nearest].dist2(x, y);
        for (int bid = 1; bid < LENGTH; bid++) {
            double dist = keyboardState.blobs[bid].dist2(x, y);
            if (dist < dist2) {
                dist2 = dist;
                nearest = bid;
            }
        }
        if (closestDist != null)
            closestDist.value = Math.sqrt(dist2);
        return nearest;
    }

    private int pixels(int dpi) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, r.getDisplayMetrics());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int i = event.getActionIndex(); // index of finger that caused the down event

        if (event.getPointerId(i) >= keyboardState.fingerTouches.length) {
            Log.e(TAG, "only up to " + keyboardState.fingerTouches.length + " simultanious fingers supported ...");
            return false;
        }

        MutableDouble d = new MutableDouble(-1);
        touch2blob(event.getX(i), event.getY(i), d);
        // we're pressing "outside" of keyboard, hide it
        if (d.value > pixels((int) (BLOB_RADIUS * 1.5))) {
            keyboardState.flushStroke();
            hide();
        }
        final int eventType = event.getActionMasked();
        if (eventType == MotionEvent.ACTION_MOVE) {
            for (int j = 0; j < event.getPointerCount(); j++) {
                final int btn = touch2blob(event.getX(j), event.getY(j));
                if (keyboardState.update(eventType, btn, event.getPointerId(j))) {
                    vibrate(10);
                }
            }
        } else {
            final int btn = touch2blob(event.getX(i), event.getY(i));
            if (keyboardState.update(eventType, btn, -1)) {
                vibrate(10);
            }
        }
        if (eventType == MotionEvent.ACTION_UP) {
            final String pattern = keyboardState.stroke.toString();
            Log.i(TAG, "" + pattern);
            handlePattern(keyboardState.stroke);
            keyboardState.flushStroke();
        }
        postInvalidate();
        return true;
    }


    private String prettify(final String label) {
        if (label == null) {
            return "";
        }
        String newLabel;
        if (label.startsWith(":") | label.startsWith("!")) {
            newLabel = label.substring(1);
        } else if (label.startsWith("\"")) {
            newLabel = label.substring(1, label.length() - 1);
        } else {
            newLabel = label;
        }
        if ("overlay".equals(newLabel)) return "";
        if ("space".equals(newLabel)) return "␣";
        if (" ".equals(newLabel)) return "⍽";
        if ("repeat".equals(newLabel)) return "↺";
        if ("delete".equals(newLabel)) return "Del";
        if ("C-delete".equals(newLabel)) return "DW";
        if ("backspace".equals(newLabel)) return "⌫";
        if ("C-backspace".equals(newLabel)) return "⌫⌫";
        if ("tab".equals(newLabel)) return "↹";
        if ("shift".equals(newLabel)) return "⇧";
        if ("enter".equals(newLabel)) return "↵";
        if ("dpad_left".equals(newLabel)) return "←";
        if ("dpad_right".equals(newLabel)) return "→";
        if ("dpad_up".equals(newLabel)) return "↑";
        if ("dpad_down".equals(newLabel)) return "↓";
        if ("move_end".equals(newLabel)) return "⇲";
        if ("move_home".equals(newLabel)) return "⇱";
        if ("ctrl".equals(newLabel)) return "Ctrl";

        if (newLabel.length() == 1) {
            return modShift() ? newLabel.toUpperCase() : newLabel.toLowerCase();
        }
        return newLabel; // For all other keys
    }

    private void getChildren(KeyboardState keyboardState, String[] tokens, String[][] subs) {
        for (int i = 0; i < LENGTH; i++) {
            final boolean alreadyPressed = keyboardState.blobs[i].tapping || keyboardState.blobs[i].holding;
            if (alreadyPressed && subs == null) {
                tokens[i] = null;
                continue;
            }
            boolean swipable = false;
            final KeyboardState fakeState = keyboardState.clone();
            for (int direction = 0; direction < 4; direction++) {
                int from = -1;
                switch (direction) {
                    case 0: {
                        from = Blobdelta(i, -1, 0);
                        break;
                    }
                    case 1: {
                        from = Blobdelta(i, 1, 0);
                        break;
                    }
                    case 2: {
                        from = Blobdelta(i, 0, -1);
                        break;
                    }
                    case 3: {
                        from = Blobdelta(i, 0, 1);
                        break;
                    }
                }
                if (from == -1 || (from % 4) / 2 != (i % 4) / 2) {
                    continue;
                }
                for (int pointer = 0; pointer < 4; pointer++) {
                    if (keyboardState.fingerTouches[pointer] == from) {
                        fakeState.update(MotionEvent.ACTION_MOVE, i, pointer);
                        swipable = true;
                        break;
                    }
                }
            }
            if (!swipable && !alreadyPressed) {
                fakeState.update(MotionEvent.ACTION_POINTER_DOWN, i, -1);
                for (int p = 0; p < 4; p++) {
                    if (fakeState.fingerTouches[p] == -1) {
                        fakeState.fingerTouches[p] = i;
                        break;
                    }
                }
            }
            final KeyboardState childFakeState = fakeState.clone();
            int down = 0;
            for (int blob = 0; blob < LENGTH; blob++) {
                if (fakeState.blobs[blob].tapping || fakeState.blobs[blob].holding) {
                    down++;
                }
            }
            System.out.println(down);
            for (int blob = 0; blob < LENGTH; blob++) {
                if (fakeState.blobs[blob].tapping || fakeState.blobs[blob].holding) {
                    down--;
                    if (down == 0) {
                        fakeState.update(MotionEvent.ACTION_UP, blob, -1);
                    } else {
                        fakeState.update(MotionEvent.ACTION_POINTER_UP, blob, -1);
                    }
                }
            }
            final Object result = Ime.layout.get(fakeState.stroke);
            if (result == null) {
                tokens[i] = null;
            } else {
                tokens[i] = result.toString();
            }
            if (subs != null) {
                if (alreadyPressed) {
                    Arrays.fill(subs[i], null);
                } else {
                    getChildren(childFakeState, subs[i], null);
                }
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Blob[] bs = keyboardState.blobs;

        boolean any = false; // <-- anybody being pressed?
        for (int i = 0; i < bs.length; i++) {
            any |= bs[i].holding;
            keyboardState.blobTaps[i] = bs[i].tapping;
        }

        getChildren(keyboardState, tokens, subTokens);
        for (int i = 0; i < bs.length; i++) {
            //final boolean show_labels_maybe = true;
            //final boolean show_labels = Ime.config.showLabelsAlways || show_labels_maybe;
            bs[i].draw(canvas, any, tokens[i] == null ? "" : tokens[i], subTokens[i]);
        }
    }

    class KeyboardState {
        public final Blob[] blobs;
        public final Stroke stroke;
        public final boolean[] blobTaps;
        public final int[] fingerTouches;

        public KeyboardState(boolean[] blobTaps, Blob[] blobs, Stroke stroke, int[] fingerTouches) {
            this.blobTaps = blobTaps;
            this.blobs = blobs;
            this.stroke = stroke;
            this.fingerTouches = fingerTouches;
        }

        public KeyboardState clone() {
            Blob[] newBlobs = blobs.clone();
            for (int i = 0; i < newBlobs.length; i++) {
                newBlobs[i] = newBlobs[i].clone();
            }
            return new KeyboardState(blobTaps.clone(), newBlobs, stroke.clone(), fingerTouches.clone());
        }

        public void flushStroke() {
            stroke.clear();
            Arrays.fill(fingerTouches, -1);
            for (int j = 0; j < LENGTH; j++) {
                blobs[j].tapping = false;
                blobs[j].holding = false;
            }
        }

        public boolean update(int eventType, int btn, int pointer) {
            boolean wouldVibrate = false;
            switch (eventType) {
                case MotionEvent.ACTION_DOWN:  // primary finger down!
                case MotionEvent.ACTION_POINTER_DOWN: { // another finger down while holding one down
                    if (btn >= 0) {
                        blobs[btn].tapping = true;
                        blobs[btn].holding = true;
                    }
                    wouldVibrate = true;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: { // finger up, still holding one down
                    if (btn >= 0) {
                        if (blobs[btn].tapping)
                            stroke.taps[btn]++;
                        blobs[btn].holding = false;
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    final Blob button = blobs[btn]; // <-- going to
                    final int blobId = fingerTouches[pointer];
                    if (btn != blobId && blobId != -1) {
                        wouldVibrate = true;
                        final Blob old = blobs[blobId]; // <-- coming from
                        old.holding = false;
                        button.holding = true;

                        final int bid = old.bid();
                        final int ox = old.bid() % 4, oy = old.bid() / 4;
                        final int nx = btn % 4, ny = btn / 4;
                        final int dx = nx - ox, dy = ny - oy;
                        Log.i(TAG, "swipe on " + bid + ": " + dx + "," + dy);
                        int[] table = (dx == 0
                                ? (dy == 1 ? stroke.downs : stroke.ups)
                                : (dx == 1 ? stroke.rights : stroke.lefts));
                        table[bid]++;
                        old.tapping = false; // this is no longer a tap
                    }
                    fingerTouches[pointer] = btn;
                    break;
                }
                case MotionEvent.ACTION_UP: { // all fingers up! obs: last finger up ?≠ first finger down
                    if (btn >= 0) {
                        if (blobs[btn].tapping)
                            stroke.taps[btn]++;
                    }
                    break;
                }
                default:
                    //Log.d(TAG, "missed event " + event.getActionMasked());
                    break;
            }
            return wouldVibrate;
        }
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

        public Blob clone() {
            final Blob newBlob = new Blob(bid, col, row);
            newBlob.tapping = tapping;
            newBlob.holding = holding;
            return newBlob;
        }

        public double dist2(double x, double y) {
            return (this.x() - x) * (this.x() - x) +
                    (this.y() - y) * (this.y() - y);
        }

        // screen coordinates
        public float x() {
            return (col < 0 ? getWidth() : 0) + pixels(col * (BS * 2) + BS);
        }

        public float y() {
            return anchorY() + (pixels(row * (BS * 2) + BS));
        }

        public int bid() {
            return bid;
        }

        private float[] getOffset(int motherIndex, int daughterIndex) {
            float[] offsets = new float[2];
            int motherSide = motherIndex % 4 < 2 ? -1 : 1;
            int daughterSide = daughterIndex % 4 < 2 ? -1 : 1;

            if (motherSide != daughterSide) {
                offsets[0] = 35f * (daughterIndex % 2 - 0.5f) + 65f * daughterSide;
                offsets[1] = 65f * (daughterIndex / 4 - 1);
            } else {
                int dx = daughterIndex % 4 - motherIndex % 4;
                int dy = daughterIndex / 4 - motherIndex / 4;
                offsets[0] = dx * 15f + daughterSide * 60f;
                offsets[1] = dy * 40f;
            }
            return offsets;
        }

        private void renderText(Canvas canvas, String token, TextPaint p, float size, float space, float xOffset, float yOffset) {
            int y = (int) (Math.min(canvas.getWidth(), canvas.getHeight()) * size);
            float textSize = y * 0.14f;
            int PBS = pixels((int) (BS * space));
            p.setTextSize(textSize);

            String newLabel = prettify(token);

            float txtWidth = p.measureText(newLabel);

            if (txtWidth > (PBS * 2) * 0.9) { // text is too big for button
                textSize = textSize * 0.8f / (txtWidth / (PBS * 2));
                p.setTextSize(textSize); // fit width
                txtWidth = p.measureText(newLabel);
            }
            float yPos = textSize / 3f;
            canvas.drawText(newLabel, xOffset - txtWidth / 2, yOffset + yPos, p);
        }

        public void draw(Canvas canvas, boolean idle, final String label, String[] subs) {
            float[] offsets;
            if (idle)
                if (holding) fill.setColor(Ime.config.colorBackgroundHolding);
                else fill.setColor(Ime.config.colorBackgroundNonIdle);
            else fill.setColor(Ime.config.colorBackgroundIdle);

            final int S = pixels(BLOB_RADIUS - BLOB_BORDER);
            if (bid() == -99)
                canvas.drawCircle(x(), y(), pixels(BS), fill);
            else
                canvas.drawRoundRect(new RectF(x() - S, y() - S, x() + S, y() + S), 6, 6, fill);

            final TextPaint p = new TextPaint();
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL);
            p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            canvas.save();
            canvas.translate(x(), y()); // anchor to center of rectangle

            // Draw big label
            p.setColor(Ime.config.colorLabel);
            renderText(canvas, label, p, 1, 1, 0, 0);

            // Draw sub-labels
            p.setColor(Ime.config.colorSub);
            for (int i = 0; i < LENGTH; i++) {
                p.setColor(Ime.config.colorSub);
                offsets = getOffset(bid, i);
                renderText(canvas, subs[i], p, 0.45f, 0.2f, offsets[0], offsets[1]);
            }
            canvas.restore();
        }
    }
}
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
import android.view.inputmethod.InputConnection;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by klm on 9/27/16.
 */
public class ThumbkeyboardView extends View {
    public static final int LENGTH = 12;
    private static final String TAG = "TKEY";
    public ThumbkeyboardIME Ime;
    private final boolean showHelp = false;
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

    // negative positions means right/bottom-aligned
    KeyboardState keyboardState = new KeyboardState(new boolean[LENGTH],
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
            }, new Stroke(LENGTH));
    final Blob[] fingerTouches = new Blob[4]; // who'se got 4 thumbs anyway?

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

    String[] tokens = new String[LENGTH];


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
    static final int BB = BLOB_BORDER * 2; // wall margin

    // utils
    String readBackwardsUntil(String p, boolean eof) {
        final InputConnection ic = Ime.getCurrentInputConnection();
        if (ic == null) return null;
        int size = 32;
        String c = null;
        while (size < 4096) {
            c = ic.getTextBeforeCursor(size, 0).toString();
            int idx = c.lastIndexOf(p);
            if (idx >= 0) {
                return c.substring(idx + 1);
            }
            size *= 2;
        }
        return eof ? c : null;
    }

    String readForwardsUntil(String p, boolean eof) {
        final InputConnection ic = Ime.getCurrentInputConnection();
        if (ic == null) return null;
        int size = 32;
        String c = null;
        while (size < 4096) {
            c = ic.getTextAfterCursor(size, 0).toString();
            int idx = c.indexOf(p);
            if (idx >= 0) {
                return c.substring(0, idx);
            }
            size *= 2;
        }
        return eof ? c : null;
    }

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

    Map<String, Layout> layouts = new HashMap<String, Layout>();

    boolean holding = false;

    private boolean deleteSurroundingText(int before, int after) {
        final InputConnection ic = Ime.getCurrentInputConnection();
        if (ic != null) {
            ic.deleteSurroundingText(before, after);
            return true;
        }
        return false;
    }

    String[][] subTokens = new String[LENGTH][LENGTH];

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

    /**
     * This guy isn't great. You can't specify SPACE|ENTER. So, for example,
     * a word boundary can only be " " and not " |\n|\t" which is very limiting.
     *
     * @return The string that was deleted
     */
    private String deleteSurroundingUntil(final String pre, boolean bof, final String post, boolean eof, boolean trimLeft, boolean trimRight) {
        final String preline = readBackwardsUntil(pre, bof);
        final String postline = readForwardsUntil(post, eof);
        if (deleteSurroundingText(
                preline == null ? 0 : preline.length() + (trimLeft ? 1 : 0),
                postline == null ? 0 : postline.length() + (trimRight ? 1 : 0)))
            return preline + postline;
        return "";
    }

    private boolean hidden() {
        return hidden;
    }

    private static boolean isTapped(Stroke stroke, boolean[] blobTaps, int j) {
        int outs = stroke.ups[j] + stroke.downs[j] + stroke.lefts[j] + stroke.rights[j];
        System.out.println(outs);
        if ((j - 4) >= 0 && stroke.downs[j - 4] > outs) {
            return true;
        }
        if ((j + 4) < blobTaps.length && stroke.ups[j + 4] > outs) {
            return true;
        }
        if ((j - 1) >= 0 && stroke.rights[j - 1] > outs) {
            return true;
        }
        if ((j + 1) < blobTaps.length && stroke.lefts[j + 1] > outs) {
            return true;
        }
        if (outs == 0) {
            return blobTaps[j];
        }
        return false;
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

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                holding = true;
                break;
            case MotionEvent.ACTION_UP:
                holding = false;
                break;
        }

        if (event.getPointerId(i) >= fingerTouches.length) {
            Log.e(TAG, "only up to " + fingerTouches.length + " simultanious fingers supported ...");
            return false;
        }

        MutableDouble d = new MutableDouble(-1);
        touch2blob(event.getX(i), event.getY(i), d);
        // we're pressing "outside" of keyboard, hide it
        if (d.value > pixels((int) (BLOB_RADIUS * 1.5))) {
            flushStroke();
            postInvalidate();
            hide();
        }
        final int btn = touch2blob(event.getX(i), event.getY(i));
        keyboardState.update(event, btn);
        postInvalidate();
        return true;
    }

    private void flushStroke() {
        keyboardState.stroke.clear();
        Arrays.fill(fingerTouches, null);
        for (int j = 0; j < LENGTH; j++) {
            keyboardState.blobs[j].tapping = false;
            keyboardState.blobs[j].holding = false;
        }
    }

    private String prettify2(final String label) {
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

        if (newLabel.length() == 1) {
            return modShift() ? newLabel.toUpperCase() : newLabel.toLowerCase();
        }
        return newLabel; // For all other keys
    }

    /**
     * This is supertricky and a complete mess.
     *
     * @param bid      the index of the Blob that you're looking at (the one we're trying to label)
     * @param original the unmodified stroke
     * @param stroke   our temp stroke
     * @param i        array of inverse direction of dx,dy
     * @param dx       on Blob grid, +1 is right, -1 is left
     * @param dy       on Blob grid, +1 is down, -1 is up
     * @param sbs      The subToken array to calculate
     * @return the symbol on that square
     */
    public String strokeTry(int bid, Stroke original, Stroke stroke, int[] i, int dx, int dy, String[][] sbs, boolean[] blobTaps) {
        boolean realTap = false;
        stroke.copyFrom(original);
        int j = Blobdelta(bid, dx, dy);
        if (j >= 0 && isTapped(stroke, blobTaps, j)) {
            i[j]++;
            if (stroke.taps[j] > 0) {
                stroke.taps[j]--;
                realTap = true;
            }
            if (sbs != null) {
                if (realTap) {
                    blobTaps[j] = false;
                }
                getChildren(stroke, blobTaps, sbs[bid], null);
                if (realTap) {
                    blobTaps[j] = true;
                }
            }
            final Object o = Ime.layout.get(stroke);
            return o == null ? null : o.toString();
        } else if (sbs != null) {
            Arrays.fill(sbs[bid], null);
        }
        return null;
    }

    private void getChildren(Stroke stroke, boolean[] blobTaps, String[] tokens, String[][] subs) {
        // fakeStroke is the stroke if the current held buttons were released
        Stroke fakeStroke = new Stroke(blobTaps.length);
        // tempStroke is the stroke if a new button was tapped as well
        Stroke tempStroke = new Stroke(blobTaps.length);
        String token;
        fakeStroke.copyFrom(stroke);
        for (int j = 0; j < blobTaps.length; j++) {
            if (blobTaps[j]) {
                fakeStroke.taps[j]++; // <-- pretend we tapped held buttons
            }
        }
        for (int i = 0; i < blobTaps.length; i++) {
            tempStroke.copyFrom(fakeStroke);
            tempStroke.taps[i]++; // <-- pretend we tapped current
            final Object _token = Ime.layout.get(tempStroke);
            token = null;
            if (_token != null) {
                tokens[i] = _token.toString();
            } else {
                if (token == null)
                    token = strokeTry(i, fakeStroke, tempStroke, tempStroke.downs, 0, -1, subs, blobTaps);
                if (token == null)
                    token = strokeTry(i, fakeStroke, tempStroke, tempStroke.ups, 0, 1, subs, blobTaps);
                if (token == null)
                    token = strokeTry(i, fakeStroke, tempStroke, tempStroke.rights, -1, 0, subs, blobTaps);
                if (token == null)
                    token = strokeTry(i, fakeStroke, tempStroke, tempStroke.lefts, 1, 0, subs, blobTaps);
                tokens[i] = token;
            }
            if (subs != null) {
                boolean btSave = blobTaps[i];
                blobTaps[i] = true;
                getChildren(fakeStroke, blobTaps, subs[i], null);
                blobTaps[i] = btSave;
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

        getChildren(keyboardState.stroke, keyboardState.blobTaps, tokens, subTokens);
        for (int i = 0; i < bs.length; i++) {
            //final boolean show_labels_maybe = true;
            //final boolean show_labels = Ime.config.showLabelsAlways || show_labels_maybe;
            bs[i].draw(canvas, any, tokens[i] == null ? "" : tokens[i], subTokens[i]);
        }
    }

    // describe a token to the user (lower case letter? special command?).
    // we can make this as pretty as we want.
    private String prettify(final String token) {
        if ("key SPACE".equals(token)) return "␣";
        if ("repeat".equals(token)) return "↺";
        if ("key DEL".equals(token)) return "⇐";
        if ("key ENTER".equals(token)) return "⏎";
        if ("key DPAD_LEFT".equals(token)) return "←";
        if ("key DPAD_UP".equals(token)) return "↑";
        if ("key DPAD_RIGHT".equals(token)) return "→";
        if ("key DPAD_DOWN".equals(token)) return "↓";
        if (token.startsWith("key ")) {
            final String key = token.substring(4);
            return key.length() == 1
                    ? (modShift() ? key.toUpperCase() : key.toLowerCase()) // eg "key S"
                    : key; // eg "key DEL"
        } else if (token.startsWith("input ")) {
            return token.substring(6);
        }
        return token; // eg "input å"
    }

    class KeyboardState {
        public Blob[] blobs;
        public Stroke stroke;
        public boolean[] blobTaps;

        public KeyboardState(boolean[] blobTaps, Blob[] blobs, Stroke stroke) {
            this.blobTaps = blobTaps;
            this.blobs = blobs;
            this.stroke = stroke;
        }

        public void update(MotionEvent event, int btn) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: { // primary finger down!
                    if (btn >= 0) {
                        keyboardState.blobs[btn].tapping = true;
                        keyboardState.blobs[btn].holding = true;
                    }
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN: { // another finger down while holding one down
                    if (btn >= 0) {
                        keyboardState.blobs[btn].tapping = true;
                        keyboardState.blobs[btn].holding = true;
                    }
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: { // finger up, still holding one down
                    if (btn >= 0) {
                        if (keyboardState.blobs[btn].tapping)
                            keyboardState.stroke.taps[btn]++;
                        keyboardState.blobs[btn].holding = false;
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    for (int j = 0; j < event.getPointerCount(); j++) {
                        final Blob button = keyboardState.blobs[btn]; // <-- going to
                        if (button.bid() >= 0) {
                            if (button != fingerTouches[event.getPointerId(j)]) {
                                final int fid = event.getPointerId(j);
                                final Blob old = fingerTouches[fid]; // <-- coming from
                                if (old != null) {
                                    old.holding = false;
                                    button.holding = true;

                                    final int bid = old.bid();
                                    int ox = old.bid() % 4, oy = old.bid() / 4;
                                    int nx = button.bid() % 4, ny = button.bid() / 4;
                                    int dx = nx - ox, dy = ny - oy;
                                    Log.i(TAG, "swipe on " + bid + ": " + dx + "," + dy);
                                    int[] table = (dx == 0
                                            ? (dy == 1 ? keyboardState.stroke.downs : keyboardState.stroke.ups)
                                            : (dx == 1 ? keyboardState.stroke.rights : keyboardState.stroke.lefts));
                                    table[bid]++;
                                    old.tapping = false; // this is no longer a tap
                                }
                                fingerTouches[fid] = button;
                            }
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: { // all fingers up! obs: last finger up ?≠ first finger down
                    if (btn >= 0) {
                        if (keyboardState.blobs[btn].tapping)
                            keyboardState.stroke.taps[btn]++;
                        String pattern = keyboardState.stroke.toString();
                        Log.i(TAG, "" + pattern);
                        handlePattern(keyboardState.stroke);
                        flushStroke();
                    }
                    show();
                    break;
                }
                default:
                    //Log.d(TAG, "missed event " + event.getActionMasked());
                    break;
            }
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

            String newLabel = prettify2(token);

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
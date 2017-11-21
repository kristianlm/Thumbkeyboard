// 16may16abu
// (c) Software Lab. Alexander Burger

package com.adellica.thumbkeyboard;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.adellica.thumbkeyboard.tsm.Keypress;
import com.adellica.thumbkeyboard.tsm.Library.NamedApplicable;
import com.adellica.thumbkeyboard.tsm.Machine;
import com.adellica.thumbkeyboard.tsm.Machine.Str;
import com.adellica.thumbkeyboard.tsm.Reader;
import com.adellica.thumbkeyboard.tsm.stack.IPair;

import java.util.HashMap;
import java.util.Map;

import static android.view.KeyEvent.META_ALT_LEFT_ON;
import static android.view.KeyEvent.META_ALT_ON;
import static android.view.KeyEvent.META_CTRL_LEFT_ON;
import static android.view.KeyEvent.META_CTRL_ON;
import static android.view.KeyEvent.META_META_LEFT_ON;
import static android.view.KeyEvent.META_META_ON;
import static android.view.KeyEvent.META_SHIFT_LEFT_ON;
import static android.view.KeyEvent.META_SHIFT_ON;


public class ThumbkeyboardIME extends InputMethodService {
    private static final String TAG = "TSM";

    private final Handler mHandler = new Handler();

    public final Map<Stroke, Object> layout = new HashMap<Stroke, Object>();
    // only one machine and one server per app instance
    public static Machine m = new Machine();
    public static Thread server = null;

    /**
     * toggle using overlay(boolean) method.
     */
    private boolean _overlaymode = false;

    private ThumbkeyboardView viewInput; // used for non-overlay
    private ThumbkeyboardView viewCandidates; // used for fullscreen overlay

    public boolean overlay() {
        return _overlaymode;
    }

    /**
     * fullblown-fullscreen overlay or standard opaque keyboard view with a height?
     * @param value
     */
    public void overlay(boolean value) {
        if(value == _overlaymode) return;
        _overlaymode = value;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setCandidatesViewShown(_overlaymode);
                if(_overlaymode) {
                    viewInput.setVisibility(View.GONE);
                    viewCandidates.setVisibility(View.VISIBLE);
                } else {
                    viewInput.setVisibility(View.VISIBLE);
                    viewCandidates.setVisibility(View.GONE);
                }
            }
        });
    }

    private ThumbkeyboardView createThumbkeyboardView(boolean overlayed) {
        final ThumbkeyboardView tv = new ThumbkeyboardView(getApplicationContext(), null);
        tv.Ime = this;
        tv.overlay = overlayed;
        return tv;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // just cause I don't want null-pointers around
        viewInput = createThumbkeyboardView(false);
        viewCandidates = createThumbkeyboardView(true);

        // it's important this gets run on every new instance of ThumbkeyboardIME because
        // we need to update the press procedure so that it references the "latest and greatest"
        // getCurrentInputConnection.
        m.dict.put("press", new NamedApplicable("press") {
            // new thread, grab its ic
            @Override
            public void exe(Machine m) {
                final InputConnection ic = getCurrentInputConnection();
                if (ic == null)
                    throw new Machine.TFE("getCurrentInputConnection is null, cannot press");

                final Object o = m.stk.pop();
                if (o instanceof Keypress) handleKeypress((Keypress) o, ic);
                else if (o instanceof Str) handleStr((Str) o, ic);
                else throw new Machine.TypeMismatch(Keypress.class, o);
            }
        });

        m.dict.put("overlay", new NamedApplicable("overlay") {
            @Override
            public void exe(Machine m) {
                m.stk.push(overlay());
            }
        });
        m.dict.put("overlay!", new NamedApplicable("overlay!") {
            @Override
            public void exe(Machine m) {
                overlay(m.stk.pop(Boolean.class));
            }
        });

        m.dict.put(">>stroke", new NamedApplicable(">stroke") {
            @Override
            public void exe(Machine m) {
                IPair pStroke = m.stk.pop(IPair.class);
                m.stk.push(Stroke.fromPair(pStroke));
            }
        });

        m.dict.put("bind!", new NamedApplicable("bind!") {
            @Override
            public void exe(Machine m) {
                Object proc = m.stk.pop();
                Stroke stroke;
                try {
                    stroke = m.stk.pop(Stroke.class);
                } catch(Machine.TypeMismatch e) {
                    stroke = Stroke.fromPair(m.stk.pop(IPair.class));
                }
                if(stroke == null) {
                    Log.e(TAG, "cannot bind!: stroke is null");
                    return;
                }
                Log.i(TAG, "assigning layout: " + stroke + " " + proc);
                layout.put(stroke, proc);
            }
        });

        m.dict.put("log", new NamedApplicable("log") {
            @Override
            public void exe(Machine m) {
                Log.i(TAG, m.stk.pop(Str.class).value);
            }
        });

        m.searchPaths.add(0, ThumbkeyboardView.configDir());
        Layout.ensureExists(getAssets(), "default.layout.thumb");
        Layout.ensureExists(getAssets(), "main.thumb");

        try {
            m.stk.push(new Str(ThumbkeyboardView.configDir() + "main.thumb"));
            m.eval(new Machine.Word("load"));
        } catch (Throwable e) {
            e.printStackTrace();
            Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if(server == null) {
            server = new Thread(new Runnable() {
                @Override
                public void run() {
                    int port = 1234;
                    Log.i(TAG, "Starting server on port " + port);
                    Reader.serve(m, port);
                }
            });
            server.start();
        }
    }

    @Override
    public View onCreateInputView() {
        viewInput = createThumbkeyboardView(false);
        return viewInput;
    }

    @Override
    public View onCreateCandidatesView() {
        viewCandidates = createThumbkeyboardView(true);
        if(!overlay())
            viewCandidates.setVisibility(View.GONE);
        // ^ otherwise, it will hide viewInput (since it has superheight)
        return viewCandidates;
    }

    private int keypressMetastate(Keypress key) {
        int mask = 0;
        if (key.shift) mask |= META_SHIFT_ON | META_SHIFT_LEFT_ON;
        if (key.ctrl)  mask |= META_CTRL_ON  | META_CTRL_LEFT_ON;
        if (key.alt)   mask |= META_ALT_ON   | META_ALT_LEFT_ON;
        if (key.win)   mask |= META_META_ON  | META_META_LEFT_ON;
        return mask;
    }

    private void handleKeypress(Keypress key, InputConnection ic) {
        final int meta = keypressMetastate(key);
        final long now = System.currentTimeMillis();
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, key.keycode, 0, meta));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,   key.keycode, 0, meta));
    }

    private void handleStr(Str input, InputConnection ic) {
        ic.commitText(input.value, 0);
    }

    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setCandidatesViewShown(overlay());
    }

    @Override public boolean onEvaluateFullscreenMode() {
      return false;
   }

    @Override public void onFinishInput() {
        setCandidatesViewShown(false);
        super.onFinishInput();
    }

    public void handleStroke(Stroke stroke) {
        final Object op = layout.get(stroke);
        Log.i(TAG, "handleStroke: " + op);
        m.dict.put("last", stroke);

        if(op == null) {
            try {
                m.eval(new Machine.Word("missing"));
            } catch (Throwable e) {
                Log.e(TAG, "running `missing`: " + e.getMessage());
            }
            return;
        } else {
            // we found a procedure for stroke, go!
            m.dict.put("last.op", op);
            try {
                if(op instanceof Keypress || op instanceof Str) {
                    m.stk.push(op);
                    m.eval(m.dict.get("press"));
                } else {
                    m.eval(op);
                }
            } catch (Throwable e) {
                Log.i(TAG, "failed handling stroke:" + stroke);
                e.printStackTrace();
            }
        }
    }
}

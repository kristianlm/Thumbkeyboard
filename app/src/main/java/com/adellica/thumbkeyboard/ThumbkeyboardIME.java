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
import com.adellica.thumbkeyboard.tsm.stack.IPair;

import java.util.HashMap;
import java.util.Map;


public class ThumbkeyboardIME extends InputMethodService {
    private static final String TAG = "TSM";

    private final Handler mHandler = new Handler();

    // only one machine and one server per app instance
    public static final Machine m = new Machine();
    public final Map<Stroke, Object> layout = new HashMap<>();

    /**
     * toggle using overlay(boolean) method.
     */
    private boolean _overlaymode = false;
    final Config config = new Config();

    private ThumbkeyboardView viewInput; // used for non-overlay
    private ThumbkeyboardView viewCandidates; // used for fullscreen overlay

    public boolean overlay() {
        return _overlaymode;
    }

    private void redraw() {
        viewCandidates.postInvalidate();
        viewInput.postInvalidate();
    }

    /**
     * fullblown-fullscreen overlay or standard opaque keyboard view with a height?
     *
     * @param value whether overlay mode is enabled
     */
    public void overlay(boolean value) {
        if (value == _overlaymode) return;
        _overlaymode = value;

        mHandler.post(() -> {
            setCandidatesViewShown(_overlaymode);
            if (_overlaymode) {
                viewInput.setVisibility(View.GONE);
                viewCandidates.setVisibility(View.VISIBLE);
            } else {
                viewInput.setVisibility(View.VISIBLE);
                viewCandidates.setVisibility(View.GONE);
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
        m.dict.put("press*", m.dict.get("press")); // in case you want to override it.

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
                } catch (Machine.TypeMismatch e) {
                    stroke = Stroke.fromPair(m.stk.pop(IPair.class));
                }
                if (stroke == null) {
                    Log.e(TAG, "cannot bind!: stroke is null");
                    return;
                }
                Log.i(TAG, "assigning layout: " + stroke + " " + proc);
                layout.put(stroke, proc);
            }
        });

        m.dict.put("layout", new NamedApplicable("layout") {
            @Override
            public void exe(Machine m) {
                m.stk.push(layout);
            }
        });

        m.dict.put("log", new NamedApplicable("log") {
            @Override
            public void exe(Machine m) {
                Log.i(TAG, m.stk.pop(Str.class).value);
            }
        });

        m.dict.put("label.color!", new NamedApplicable("label.color!") {
            @Override
            public void exe(Machine m) {
                config.colorLabel = Config.s2c(m.stk.pop(Str.class).value);
                redraw();
            }
        });
        m.dict.put("background.color!", new NamedApplicable("background.color!") {
            @Override
            public void exe(Machine m) {
                config.colorBackgroundIdle = Config.s2c(m.stk.pop(Str.class).value);
                redraw();
            }
        });

        m.searchPaths.add(0, ThumbkeyboardView.configDir());
        FileCopier.ensureExists(getAssets(), "default.layout.thumb");
        FileCopier.ensureExists(getAssets(), "main.thumb");

        try {
            m.stk.push(new Str(ThumbkeyboardView.configDir() + "main.thumb"));
            m.eval(new Machine.Word("load"));
        } catch (Throwable e) {
            e.printStackTrace();
            Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        if (!overlay())
            viewCandidates.setVisibility(View.GONE);
        // ^ otherwise, it will hide viewInput (since it has superheight)
        return viewCandidates;
    }

    private void handleKeypress(Keypress key, InputConnection ic) {
        final long now = System.currentTimeMillis();
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, key.keycode, 0, key.getMetaState()));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, key.keycode, 0, key.getMetaState()));
    }

    private void handleStr(Str input, InputConnection ic) {
        ic.commitText(input.value, 1); // 1 means 'end of new inserted text'
    }

    @Override
    public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setCandidatesViewShown(overlay());
        if ((info.inputType & 3) == 2 || (info.inputType & 3) == 3) {
            m.eval(m.dict.get("numpad"));
        } else {
            m.eval(m.dict.get("latin"));
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void onFinishInput() {
        setCandidatesViewShown(false);
        super.onFinishInput();
    }

    public void handleStroke(Stroke stroke) {
        final Object op = layout.get(stroke);
        Log.i(TAG, "handleStroke: " + op);

        if (op == null) {
            try {
                m.eval(new Machine.Word("missing"));
            } catch (Throwable e) {
                Log.e(TAG, "running `missing`: " + e.getMessage());
            }
            return;
        } else {
            // we found a procedure for stroke, go!
            try {
                if (op instanceof Keypress || op instanceof Str) {
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
        m.dict.put("last", stroke);
        m.dict.put("last.op", op);
    }
}

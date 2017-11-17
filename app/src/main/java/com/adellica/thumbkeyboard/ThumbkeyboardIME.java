// 16may16abu
// (c) Software Lab. Alexander Burger

package com.adellica.thumbkeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.adellica.thumbkeyboard.tsm.Keypress;
import com.adellica.thumbkeyboard.tsm.Library.NamedApplicable;
import com.adellica.thumbkeyboard.tsm.Machine;
import com.adellica.thumbkeyboard.tsm.Machine.Str;
import com.adellica.thumbkeyboard.tsm.Reader;
import com.adellica.thumbkeyboard3.R;

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

    // only one machine and one server per app instance
    public static Machine m = new Machine();
    public static Thread server = null;

    @Override
    public View onCreateInputView() {
        Log.i(TAG, "onCreateInputView");

        // always overwrite press, so getCurrentInputConnection always gets the newest instance of ThumbkeyboardIME
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
                else throw new Machine.TypeMismatch(Keypress.class, o.getClass());
            }
        });

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


        ThumbkeyboardView tv = new ThumbkeyboardView(getApplicationContext(), null);
        tv.Ime = this;
        return tv;
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
        setCandidatesViewShown(true);
    }

    @Override public boolean onEvaluateFullscreenMode() {
      return false;
   }

    @Override public void onFinishInput() {
        setCandidatesViewShown(false);
        super.onFinishInput();
    }
}

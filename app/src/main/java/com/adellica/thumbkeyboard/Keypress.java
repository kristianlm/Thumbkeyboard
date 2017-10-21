package com.adellica.thumbkeyboard;

import android.view.KeyEvent;

/**
 * Created by klm on 10/21/17.
 */

public class Keypress {
    final boolean ctrl, alt, shift, meta;

    public Keypress(boolean ctrl, boolean alt, boolean shift, boolean meta) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.meta = meta;
    }

    private int getMetaState() {
        int mask = 0;
        if (shift) mask |= KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        if (ctrl)  mask |= KeyEvent.META_CTRL_ON  | KeyEvent.META_CTRL_LEFT_ON;
        if (alt)   mask |= KeyEvent.META_ALT_ON   | KeyEvent.META_ALT_LEFT_ON;
        if (meta)  mask |= KeyEvent.META_META_ON  | KeyEvent.META_META_LEFT_ON;
        return mask;
    }

}

package com.adellica.thumbkeyboard;


import com.adellica.thumbkeyboard.ThumbJoy.InvalidToken;

import java.util.HashMap;
import java.util.Map;

public class Keypress {
    final boolean ctrl, alt, shift, win;
    final int keycode;

    public static final int META_ALT_ON = 2;
    public static final int META_ALT_LEFT_ON = 16;
    public static final int META_ALT_RIGHT_ON = 32;
    public static final int META_SHIFT_ON = 1;
    public static final int META_SHIFT_LEFT_ON = 64;
    public static final int META_SHIFT_RIGHT_ON = 128;
    public static final int META_SYM_ON = 4;
    public static final int META_FUNCTION_ON = 8;
    public static final int META_CTRL_ON = 4096;
    public static final int META_CTRL_LEFT_ON = 8192;
    public static final int META_CTRL_RIGHT_ON = 16384;
    public static final int META_META_ON = 65536;
    public static final int META_META_LEFT_ON = 131072;
    public static final int META_META_RIGHT_ON = 262144;

    public Keypress(int keycode, boolean shift, boolean ctrl, boolean alt, boolean meta) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.win = meta;
        this.keycode = keycode;
    }

    public int getMetaState() {
        int mask = 0;
        if (shift) mask |= META_SHIFT_ON | META_SHIFT_LEFT_ON;
        if (ctrl)  mask |= META_CTRL_ON  | META_CTRL_LEFT_ON;
        if (alt)   mask |= META_ALT_ON   | META_ALT_LEFT_ON;
        if (win)   mask |= META_META_ON  | META_META_LEFT_ON;
        return mask;
    }

    @Override
    public String toString() {
        String s = ":";
        if(ctrl) s += "C-";
        if(alt) s += "M-";
        if(win) s += "s-";
        for(String key : specials.keySet()) {
            if(specials.get(key) == keycode) {
                if(!shift) throw new RuntimeException("this should never happen");
                // found special key, show C-! instead of C-S-1
                return s + key;
            }
        }
        final String description = ThumboardKeycodes.toString(keycode);
        if(shift) {
            if(description.length() == 1) {
                // one-char description means it's a simple letter
                return s + description.toUpperCase();
            }
            s += "S-";
        }
        return s + description.toLowerCase();
    }

    public static Keypress fromString(String inn) {
        boolean s = false, c = false, a = false, w = false;
        String in = inn;
        while(in.length() >= 2) {
            final Integer keycode = ThumboardKeycodes.fromString(in.toUpperCase());
            if("-".equals(in.substring(1, 2))) {
                switch(in.charAt(0)) {
                    case 'C': c = true; break;
                    case 'S': s = true; break;
                    case 'M': a = true; break;
                    case 's': w = true; break;
                    default: throw new InvalidToken(in.substring(0, 1) + " in " + inn);
                }
                in = in.substring(2);
            } else if( keycode != null) {
                break;
            } else {
                throw new InvalidToken("missing “-” in " + in + " of " + inn);
            }
        }
        if(in.length() != 1) throw new InvalidToken("unexpected end of sequence in " + inn);
        if(specials.get(in) != null) {
            s = true;
            in = ThumboardKeycodes.toString(specials.get(in));
        }

        Integer kp = ThumboardKeycodes.fromString(in);
        if(kp == null) {
            kp = ThumboardKeycodes.fromString(in.toUpperCase());
        } else {
            s = true; // found "A" without capitalizing, must be shift
        }
        if(kp == null) throw new InvalidToken("key not found in db " + in + " in " + inn);
        return new Keypress(kp, s, c, a, w);
    }

    public final static Map<String, Integer> specials = new HashMap<String, Integer>();
    private static void pr(String name, String ref) {
        specials.put(name, ThumboardKeycodes.fromString(ref));
    }

    static {
        // special-care symbols that so that we can do :! instead of having to do :S-1
        pr("!", "1");
        pr("@", "2");
        pr("#", "3");
        pr("$", "4");
        pr("%", "5");
        pr("^", "6");
        pr("&", "7");
        pr("*", "8");
        pr("(", "9");
        pr(")", "0");
        pr("_", "MINUS");
        pr("+", "EQUALS");
        pr("|", "BACKSLASH");
        pr("}", "LEFT_BRACKET");
        pr("{", "RIGHT_BRACKET");
        pr("\"", "GRAVE");
        pr(":", "SEMICOLON");
        pr("?", "SLASH");
        pr(">", "PERIOD");
        pr("<", "COMMA");
        pr("~", "APOSTROPHE");
    }
}

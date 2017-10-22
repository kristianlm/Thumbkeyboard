package com.adellica.thumbkeyboard;


import com.adellica.thumbkeyboard.JoyLibrary.NamedApplicable;
import com.adellica.thumbkeyboard.ThumbJoy.InvalidToken;

import java.util.HashMap;
import java.util.Map;

import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;

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

    public Keypress(int keycode, boolean shift, boolean ctrl, boolean alt, boolean win) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.win = win;
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

    public static Keypress fromString(String in) {
        boolean s = false, c = false, a = false, w = false;
        Integer keycode = null;
        while(in.length() >= 2) {
            if(ThumboardKeycodes.fromString(in.toUpperCase()) != null) {
                break; // in is good as it is, eg "DEL"
            } else if("-".equals(in.substring(1, 2))) {
                switch(in.charAt(0)) {
                    case 'C': c = true; break;
                    case 'S': s = true; break;
                    case 'M': a = true; break;
                    case 's': w = true; break; // emacs uses s for "super"
                    case 'W': w = true; break;
                    default: throw new InvalidToken(in.substring(0, 1) + " in " + in);
                }
                in = in.substring(2);
            } else {
                throw new InvalidToken("key “"+in+"” unknown");
            }
        }
        //if(keycode == null) throw new InvalidToken("unexpected end of sequence in " + inn);
        if(specials.get(in) != null) {
            s = true;
            in = ThumboardKeycodes.toString(specials.get(in));
        }

        Integer kp = ThumboardKeycodes.fromString(in);
        if(kp == null) {
            kp = ThumboardKeycodes.fromString(in.toUpperCase());
        } else {
            if(in.length() == 1)
            s = true; // found single-letter description "A" without capitalizing, hold shift
        }
        if(kp == null) throw new InvalidToken("key “"+in+"” unknown");
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

    abstract public static class ModifierSetter extends NamedApplicable {
        public ModifierSetter(String name) {super(name);}
        @Override
        public ThumbJoy.Machine exe(ThumbJoy.Machine m) {
                Keypress kp = m.stk.car(Keypress.class);
                return M(cons(new Keypress(kp.keycode, shift(kp.shift), ctrl(kp.ctrl), alt(kp.alt), win(kp.win)), m.stk.cdr()), m);
        }
        public boolean shift(boolean shift) {return shift; }
        public boolean ctrl(boolean ctrl) { return ctrl; }
        public boolean alt(boolean alt) { return alt; }
        public boolean win(boolean win) { return  win; }
    }
    abstract public static class ModifierGetter extends NamedApplicable {
        @Override
        public ThumbJoy.Machine exe(ThumbJoy.Machine m) {
            Keypress kp = m.stk.car(Keypress.class);
            return M(cons(get(kp), m.stk.cdr()), m);
        }
        abstract public boolean get(Keypress kp);
    }

    @SuppressWarnings("unused")
    public static class Keypresses extends JoyLibrary {
        public static final NamedApplicable shift_ = new ModifierSetter("shift!") {
            public boolean shift(boolean val) {return true;}
        };
        public static final NamedApplicable ctrl_ = new ModifierSetter("ctrl!") {
            public boolean ctrl(boolean val) {return true;}
        };
        public static final NamedApplicable alt_ = new ModifierSetter("alt!") {
            public boolean alt(boolean val) {return true;}
        };
        public static final NamedApplicable win_ = new ModifierSetter("win!") {
            public boolean win(boolean val) {return true;}
        };

        public static final NamedApplicable shift_toggle = new ModifierSetter("!shift") {
            public boolean shift(boolean val) {return !val;}
        };
        public static final NamedApplicable ctrl_toggle = new ModifierSetter("!ctrl") {
            public boolean ctrl(boolean val) {return !val;}
        };
        public static final NamedApplicable alt_toggle = new ModifierSetter("!alt") {
            public boolean alt(boolean val) {return !val;}
        };
        public static final NamedApplicable win_toggle = new ModifierSetter("!win") {
            public boolean win(boolean val) {return !val;}
        };

        public static final NamedApplicable shift = new ModifierGetter() {
            public boolean get(Keypress kp) { return kp.shift;}
        };
        public static final NamedApplicable ctrl = new ModifierGetter() {
            public boolean get(Keypress kp) { return kp.ctrl;}
        };
        public static final NamedApplicable alt = new ModifierGetter() {
            public boolean get(Keypress kp) { return kp.alt;}
        };
        public static final NamedApplicable win = new ModifierGetter() {
            public boolean get(Keypress kp) { return kp.win;}
        };
    }
}

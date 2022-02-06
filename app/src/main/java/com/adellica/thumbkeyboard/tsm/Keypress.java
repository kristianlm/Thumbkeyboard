package com.adellica.thumbkeyboard.tsm;


import com.adellica.thumbkeyboard.tsm.Library.NamedApplicable;
import com.adellica.thumbkeyboard.tsm.Machine.InvalidToken;

import java.util.HashMap;
import java.util.Map;

public class Keypress {
    public final boolean ctrl, alt, shift, win;
    public final int keycode;

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
        if (ctrl) mask |= META_CTRL_ON | META_CTRL_LEFT_ON;
        if (alt) mask |= META_ALT_ON | META_ALT_LEFT_ON;
        if (win) mask |= META_META_ON | META_META_LEFT_ON;
        return mask;
    }

    public final static Map<String, Integer> specials = new HashMap<>();

    private static void pr(String name, String ref) {
        Integer kk = Keycodes.fromString(ref);
        if (kk == null)
            throw new RuntimeException("internal error JD8: " + name + " not found in Keycodes");
        specials.put(name, kk);
    }

    public static Keypress fromString(String in) {
        boolean s = false, c = false, a = false, w = false;
        Integer keycode = null;
        while (in.length() >= 2) {
            if (Keycodes.fromString(in.toUpperCase()) != null) {
                break; // in is good as it is, eg "DEL"
            } else if ("-".equals(in.substring(1, 2))) {
                switch (in.charAt(0)) {
                    case 'C':
                        c = true;
                        break;
                    case 'S':
                        s = true;
                        break;
                    case 'M':
                        a = true;
                        break;
                    case 's':
                    case 'W':
                        w = true;
                        break; // emacs uses s for "super"
                    default:
                        throw new InvalidToken(in.charAt(0) + " in " + in);
                }
                in = in.substring(2);
            } else {
                throw new InvalidToken("key “" + in + "” unknown");
            }
        }
        //if(keycode == null) throw new InvalidToken("unexpected end of sequence in " + inn);
        if (specials.get(in) != null) {
            s = true;
            in = Keycodes.toString(specials.get(in));
        }

        Integer kp = Keycodes.fromString(in);
        if (kp == null) {
            kp = Keycodes.fromString(in.toUpperCase());
        } else {
            if (in.length() == 1 && !in.toLowerCase().equals(in.toUpperCase()))
                s = true; // found single-letter description "A" without capitalizing, hold shift
        }
        if (kp == null) throw new InvalidToken("key “" + in + "” unknown");
        return new Keypress(kp, s, c, a, w);
    }

    @Override
    public String toString() {
        String s = ":";
        if (ctrl) s += "C-";
        if (alt) s += "M-";
        if (win) s += "s-";
        for (String key : specials.keySet()) {
            if (specials.get(key) == null)
                throw new RuntimeException("internal error: 0mwhc " + key);
            if (specials.get(key) == keycode) {
                if (shift)
                    // found special key, show C-! instead of C-S-1
                    return s + key;
            }
        }
        final String description = Keycodes.toString(keycode);
        if (shift) {
            if (description.length() == 1) {
                // one-char description means it's a simple letter
                return s + description.toUpperCase();
            }
            s += "S-";
        }
        return s + description.toLowerCase();
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
        pr("_", "-");
        pr("+", "=");
        pr("|", "\\");
        pr("}", "]");
        pr("{", "[");
        pr("\"", "'");
        pr(":", ";");
        pr("?", "/");
        pr(">", ".");
        pr("<", ",");
        pr("~", "`");
    }

    abstract public static class ModifierSetter extends NamedApplicable {
        public ModifierSetter(String name) {
            super(name);
        }

        public void exe(Machine m) {
            final Boolean v = m.stk.pop(Boolean.class);
            final Keypress kp = m.stk.pop(Keypress.class);
            m.stk.push(new Keypress(kp.keycode, shift(kp.shift, v), ctrl(kp.ctrl, v), alt(kp.alt, v), win(kp.win, v)));
        }

        public boolean shift(boolean shift, boolean s) {
            return shift;
        }

        public boolean ctrl(boolean ctrl, boolean s) {
            return ctrl;
        }

        public boolean alt(boolean alt, boolean s) {
            return alt;
        }

        public boolean win(boolean win, boolean s) {
            return win;
        }
    }

    abstract public static class ModifierGetter extends NamedApplicable {
        public ModifierGetter(String s) {
            super(s);
        }

        @Override
        public void exe(Machine m) {
            Keypress kp = m.stk.pop(Keypress.class);
            m.stk.push(get(kp));
        }

        abstract public boolean get(Keypress kp);
    }

    @SuppressWarnings("unused")
    public static class Keypresses extends Library {
        public static final NamedApplicable shift_ = new ModifierSetter("shift!") {
            @Override
            public boolean shift(boolean val, boolean v) {
                return v;
            }
        };
        public static final NamedApplicable ctrl_ = new ModifierSetter("ctrl!") {
            @Override
            public boolean ctrl(boolean val, boolean v) {
                return v;
            }
        };
        public static final NamedApplicable alt_ = new ModifierSetter("alt!") {
            @Override
            public boolean alt(boolean val, boolean v) {
                return v;
            }
        };
        public static final NamedApplicable win_ = new ModifierSetter("win!") {
            @Override
            public boolean win(boolean val, boolean v) {
                return v;
            }
        };

        public static final NamedApplicable shift_p = new ModifierGetter("shift?") {
            public boolean get(Keypress kp) {
                return kp.shift;
            }
        };
        public static final NamedApplicable ctrl_p = new ModifierGetter("ctrl?") {
            public boolean get(Keypress kp) {
                return kp.ctrl;
            }
        };
        public static final NamedApplicable alt_p = new ModifierGetter("alt?") {
            public boolean get(Keypress kp) {
                return kp.alt;
            }
        };
        public static final NamedApplicable win_p = new ModifierGetter("win?") {
            public boolean get(Keypress kp) {
                return kp.win;
            }
        };

        public static final NamedApplicable keycode = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.push(m.stk.pop(Keypress.class).keycode);
            }
        };
        public static final NamedApplicable keypress = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.push(new Keypress(m.stk.pop(Integer.class), false, false, false, false));
            }
        };
    }
}


package com.adellica.thumbkeyboard;

import com.adellica.thumbkeyboard.ThumbJoy.Applicable;
import com.adellica.thumbkeyboard.ThumbJoy.IPair;
import com.adellica.thumbkeyboard.ThumbJoy.Machine;
import com.adellica.thumbkeyboard.ThumbJoy.Pair;
import com.adellica.thumbkeyboard.ThumbJoy.TFE;
import com.adellica.thumbkeyboard.ThumbJoy.Word;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;

import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.isTrue;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.list;

/**
 * a JoyLibrary is just a class where
 * all fields are static and of type {@link NamedApplicable}
 */

public class JoyLibrary {

    public abstract static class NamedApplicable implements Applicable {
        public String name = null;
        public NamedApplicable() {}
        public NamedApplicable(String name) {this.name=name;}
        @Override public String toString() {return "\u001b[34m‹" + name + "›\u001b[0m";}
    }
    public static void init(JoyLibrary library) {
        for(Field field : library.getClass().getFields()) {
            try {
                final Object o = field.get(library);
                if(!(o instanceof NamedApplicable)) continue;
                NamedApplicable na = ((NamedApplicable)o);
                if(na.name == null) na.name = field.getName();
            } catch (IllegalAccessException e) {e.printStackTrace();}
        }
    }

    public static void fillDict(Map<String, Object> dict, JoyLibrary lib) {
        init(lib);
        for(Field field : lib.getClass().getFields()) {
            try {
                Object o = field.get(lib);
                if(!(o instanceof NamedApplicable)) continue;
                NamedApplicable na = (NamedApplicable)o;
                dict.put(na.name, na);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Core extends JoyLibrary {

        public static Applicable drop = new NamedApplicable() {
            public Machine exe(Machine m) {
                return M(m.stk.cdr(), m);
            }
        };
        public static Applicable dup = new NamedApplicable() {
            public Machine exe(Machine m) {
                return M(cons(m.stk.car(), m.stk), m);
            }
        };
        public static Applicable swap = new NamedApplicable() {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                Object e0 = p.car(); p = p.cdr();
                Object e1 = p.car(); p = p.cdr();
                return M(cons(e1, cons(e0, p)), m);
            }
        };
        public static Applicable dd = new NamedApplicable("dd") {
            public Machine exe(Machine m) {
                Object o = m.dict.get("println");
                if(o == null) throw new ThumbJoy.InvalidReference("println");
                M(list(m.dict), m).eval(o);
                return m;
            }
        };
        public static Applicable i = new NamedApplicable() {
            public Machine exe(Machine m) {
                Machine mm = M(m.stk.cdr(), m);
                return mm.eval(m.stk.car());
            }
        };
        public static Applicable QUOTE = new NamedApplicable("'") {
            public Machine exe(Machine m) {
                if(m.code == Pair.nil) throw new RuntimeException("QUOTE: unexpected eof");
                return M(cons(m.code.car(), m.stk), m.code.cdr(), m);
            }
        };
        public static Applicable type = new NamedApplicable() {
            public Machine exe(Machine m) {
                return M(cons(m.stk.car().getClass(), m.stk.cdr()), m);
            }
        };
        public static Applicable get = new NamedApplicable() {
            @Override
            public Machine exe(Machine m) {
                final Word name = m.stk.car(Word.class);
                final Object o = m.get(name.value, Object.class);
                return M(cons(o, m.stk.cdr()), m);
            }
        };
        public static Applicable set = new NamedApplicable() {
            @Override
            public Machine exe(Machine m) {
                final Word name = m.stk.cdr().car(Word.class);
                final Object content = m.stk.car();
                m.dict.put(name.value, content);
                return M(m.stk.cdr().cdr(), m);
            }
        };
        public static Applicable println = new NamedApplicable() {
            @Override
            public Machine exe(Machine m) {
                OutputStream os = m.get("out", OutputStream.class);
                try {
                    os.write((m.stk.car().toString() + "\n").getBytes());
                    os.flush();
                } catch (final IOException e) {
                    throw new TFE() {
                        public String getMessage() { return "io error " + e; }
                    };
                }
                return M(m.stk.cdr(), m);
            }
        };
        public static Applicable ifte = new NamedApplicable("ifte") {
            @Override
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Object e = p.car(); p = p.cdr();
                final Object t = p.car(); p = p.cdr();
                final Object i = p.car(); p = p.cdr();
                if(isTrue(i)) return m.eval(t);
                else return m.eval(e);
            }
        };

    }

    public static class Math extends JoyLibrary {

        public static final Applicable PLUS = new NamedApplicable("+") {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Long n = p.car(Long.class); p = p.cdr();
                final Long d = p.car(Long.class); p = p.cdr();
                return M(cons(new Long(n.longValue() + d.longValue()), p), m);
            }
        };
        public static final Applicable MINUS = new NamedApplicable("-") {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Long n = p.car(Long.class); p = p.cdr();
                final Long d = p.car(Long.class); p = p.cdr();
                return M(cons(new Long(d.longValue() - n.longValue()), p), m);
            }
        };
        public static final Applicable MULTIPLY = new NamedApplicable("*") {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Long n = p.car(Long.class); p = p.cdr();
                final Long d = p.car(Long.class); p = p.cdr();
                return M(cons(new Long(d.longValue() * n.longValue()), p), m);
            }
        };
        public static final Applicable DIVIDE = new NamedApplicable("/") {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Long n = p.car(Long.class); p = p.cdr();
                final Long d = p.car(Long.class); p = p.cdr();
                return M(cons(new Long(d.longValue() / n.longValue()), p), m);
            }
        };
        public static final Applicable MODULUS = new NamedApplicable("%") {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Long n = p.car(Long.class); p = p.cdr();
                final Long d = p.car(Long.class); p = p.cdr();
                return M(cons(new Long(d.longValue() % n.longValue()), p), m);
            }
        };
    }

    public static class Strings extends JoyLibrary {
        public static final Applicable concat = new NamedApplicable() {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                ThumbJoy.Str s0 = p.car(ThumbJoy.Str.class);
                p = p.cdr();
                ThumbJoy.Str s1 = p.car(ThumbJoy.Str.class);
                p = p.cdr();
                return M(cons(new ThumbJoy.Str(s1.value + s0.value), p), m);
            }
        };
    }
}

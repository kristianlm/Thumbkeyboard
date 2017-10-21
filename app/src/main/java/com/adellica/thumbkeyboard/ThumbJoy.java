package com.adellica.thumbkeyboard;

import java.util.HashMap;
import java.util.Map;

import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;


public class ThumbJoy {
    public interface Applicable {
        Machine exe(Machine m);
    }

    public static class TFE extends RuntimeException {
        public TFE() {}
        public TFE(String m) {super(m);}
        public String toString() {
            return this.getClass().getSimpleName() + " " + (getMessage()==null?"":getMessage());
        }
    }
    public static class EmptyStackPopped extends TFE {}
    public static class TypeMismatch extends TFE {
        public TypeMismatch(Class expected, Object got) {
            super("expected " + expected.getSimpleName() + ", got " + got);
        }
    }
    public static class InvalidReference extends TFE {
        public InvalidReference(String name) {
            super(name + " not found in dict (d for list)");
        }
    }
    public static class InvalidToken extends TFE {
        public InvalidToken(String s) { super(s); }
    }

    public interface IPair {
        Object car();
        IPair cdr();
        <T> T car(Class<T> t);
    }
    abstract public static class APair implements IPair, Applicable {
        @SuppressWarnings("unchecked")
        public <T> T car(Class<T> t) {
            if(t.isInstance(car())) return (T)car();
            throw new TypeMismatch(t, car());
        }
        public String toString() {
            if(this == Pair.nil) return "[ ]";
            String f = "[";
            IPair p = this;
            while(p != Pair.nil) {
                f += " " + p.car();
                p = p.cdr();
            }
            return f + " ]";
        }
        @Override
        public Machine exe(Machine m) {
            Machine mm = M(m.stk, this, m);
            while(mm.code != Pair.nil) {
                final Object read = mm.code.car();
                mm = M(mm.stk, mm.code.cdr(), mm).eval(read);
            }
            return M(mm.stk, m.code, m);
        }
    }
    public static class Pair extends APair implements Applicable {
        private final Object __car;
        private final IPair __cdr;
        public Pair(Object car, IPair cdr) { __car=car; __cdr=cdr;}
        public Object car() { return __car; }
        public IPair cdr() { return __cdr; }

        public static final Pair nil = new Pair(null, null) {
                public <T> T car(Class<T> t) { throw new EmptyStackPopped(); }
                public Object car() { throw new EmptyStackPopped(); }
                public Pair cdr() { throw new EmptyStackPopped(); }
                public Machine exe(Machine m) {return m;}
        };

        public static Pair cons(Object car, IPair cdr) { return new Pair(car, cdr); }
        public static Pair list(Object... args) {
            Pair p = nil;
            for(int i = args.length - 1; i >= 0 ; i--) {
                p = cons(args[i], p);
            }
            return p;
        }
        public static Pair reverse(IPair pair) {
            Pair result = nil;
            while(pair != nil) {
                result = cons(pair.car(), result);
                pair = pair.cdr();
            }
            return result;
        }
    }

    abstract public static class Datum<T> {
        final T value;
        public Datum(T value) { this.value = value; }
        abstract public String toString();
    }
    public static class Str extends Datum<String> {
        public Str(String s) { super(s); }
        public String toString() { return "\"" + ThumbReader.writeString(value) + "\""; }
    }
    public static class Keyword extends Datum<String> {
        public Keyword(String s) { super(s); }
        public String toString() { return ":" + value; }
    }
    public static class Word extends Datum<String> implements Applicable {
        public Word(String s) { super(s);}
        public String toString() { return value; }
        public Machine exe(Machine m) {
            Object o = m.dict.get(this.value);
            if(o == null) throw new InvalidReference(this.toString());
            return m.eval(o);
        }
    }

    public static class Machine {
        public final Map<String, Object> dict;
        public final IPair stk;
        public final IPair code;

        @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> t) {
            final Object ref = dict.get(key);
            if(t.isInstance(ref)) return (T)ref;
            if(ref == null) throw new InvalidReference(key);
            throw new TypeMismatch(t, ref);
        }
        public static Machine M(IPair stk, IPair code, Map<String, Object> dict) {
            return new Machine(stk, code, dict);
        }
        public static Machine M(IPair stk, IPair code, Machine m) {
            return new Machine(stk, code, m.dict);
        }
        public static Machine M(IPair stk, Machine m) {
            return new Machine(stk, m.code, m.dict);
        }

        public Machine(IPair stk, IPair code, Map<String, Object> dict) {

            this.stk = stk;
            this.code = code;
            this.dict = dict;
        }

        @Override
        public String toString() {
            return " << " + stk + "\n    " + code + "\n    " + dict + " >>";
        }

        public static Map<String, Object> dictDefault() {
            Map<String, Object> dict = new HashMap<String, Object>();
            JoyLibrary.fillDict(dict, new JoyLibrary.Core());
            JoyLibrary.fillDict(dict, new JoyLibrary.Math());
            JoyLibrary.fillDict(dict, new JoyLibrary.Strings());
            return dict;
        }

        public Machine eval(final Object o) {
            if (o instanceof Applicable) return ((Applicable) o).exe(this);
            return M(cons(o, stk), this); // defaults to "self evaluation"
        }
    }

    public static boolean isTrue(Object o) {
        if(o instanceof Keyword) {
            Keyword k = (Keyword)o;
            if("t".equals(k.value)) return true;
        }
        return false;
    }
}

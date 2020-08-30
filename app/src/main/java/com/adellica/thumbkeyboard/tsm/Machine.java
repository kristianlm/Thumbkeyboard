package com.adellica.thumbkeyboard.tsm;

import com.adellica.thumbkeyboard.tsm.stack.Pair;
import com.adellica.thumbkeyboard.tsm.stack.Stack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thumb Stack Machine
 * Core machine: it has a stack and a dictionary. you can eval things inside it, which will modify its stack.
 */
public class Machine {
    public interface Applicable {
        void exe(Machine m);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> t) {
        final Object ref = dict.get(key);
        if (t.isInstance(ref)) return (T) ref;
        if (ref == null) throw new InvalidReference(key);
        throw new TypeMismatch(t, ref);
    }

    public static class TFE extends RuntimeException {
        public TFE() {
        }

        public TFE(String m) {
            super(m);
        }

        public String toString() {
            return this.getClass().getSimpleName() + " " + (getMessage() == null ? "" : getMessage());
        }
    }

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

    public static class EmptyStackPopped extends TFE {
    }

    public static class InvalidToken extends TFE {
        public InvalidToken(String s) {
            super(s);
        }
    }

    abstract public static class Datum<T> {
        public final T value;

        public Datum(T value) {
            this.value = value;
        }

        abstract public String toString();

        @Override
        public boolean equals(Object o) {
            if (o instanceof Datum) return value.equals(((Datum) o).value);
            return false;
        }
    }

    public static class Str extends Datum<String> {
        public Str(String s) {
            super(s);
        }

        public String toString() {
            return "\"" + Reader.writeString(value) + "\"";
        }
    }


    public final Map<String, Object> dict;
    public final Stack stk;
    public final List<String> searchPaths = new ArrayList<>(); // used by "load"

    public static class Word extends Datum<String> implements Applicable {
        public Word(String s) {
            super(s);
        }

        public String toString() {
            return value;
        }

        public void exe(Machine m) {
            Object o = m.dict.get(this.value);
            if (o == null) throw new InvalidReference(this.toString());
            m.eval(o);
        }
    }

    public Machine() {
        this.stk = new Stack(Pair.nil);
        this.dict = dictDefault();
    }

    public Machine(Map<String, Object> dict) {
        this.stk = new Stack(Pair.nil);
        this.dict = dict;
    }

    public Machine(Stack stk, Map<String, Object> dict) {
        this.stk = stk;
        this.dict = dict;
    }

    @Override
    public String toString() {
        return " << " + stk + "\n    " + dict + " >>";
    }

    public static Map<String, Object> dictDefault() {
        Map<String, Object> dict = new HashMap<>();
        new Library.Core().fillDict(dict);
        new Library.Math().fillDict(dict);
        new Library.Strings().fillDict(dict);
        new Keypress.Keypresses().fillDict(dict);
        return dict;
    }

    public void eval(final Object o) {
        if (o instanceof Applicable) {
            ((Applicable) o).exe(this);
        } else {
            stk.push(o);
        }
    }

    public static class Quoted implements Applicable {
        final Object obj;

        public Quoted(Object obj) {
            this.obj = obj;
        }

        @Override
        public void exe(Machine m) {
            m.stk.push(obj);
        }

        @Override
        public String toString() {
            return "'" + obj.toString();
        }
    }
}




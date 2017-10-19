package com.adellica.thumbkeyboard;

import java.util.Collections;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Runnable;

public class ThumbJoy {
    public interface Env {
        public Object get(Object key);
    }
    public interface JoyMachine {
        public Pair eval(Pair stk);
    }
    
    public static class TFE extends RuntimeException {
        public TFE() {}
        public TFE(String m) {super(m);}
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

    public static class Pair implements JoyMachine {
        public final Object car;
        public final Pair cdr;
        public static Pair nil = new Pair(null, null);
        public Pair(Object car, Pair cdr) { this.car=car; this.cdr=cdr;}
        public static Pair cons(Object car, Pair cdr) { return new Pair(car, cdr); }
        public static Pair list(Object... args) {
            Pair p = Pair.nil;
            for(int i = args.length - 1; i >= 0 ; i--) {
                p = cons(args[i], p);
            }
            return p;
        }

        public String toString() {
            if(this == Pair.nil) return "[]";
            String f = "[";
            Pair p = this;
            while(p != nil) {
                f += " " + p.car;
                p = p.cdr;
            }
            return f + " ]";
        }
        
        public Pair eval(Pair stk) {
            Pair p = this;
            while(p != Pair.nil) {
                stk = run(p.car, stk);
                p = p.cdr;
            }            
            return stk;
        }
    }

    abstract public static class Datum<T> {
        final T value;
        public Datum(T value) { this.value = value; }
        public boolean equals(Object o) {
            if(o == null) return false;
            if(!(this.getClass().equals(o.getClass()))) return false;
            return value.equals(((Datum)o).value);
        }
    }
    public static class Word extends Datum<String> {
        public Word(String s) { super(s); }
        public String toString() { return value.toString(); }
    }
    public static class Keyword extends Datum<String> {
        public Keyword(String s) { super(s); }
        public String toString() { return ":" + value; }
    }

    public static class Reader {
        public static boolean isWs(int c) {
            switch(c) {
                case ' ': case '\t':
                case '\r': case '\n': return true;
                default: return false;
            }
        }
        // dummy token for ')'
        private static Object CLOSE_PAREN = new Object() {};
        // read a list of instructions (  :a :b drop )
        public static Pair readCodeBlock(InputStream is) {
            Pair lst = Pair.nil;
            while(true) {
                Object o = read(is);
                if(o == null) break;
                if(o == CLOSE_PAREN) break;
                lst = Pair.cons(o, lst);
            }
            return lst;
        }

        public static Object read(InputStream is) {
            int c;
            try {
                while((c = is.read()) >= 0) {
                    if(isWs(c)) continue;
                    switch(c) {
                        case ':': return new Keyword(readUntilWS(is, ""));
                        case '?': return new Boolean("t".equals(readUntilWS(is, "")));
                        case ')': return CLOSE_PAREN;
                        case '(': return readCodeBlock(is);
                        default:  return new Word(readUntilWS(is, String.format("%c", c)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static String readUntilWS (InputStream is, String init) throws IOException {
            int c;
            while((c = is.read()) >= 0) {
                if(isWs(c)) break;
                init += String.format("%c", c);
            }
            return init;
        }
    }

    /**
     show user prompt only on ENTER keys for less noise
     */
    public static class InputStreamEnterCallback extends InputStream {
        final InputStream source;
        final Runnable callback;
        public InputStreamEnterCallback(InputStream source, Runnable callback) {
            this.source = source;
            this.callback = callback;
        }
        private boolean showPrompt = true;
        public int read() throws IOException {
            if(showPrompt) {
                callback.run();
                showPrompt = false;
            }
            final int c = source.read();
            if(c == '\n') showPrompt = true;
            return c;
        }
        public void close() throws IOException {
            source.close();
        }
    }

    public static Pair run(Object o, Pair stk) {
        if(o instanceof JoyMachine) return ((JoyMachine)o).eval(stk);
        return Pair.cons(o, stk);
    }

    public final static JoyMachine _PLUS = new JoyMachine() {
            public Pair eval(Pair stk) {
                Integer i0 = (Integer)stk.car;
                Integer i1 = (Integer)stk.cdr.car;
                return Pair.cons(i0 + i1, stk.cdr.cdr);
            }
        };

    public static void main(String[] args) {
        System.out.println("hello " + Pair.nil);
        System.out.println(run(Pair.list(2, 3, _PLUS), Pair.nil));
    }
}

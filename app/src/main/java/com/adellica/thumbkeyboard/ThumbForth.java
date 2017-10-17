package com.adellica.thumbkeyboard;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Runnable;

public class ThumbForth {

    public interface Code {
        Obj pop();
        void push(Obj o);
    }

    public interface Obj {
        void exe(Machine m);
    }

    public static class TFE extends RuntimeException {
        public TFE() {}
        public TFE(String m) {super(m);}
    }
    public static class EmptyStackPopped extends TFE {}
    public static class TypeMismatch extends TFE {
        public TypeMismatch(Class expected, Obj got) {
            super("expected " + expected.getSimpleName() + ", got " + got);
        }
    }
    public static class InvalidReference extends TFE {
        public InvalidReference(String name) {
            super(name + " not found in dict (d for list)");
        }
    }

    abstract public static class Datum<T> implements Obj {
        final T value;
        public Datum(T value) {
            this.value = value;
        }
        public void exe(Machine m) {
            // default behaviour is self-evaluation
            m.stack.push(this);
        }
        public boolean equals(Object o) {
            if(o == null) return false;
            if(!(this.getClass().equals(o.getClass()))) return false;

            return value.equals(((Datum)o).value);
        }
        abstract public String toString();
    }

    public static class Keyword extends Datum<String> {
        public Keyword(String v) { super(v); }
        public String toString() {
            return ":" + value;
        }
    }
    public static class Str extends Datum<String> {
        public Str(String v) { super(v); }
        public String toString() {
            return "\"" + value + "\"";
        }
    }
    public static class Boolean extends Datum<java.lang.Boolean> {
        public Boolean(boolean b) { super(b); }
        public String toString() {
            return value.booleanValue() ? "?t" : "?f";
        }
    }
    public static class Word extends Datum<String> {
        public Word(String v) { super(v); }
        public String toString() {
            return value;
        }
        public void exe(Machine m) {
            Obj ref = m.dict.get(value);
            if(ref == null) throw new InvalidReference(value);
            ref.exe(m);
        }
    }
    public static class Proc extends Datum<List<Obj>> {
        // s here should be in reverese order!
        public Proc(List<Obj> s) { super(s); }
        public String toString() {
            return value.toString();
        }
        public void exe(Machine m) {
            final Code original = m.code();
            final Stack<Obj> source = new Stack<Obj>();
            for(int i = value.size()-1 ; i >= 0 ; i--) {
                source.push(value.get(i));
            }
            m.code(new Code() {
                @Override
                public Obj pop() {
                    if(source.isEmpty()) return null;
                    return source.pop();
                }
                @Override
                public void push(Obj o) {
                    source.push(o);
                }
            });
            m.code(original);
            m.run();
        }
    }

    public static class CodeBlock extends Datum<List<Obj>> {
        public CodeBlock(List<Obj> v) { super(v); }
        public String toString() {
            return '@' + value.toString();
        }
        public void exe(Machine m) {
            m.push(new Proc(value));
        }
    }

    // InputStream combined with a stack so we can push data to it.
    public static class InputCode implements Code {
        final InputStream is;
        public InputCode(InputStream is) {
            this.is = is;
        }
        Stack<Obj> stack = new Stack<Obj>();
        public Obj pop() {
            if(!stack.isEmpty()) return stack.pop();
            return Reader.read(is);
        }
        public void push(Obj obj) {
            stack.push(obj);
        }
    }

    public static class Machine {
        public Stack<Obj> stack = new Stack<Obj>();
        Map<String, Obj> dict = new HashMap<String, Obj>();
        private Code code;


        public void push(Obj o) { stack.push(o); }

        @SuppressWarnings("unchecked")
        public <T> T pop(Class<T> t) {
            if(stack.isEmpty()) throw new EmptyStackPopped();
            Obj r = stack.pop();
            if(t.isInstance(r)) return (T)r;
            throw new TypeMismatch(t, r);
        }

        public Code code() { return code; }
        public void code(Code code) { this.code = code; }
        public void code(InputStream is) { this.code = new InputCode(is); }

        public void run() {
            while (true) {
                try {
                    Obj op = code.pop();
                    if (op == null) break;
                    op.exe(this);
                } catch (TFE e) {
                    System.err.println("obs: " + e.getClass().getSimpleName()
                            + (e.getMessage() == null ? "" : " " + e.getMessage()));
                }
            }
        }
        public void exe(Obj o) { o.exe(this); }

        public Machine() {
            dict.put("if", new Obj() {
                public void exe(Machine m) {
                    final Code code = m.code();
                    final Obj c0 = code.pop();
                    final Obj c1 = code.pop();
                    if(m.pop(Boolean.class).equals(new Boolean(true))) {
                        code.push(c0);
                    } else {
                        code.push(c1);
                    }
                }
                public String toString() { return "_IF"; }
            });
            dict.put("quote", new Obj() {
                public void exe(Machine m) {
                    final Obj c0 = m.code.pop();
                    m.push(c0);
                }
                public String toString() { return "_QUOTE"; }
            });
            dict.put("drop", new Obj() {
                public void exe(Machine m) {
                    m.pop(Obj.class);
                }
                public String toString() { return "_DROP"; }
            });
            dict.put("swap", new Obj() {
                public void exe(Machine m) {
                    final Obj o0 = m.pop(Obj.class);
                    final Obj o1 = m.pop(Obj.class);
                    m.push(o0);
                    m.push(o1);
                }
                public String toString() { return "_SWAP"; }
            });
            dict.put("exe", new Obj() {
                public void exe(Machine m) {
                    m.pop(Obj.class).exe(m);
                }
                public String toString() { return "_EXE"; }
            });
            dict.put("save", new Obj() {
                public void exe(Machine m) {
                    final Obj value = m.pop(Obj.class);
                    final Word variable = m.pop(Word.class);
                    m.dict.put(variable.value, value);
                }
                public String toString() { return "_SAVE"; }
            });
            dict.put("d", new Obj() {
                public void exe(Machine m) {
                    m.stack.push(new Str(dict.toString()));
                }
                public String toString() { return "_D"; }
            });
        }

    }
    public static void main(String[] args) {
        final Machine m = new Machine();

        final InputStream pis = new InputStreamEnterCallback(System.in, new Runnable() {
            public void run() {
                System.out.print(m.stack + " ");
            }
        });

        m.code(pis);

        final Code code = m.code();

    }

    public static class Reader {

        public static boolean isWs(int c) {
            switch(c) {
                case ' ': case '\t':
                case '\r': case '\n': return true;
                default: return false;
            }
        }
        public static boolean isDelimiter(int c) {
            if(isWs(c)) return true;
            switch(c) {
                case '(': case ')': case '[': case ']': return true;
                default: return false;
            }
        }
        // dummy token for ')'
        private static Obj CLOSE_PAREN = new Obj() {
            public void exe(Machine M) { throw new RuntimeException("wtf2");}
        };

        // read a list of instructions (  :a :b drop )
        public static Obj readCodeBlock(InputStream is) {
            final List<Obj> code = new ArrayList<Obj>();
            int levels = 0;
            while(true) {
                Obj o = read(is);
                if(o == null) break;
                if(o == CLOSE_PAREN) {
                    levels--;
                    if(levels < 0) break;
                }
                code.add(o);
            }
            return new CodeBlock(code);
        }

        public static Obj read(InputStream is) {
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

}

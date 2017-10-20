package com.adellica.thumbkeyboard;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.list;


public class ThumbJoy {
    public interface Applicable {
        // m mutable (environment)
        // stk immutable (return result)
        // code mutable (pop next token)
        IPair exe(Machine m, IPair stk, Stack code);
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
        public IPair exe(Machine m, IPair stk, Stack code) {
            //MutablePair recode = new MutablePair(this);

            return m.run(this, stk);
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
                public IPair exe(Machine m, IPair stk, Stack code) {return this;}
        };

        public static Pair cons(Object car, IPair cdr) { return new Pair(car, cdr); }
        public static Pair list(Object... args) {
            Pair p = nil;
            for(int i = args.length - 1; i >= 0 ; i--) {
                p = cons(args[i], p);
            }
            return p;
        }
    }
    public static class Stack {
        IPair p;
        public Stack(IPair p) {
            this.p = p;
        }
        public boolean isEmpty() {
            return p == Pair.nil;
        }
        public Object pop() {
            Object o = p.car();
            p = p.cdr();
            return o;
        }
    }

    abstract public static class Datum<T> {
        final T value;
        public Datum(T value) { this.value = value; }
        // public boolean equals(Object o) {
        //     if(o == null) return false;
        //     if(!(this.getClass().equals(o.getClass()))) return false;
        //     return value.equals(((Datum)o).value);
        // }
    }
    public static class Keyword extends Datum<String> {
        public Keyword(String s) { super(s); }
        public String toString() { return ":" + value; }
    }
    public static class Word extends Datum<String> implements Applicable {
        public Word(String s) { super(s);}
        public String toString() { return value; }
        public IPair exe(Machine m, IPair stk, Stack code) {
            Object o = m.dict.get(this.value);
            if(o == null) throw new InvalidReference(this.toString());
            return m.eval(o, stk, code);
        }
    }

    public abstract static class ApplicableCore implements Applicable {
        final String name;
        protected ApplicableCore(String name, Machine m) {
            this.name = name;
            m.dict.put(name, this);
        }
        public String toString() { return "_" + name; }
    }
    
    public static class Machine {
        //public IPair stk = Pair.nil;
        public Map<Object, Object> dict = new HashMap<Object, Object>();
        public Map<String, Object> macros = new HashMap<String, Object>();

        @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> t) {
            final Object ref = dict.get(key);
            if(t.isInstance(ref)) return (T)ref;
            throw new TypeMismatch(t, ref);
        }
        public Machine() {
            new ApplicableCore("drop", this) {
                    public IPair exe(Machine m, IPair stk, Stack code) {
                        return stk.cdr();
                    }
                };
            new ApplicableCore("dup", this) {
                    public IPair exe(Machine m, IPair stk, Stack code) {
                        return cons(stk.car(), stk);
                    }
                };
            new ApplicableCore("dd", this) {
                public IPair exe(Machine m, IPair stk, Stack code) {
                    Object o = dict.get("println");
                    if(o == null) throw new InvalidReference("println");
                    Machine.this.eval(o, list(dict), code);
                    return stk;
                }
            };
            new ApplicableCore("i", this) {
                public IPair exe(Machine m, IPair stk, Stack code) {
                    return m.eval(stk.car(), stk.cdr(), code);
                }
            };
            new ApplicableCore("'", this) {
                public IPair exe(Machine m, IPair stk, Stack code) {
                    if(code.isEmpty())
                        throw new RuntimeException("QUOTE: unexpected eof");

                    //System.out.println("QUOTing " + m.next(Object.class));
                    return cons(code.pop(), stk);
                }
            };
            new ApplicableCore("set", this) {
                @Override
                public IPair exe(Machine m, IPair stk, Stack code) {
                    final Word name = stk.cdr().car(Word.class);
                    final Object content = stk.car();
                    dict.put(name.value, content);
                    return stk.cdr().cdr();
                }
            };
            new ApplicableCore("get", this) {
                @Override
                public IPair exe(Machine m, IPair stk, Stack code) {
                    final Keyword name = stk.car(Keyword.class);
                    return cons(dict.get(name.value), stk.cdr());
                }
            };
            new ApplicableCore("println", this) {
                @Override
                public IPair exe(Machine m, IPair stk, Stack code) {
                    OutputStream os = m.get("out", OutputStream.class);
                    try {
                        os.write((stk.car().toString() + "\n").getBytes());
                        os.flush();
                    } catch (final IOException e) {
                        throw new TFE() {
                            public String getMessage() { return "io error " + e; }
                        };
                    }
                    return stk.cdr();
                }
            };
            new ApplicableCore("ifte", this) {
                @Override
                public IPair exe(Machine m, IPair stk, Stack code) {
                    IPair p = stk;
                    final Object e = p.car(); p = p.cdr();
                    final Object t = p.car(); p = p.cdr();
                    final Object i = p.car(); p = p.cdr();
                    if(isTrue(i)) return m.eval(t, p, code);
                    else return m.eval(e, p, code);
                }
            };
        }

        public boolean isTrue(Object o) {
            if(o instanceof Keyword) {
                Keyword k = (Keyword)o;
                if("t".equals(k.value)) return true;
            }
            return false;
        }

        public IPair eval(final Object o, IPair stk, Stack mp) {
            if (o instanceof Applicable) return ((Applicable) o).exe(this, stk, mp);
            return cons(o, stk); // defaults to "self evaluation"
        }
        public IPair run(IPair _source, IPair stk) {
            Stack mp = new Stack(_source);
            while(!mp.isEmpty()) {
                try {
                    final Object read = mp.pop();
                    stk = eval(read, stk, mp);
                } catch (ThumbJoy.TFE e) {
                    //e.printStackTrace();
                    System.out.println("error: " + e);
                }
            }
            return stk;
        }
    }
}

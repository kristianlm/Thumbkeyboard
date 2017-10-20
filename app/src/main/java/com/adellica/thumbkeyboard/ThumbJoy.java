package com.adellica.thumbkeyboard;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.list;
import static com.adellica.thumbkeyboard.ThumbReader.QUOTE;

public class ThumbJoy {
    public interface Env {
        Object get(Object key);
    }
    public interface Applicable {
        IPair exe(IPair stk, Machine m);
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
    public static class Pair implements IPair, Applicable {
        private final Object __car;
        private final IPair __cdr;
        public Pair(Object car, IPair cdr) { __car=car; __cdr=cdr;}
        public Object car() { return __car; }
        public IPair cdr() { return __cdr; }

        @SuppressWarnings("unchecked")
        public <T> T car(Class<T> t) {
            if(t.isInstance(__car)) return (T)__car;
            throw new TypeMismatch(t, __car);
        }
            
        public static final Pair nil = new Pair(null, null) {
                public <T> T car(Class<T> t) { throw new EmptyStackPopped(); }
                public Object car() { throw new EmptyStackPopped(); }
                public Pair cdr() { throw new EmptyStackPopped(); }
                public IPair exe(IPair stk, Machine m) {return this;}
        };

        public static Pair cons(Object car, IPair cdr) { return new Pair(car, cdr); }
        public static Pair list(Object... args) {
            Pair p = nil;
            for(int i = args.length - 1; i >= 0 ; i--) {
                p = cons(args[i], p);
            }
            return p;
        }
        public String toString() {
            if(this == nil) return "[ ]";
            String f = "[";
            IPair p = this;
            while(p != nil) {
                f += " " + p.car();
                p = p.cdr();
            }
            return f + " ]";
        }

        @Override
        public IPair exe(IPair stk, Machine m) {
            System.out.println("EXECUTING PAIR");
            IPair p = this;
            while (p != nil) {
                if(p.car() == QUOTE) {
                    System.out.println("quoting cdr " + p.cdr());
                    stk = cons(p.cdr().car(), stk);
                    System.out.println("quoting after  " + stk);
                    p = p.cdr().cdr();
                    continue;
                }
                stk = m.eval(p.car(), stk);
                p = p.cdr();
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
    public static class Keyword extends Datum<String> {
        public Keyword(String s) { super(s); }
        public String toString() { return ":" + value; }
    }
    public static class Word extends Datum<String> implements Applicable {
        public Word(String s) { super(s);}
        public String toString() { return value; }
        public IPair exe(IPair stk, Machine m) {
            Object o = m.dict.get(this.value);
            if(o == null) throw new InvalidReference(this.toString());
            return m.eval(o, stk);
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
        public Map<Object, Object> dict = new HashMap<Object, Object>();

        @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> t) {
            final Object ref = dict.get(key);
            if(t.isInstance(ref)) return (T)ref;
            throw new TypeMismatch(t, ref);
        }
        public Machine() {
            new ApplicableCore("conc", this) {
                    public IPair exe(IPair stk, Machine m) {
                        Object e0 = stk.car();
                        Object e1 = stk.cdr().car();
                        return cons(e0.toString() + e1, stk.cdr().cdr());
                    }
                };
            new ApplicableCore("drop", this) {
                    public IPair exe(IPair stk, Machine m) {
                        return stk.cdr();
                    }
                };
            new ApplicableCore("dup", this) {
                    public IPair exe(IPair stk, Machine m) {
                        return cons(stk.car(), stk);
                    }
                };
            new ApplicableCore("d", this) {
                public IPair exe(IPair stk, Machine m) {
                    return cons(dict, stk);
                }
            };
            new ApplicableCore("dd", this) {
                public IPair exe(IPair stk, Machine m) {
                    Object o = dict.get("println");
                    if(o == null) throw new InvalidReference("println");
                    Machine.this.eval(o, list(dict));
                    return stk;
                }
            };
            new ApplicableCore("i", this) {
                public IPair exe(IPair stk, Machine m) {
                    return m.eval(stk.car(), stk.cdr());
                }
            };
            new ApplicableCore("set", this) {
                @Override
                public IPair exe(IPair stk, Machine m) {
                    final Word name = stk.cdr().car(Word.class);
                    final Object content = stk.car();
                    dict.put(name.value, content);
                    return stk.cdr().cdr();
                }
            };
            new ApplicableCore("get", this) {
                @Override
                public IPair exe(IPair stk, Machine m) {
                    final Keyword name = stk.car(Keyword.class);
                    return cons(dict.get(name.value), stk.cdr());
                }
            };
            new ApplicableCore("println", this) {
                @Override
                public IPair exe(IPair stk, Machine m) {
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
        }
        public IPair eval(Object o, IPair stk) {
            if (o instanceof Applicable) return ((Applicable) o).exe(stk, this);
            return cons(o, stk); // defaults to "self evaluation"
        }

    }
}

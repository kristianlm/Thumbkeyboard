package com.adellica.thumbkeyboard;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.list;


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
    }

    abstract public static class Datum<T> {
        final T value;
        public Datum(T value) { this.value = value; }
        abstract public String toString();
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

    public abstract static class ApplicableCore implements Applicable {
        final String name;
        protected ApplicableCore(String name, Map<String, Object> dict) {
            this.name = name;
            dict.put(name, this);
        }
        public String toString() { return "_" + name; }
    }
    
    public static class Machine {
        public final Map<String, Object> dict;
        public final IPair stk;
        public final IPair code;

        @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> t) {
            final Object ref = dict.get(key);
            if(t.isInstance(ref)) return (T)ref;
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

        public static boolean isTrue(Object o) {
            if(o instanceof Keyword) {
                Keyword k = (Keyword)o;
                if("t".equals(k.value)) return true;
            }
            return false;
        }

        public static Map<String, Object> dictDefault() {
            return dictMath(dictStack(new HashMap<String, Object>()));
        }

        private static Map<String, Object> dictMath(Map<String, Object> dict) {
            new ApplicableCore("+", dict) {
                public Machine exe(Machine m) {
                    IPair p = m.stk;
                    final Long n = p.car(Long.class); p = p.cdr();
                    final Long d = p.car(Long.class); p = p.cdr();
                    return M(cons(new Long(n.longValue() + d.longValue()), p), m);
                }
            };
            new ApplicableCore("-", dict) {
                public Machine exe(Machine m) {
                    IPair p = m.stk;
                    final Long n = p.car(Long.class); p = p.cdr();
                    final Long d = p.car(Long.class); p = p.cdr();
                    return M(cons(new Long(d.longValue() - n.longValue()), p), m);
                }
            };
            new ApplicableCore("*", dict) {
                public Machine exe(Machine m) {
                    IPair p = m.stk;
                    final Long n = p.car(Long.class); p = p.cdr();
                    final Long d = p.car(Long.class); p = p.cdr();
                    return M(cons(new Long(d.longValue() * n.longValue()), p), m);
                }
            };
            new ApplicableCore("/", dict) {
                public Machine exe(Machine m) {
                    IPair p = m.stk;
                    final Long n = p.car(Long.class); p = p.cdr();
                    final Long d = p.car(Long.class); p = p.cdr();
                    return M(cons(new Long(d.longValue() / n.longValue()), p), m);
                }
            };
            new ApplicableCore("%", dict) {
                public Machine exe(Machine m) {
                    IPair p = m.stk;
                    final Long n = p.car(Long.class); p = p.cdr();
                    final Long d = p.car(Long.class); p = p.cdr();
                    return M(cons(new Long(d.longValue() % n.longValue()), p), m);
                }
            };
            return dict;
        }

        public static Map<String, Object> dictStack(Map<String, Object> dict) {

            new ApplicableCore("drop", dict) {
                public Machine exe(Machine m) {
                    return M(m.stk.cdr(), m);
                }
            };
            new ApplicableCore("dup", dict) {
                public Machine exe(Machine m) {
                    return M(cons(m.stk.car(), m.stk), m);
                }
            };
            new ApplicableCore("swap", dict) {
                public Machine exe(Machine m) {
                    IPair p = m.stk;
                    Object e0 = p.car(); p = p.cdr();
                    Object e1 = p.car(); p = p.cdr();
                    return M(cons(e1, cons(e0, p)), m);
                }
            };
            new ApplicableCore("dd", dict) {
                public Machine exe(Machine m) {
                    Object o = m.dict.get("println");
                    if(o == null) throw new InvalidReference("println");
                    M(list(m.dict), m).eval(o);
                    return m;
                }
            };
            new ApplicableCore("i", dict) {
                public Machine exe(Machine m) {
                    Machine mm = M(m.stk.cdr(), m);
                    return mm.eval(m.stk.car());
                }
            };
            new ApplicableCore("'", dict) {
                public Machine exe(Machine m) {
                    if(m.code == Pair.nil) throw new RuntimeException("QUOTE: unexpected eof");
                    return M(cons(m.code.car(), m.stk), m.code.cdr(), m);
                }
            };
            new ApplicableCore("type", dict) {
                public Machine exe(Machine m) {
                    return M(cons(m.stk.car().getClass(), m.stk.cdr()), m);
                }
            };
            new ApplicableCore("set", dict) {
                @Override
                public Machine exe(Machine m) {
                    final Word name = m.stk.cdr().car(Word.class);
                    final Object content = m.stk.car();
                    m.dict.put(name.value, content);
                    return M(m.stk.cdr().cdr(), m);
                }
            };
            new ApplicableCore("println", dict) {
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
            new ApplicableCore("ifte", dict) {
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
            return dict;
        }


        public Machine eval(final Object o) {
            if (o instanceof Applicable) return ((Applicable) o).exe(this);
            return M(cons(o, stk), this); // defaults to "self evaluation"
        }
    }
}

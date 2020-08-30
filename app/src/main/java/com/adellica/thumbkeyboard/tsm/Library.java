package com.adellica.thumbkeyboard.tsm;

import com.adellica.thumbkeyboard.tsm.Machine.Applicable;
import com.adellica.thumbkeyboard.tsm.Machine.Str;
import com.adellica.thumbkeyboard.tsm.Machine.TFE;
import com.adellica.thumbkeyboard.tsm.Machine.Word;
import com.adellica.thumbkeyboard.tsm.stack.IPair;
import com.adellica.thumbkeyboard.tsm.stack.Stack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import static com.adellica.thumbkeyboard.tsm.stack.Pair.cons;
import static com.adellica.thumbkeyboard.tsm.stack.Pair.nil;
import static com.adellica.thumbkeyboard.tsm.stack.Pair.reverse;


/**
 * a JoyLibrary is just a class where
 * all fields are static and of type {@link NamedApplicable}
 */

@SuppressWarnings("unused")
public class Library {

    public static void init(Library library) {
        for (Field field : library.getClass().getFields()) {
            try {
                final Object o = field.get(library);
                if (!(o instanceof NamedApplicable)) continue;
                NamedApplicable na = ((NamedApplicable) o);
                if (na.name == null) na.name = field.getName();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void fillDict(Map<String, Object> dict) {
        init(this);
        for (Field field : this.getClass().getFields()) {
            try {
                String name = field.getName();
                Object o = field.get(this);
                if (o instanceof NamedApplicable) {
                    name = ((NamedApplicable) o).name;
                }
                dict.put(name, o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract static class NamedApplicable implements Applicable {
        public String name = null;

        public NamedApplicable() {
        }

        public NamedApplicable(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "\u001b[34m‹" + name + "›\u001b[0m";
        }
    }

    public static class Core extends Library {
        public static Applicable help = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.push("d for dictionary, ' to quote, [] to postpone evaluation, ; for comments\n example: [ 2 3 + ] e \n example: 'greet [ \"hello there!\" p ] set greet");
                m.eval(p);
            }
        };
        public static Applicable drop = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.pop();
            }
        };
        public static Applicable dup = new NamedApplicable() {
            public void exe(Machine m) {
                Object o = m.stk.pop();
                m.stk.push(o);
                m.stk.push(o);
            }
        };
        public static Applicable swap = new NamedApplicable() {
            public void exe(Machine m) {
                Object o1 = m.stk.pop();
                Object o2 = m.stk.pop();
                m.stk.push(o1);
                m.stk.push(o2);
            }
        };
        public static Applicable d = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.push(new TreeMap(m.dict));
                m.eval(p); // use internal p for print
            }
        };
        public static Applicable e = new NamedApplicable() {
            public void exe(Machine m) {
                m.eval(m.stk.pop());
            }
        };
        public static Applicable load = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                String filename = m.stk.pop(Str.class).value;
                File file = new File(filename);
                for (String sp : m.searchPaths) {
                    if (file.exists()) break;
                    file = new File(sp + filename);
                }

                try {
                    Reader reader = new Reader(new FileInputStream(file));
                    Object read;
                    while ((read = reader.read()) != null) {
                        m.eval(read);
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        };
        public static Applicable exit = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                System.exit(m.stk.pop(Integer.class));
            }
        };

        public static Applicable quote = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.push(new Machine.Quoted(m.stk.pop()));
            }
        };
        public static Applicable equals = new NamedApplicable("=") {
            public void exe(Machine m) {
                m.stk.push(m.stk.pop().equals(m.stk.pop()));
            }
        };
        public static Applicable type = new NamedApplicable() {
            public void exe(Machine m) {
                m.stk.push(m.stk.pop().getClass());
            }
        };
        public static Applicable get = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Word name = m.stk.pop(Word.class);
                final Object o = m.get(name.value, Object.class);
                m.stk.push(o);
            }
        };
        public static Applicable set = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Object content = m.stk.pop();
                final Word name = m.stk.pop(Word.class);
                m.dict.put(name.value, content);
            }
        };
        public static final Applicable p = new NamedApplicable() { // println
            @Override
            public void exe(Machine m) {
                OutputStream os = m.get("out", OutputStream.class);
                try {
                    os.write((m.stk.pop().toString() + "\n").getBytes());
                    os.flush();
                } catch (final IOException e) {
                    throw new TFE() {
                        public String getMessage() {
                            return "io error " + e;
                        }
                    };
                }
            }
        };
        public static Applicable not = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Boolean b = m.stk.pop(Boolean.class);
                m.stk.push(b.equals(Boolean.FALSE));
            }
        };
        public static Applicable ifte = new NamedApplicable("ifte") {
            @Override
            public void exe(Machine m) {
                final Object e = m.stk.pop();
                final Object t = m.stk.pop();
                final Boolean i = m.stk.pop(Boolean.class);
                Object which = e;
                if (i) which = t;
                m.eval(which);
            }
        };
        public static Applicable repeat = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                int i = m.stk.pop(Integer.class);
                final Object body = m.stk.pop();
                while (i > 0) {
                    i--;
                    m.eval(body);
                }
            }
        };
        public static Applicable map = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Object proc = m.stk.pop();
                IPair lst = m.stk.pop(IPair.class);
                Machine mm = new Machine();
                Stack result = new Stack(nil);
                while (lst != nil) {
                    mm.stk.push(lst.car());
                    mm.eval(proc);
                    result.push(mm.stk.pop());
                    lst = lst.cdr();
                }
                m.stk.push(result);
            }
        };
        public static Applicable cons = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Object o = m.stk.pop();
                final IPair lst = m.stk.pop(IPair.class);
                m.stk.push(cons(o, lst));
            }
        };
        public static Applicable reverse = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final IPair o = m.stk.pop(IPair.class);
                m.stk.push(reverse(o));
            }
        };

        public static Applicable ref = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Object key = m.stk.pop(Object.class);
                final Map d = m.stk.pop(Map.class);
                m.stk.push(d.get(key));
            }
        };
        public static Applicable assoc = new NamedApplicable() {
            @Override
            public void exe(Machine m) {
                final Object value = m.stk.pop(Object.class);
                final Object key = m.stk.pop(Object.class);
                final Map d = m.stk.pop(Map.class);
                m.stk.push(d.put(key, value));
            }
        };
    }

    public static class Math extends Library {

        public static final Applicable PLUS = new NamedApplicable("+") {
            public void exe(Machine m) {
                final Integer n = m.stk.pop(Integer.class);
                final Integer d = m.stk.pop(Integer.class);
                m.stk.push(n + d);
            }
        };
        public static final Applicable MINUS = new NamedApplicable("-") {
            public void exe(Machine m) {
                final Integer n = m.stk.pop(Integer.class);
                final Integer d = m.stk.pop(Integer.class);
                m.stk.push(n - d);
            }
        };
        public static final Applicable MULTIPLY = new NamedApplicable("*") {
            public void exe(Machine m) {
                final Integer n = m.stk.pop(Integer.class);
                final Integer d = m.stk.pop(Integer.class);
                m.stk.push(n * d);
            }
        };
        public static final Applicable DIVIDE = new NamedApplicable("/") {
            public void exe(Machine m) {
                final Integer n = m.stk.pop(Integer.class);
                final Integer d = m.stk.pop(Integer.class);
                m.stk.push(n / d);
            }
        };
        public static final Applicable MODULUS = new NamedApplicable("%") {
            public void exe(Machine m) {
                final Integer n = m.stk.pop(Integer.class);
                final Integer d = m.stk.pop(Integer.class);
                m.stk.push(n % d);
            }
        };
    }

    public static class Strings extends Library {
        public static final Applicable concat = new NamedApplicable() {
            public void exe(Machine m) {
                Str s0 = m.stk.pop(Str.class);
                Str s1 = m.stk.pop(Str.class);
                m.stk.push(new Str(s1.value + s0.value));
            }
        };
        public static final Applicable str_p = new NamedApplicable("str?") {
            public void exe(Machine m) {
                Object o = m.stk.pop();
                m.stk.push(o instanceof Str);
            }
        };
        public static final Applicable upcase = new NamedApplicable() {
            public void exe(Machine m) {
                Str s = m.stk.pop(Str.class);
                m.stk.push(new Str(s.value.toUpperCase()));
            }
        };
        public static final Applicable downcase = new NamedApplicable() {
            public void exe(Machine m) {
                Str s = m.stk.pop(Str.class);
                m.stk.push(new Str(s.value.toLowerCase()));
            }
        };
    }
}

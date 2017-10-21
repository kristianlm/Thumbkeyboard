package com.adellica.thumbkeyboard;


import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.List;

import com.adellica.thumbkeyboard.ThumbJoy.Applicable;
import com.adellica.thumbkeyboard.ThumbJoy.IPair;
import com.adellica.thumbkeyboard.ThumbJoy.Pair;
import com.adellica.thumbkeyboard.ThumbJoy.Keyword;
import com.adellica.thumbkeyboard.ThumbJoy.Word;
import com.adellica.thumbkeyboard.ThumbJoy.Machine;

import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;


public class ThumbReader {

    public static boolean isWs(int c) {
        switch(c) {
        case ' ': case '\t':
        case '\r': case '\n': return true;
        default: return false;
        }
    }
    // dummy token for ')'
    private static Object CLOSE_PAREN = new Object() {};

    public static class Quoted implements Applicable {
        final Object obj;
        public Quoted(Object obj) { this.obj = obj; }
        @Override public Machine exe(Machine m) { return M(cons(obj, m.stk), m); }
        @Override public String toString() { return "“" + obj.toString(); }
    }

    // public static class _EVIL implements Applicable {
    //     public IPair exe(IPair stk, Machine m) {
    //         throw new ThumbJoy.TFE(){public String getMessage() {return "you should never eval me";}};
    //     }
    //     public String toString() {return "“"; }
    // }
    // public static final _EVIL QUOTE = new _EVIL();

    final InputStream is;
    //final ReaderMachine m = new ReaderMachine();
    public ThumbReader(InputStream is) {
        this.is = is;
    }

    // public class ReaderMachine extends Machine {
    //     public ReaderMachine(IPair source) {
    //         super(source);
    //         //dict.clear();
    //         new ApplicableCore("read", this) {
    //             public IPair exe(IPair stk, Machine m) {
    //                 final Object r = read();
    //                 System.out.println("running read " + r);
    //                 return cons(r, stk);
    //             }
    //         };
    //     }
    // }

    public static class IPairReader extends ThumbJoy.APair {
        final ThumbReader tr;
        Object o = null;
        public IPairReader(ThumbReader tr) {
            this.tr = tr;
        }
        public Object car() {
            if(o == null) {
                o = tr.read();                
            }
            return o;
        }
        public IPair cdr() {
            return new IPairReader(tr);
        }
        @Override public String toString() { return "<"+car()+"...>"; }
        @Override
        public Machine exe(Machine m) { return m; }
    }

    public Pair reverse(IPair pair) {
        Pair result = Pair.nil;
        while(pair != Pair.nil) {
            result = cons(pair.car(), result);
            pair = pair.cdr();
        }
        return result;
    }

    public Object read() {
        int c;
        try {
            while((c = is.read()) >= 0) {
                if(isWs(c)) continue;
                switch(c) {
                case ':': return new Keyword(readUntilWS(""));
                case '?': return new Boolean("t".equals(readUntilWS("")));
                case ']': return CLOSE_PAREN;
                case '[': return readQuotedList();
                default:
                    final String name = readUntilWS(String.format("%c", c));
                    //final Object macro = m.dict.get(name);
                    //if("'".equals(name))
                    //  return QUOTE;
                    //else if("def".equals(name)) {
                        // final Object res = (new Quoted(reverse(m.eval(
                        //         list(
                        //                 QUOTE, QUOTE,
                        //                 new Word("read"),
                        //                 new Word("read"),
                        //                 QUOTE, new Word("set")//, new Keyword("test")
                        //         ),
                        //         Pair.nil))));
                        // System.out.println("def produced: " + res);
                        // return res;
                    //}
                    //if(macro == null)
                        return new Word(name);
                    //return
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // read a list of instructions [ :a :b drop ]
    // note that we don't have syntax for reading non-quoted lists! because that would be completely useless.
    public Quoted readQuotedList() {
        List<Object> lst = new ArrayList<Object>();
        while(true) {
            Object o = read();
            if(o == null) break;
            if(o == CLOSE_PAREN) break;
            lst.add(o);
        }
        Pair p = Pair.nil;
        for(int i = lst.size() - 1; i >= 0 ; i--) {
            p = cons(lst.get(i), p);
        }
        return new Quoted(p);
    }

    public String readUntilWS (String init) throws IOException {
        int c;
        while((c = is.read()) >= 0) {
            if(isWs(c)) break;
            init += String.format("%c", c);
        }
        return init;
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

    public static class MachineBox { public Machine m ; }
    public static void repl(Machine _m, InputStream is, OutputStream os) {

        final PrintStream ps = new PrintStream(os);
        final MachineBox box = new MachineBox();

        InputStream pis = new InputStreamEnterCallback(is, new Runnable() {
            public void run() {
                ps.print(box.m.stk + " ");
            }
        });

        ThumbReader reader = new ThumbReader(pis);
        IPair source = new IPairReader(reader);
        box.m = M(_m.stk, source, _m);

        // steal all threads' output for easy
        box.m.dict.put("out", os);

        while(box.m.code != Pair.nil) {
            try {
                final Object read = box.m.code.car(); // read
                box.m = M(box.m.stk, box.m.code.cdr(), box.m); // pop code
                box.m = box.m.eval(read); // eval
            } catch (ThumbJoy.TFE e) {
                System.out.println("error: " + e);
            } catch (Throwable e) {
                e.printStackTrace(ps);
            }
        }
    }

    public static void serve(final ThumbJoy.Machine m, int port) {
        try {
            final ServerSocket ss = new ServerSocket(port);
            final Socket s = ss.accept();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final PrintStream pw = new PrintStream(s.getOutputStream());
                        pw.println("Welcome to Kristians silly ThumbForth");

                        ThumbReader.repl(m, s.getInputStream(), pw);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String [] args) {
        final int port = 2345;
        System.out.println("\u001b[32mThumbReader\u001b[0m REPL on port " + port);
        final Machine m = M(Pair.nil, Pair.nil, Machine.dictDefault());

        new Thread(new Runnable() {
            @Override
            public void run() {
                serve(m, port);
            }
            }).start();
        repl(m, System.in, System.out);
    }
}

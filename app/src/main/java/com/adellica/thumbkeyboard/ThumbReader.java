package com.adellica.thumbkeyboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import com.adellica.thumbkeyboard.ThumbJoy.Str;
import com.adellica.thumbkeyboard.ThumbJoy.TFE;
import com.adellica.thumbkeyboard.ThumbJoy.Word;
import com.adellica.thumbkeyboard.ThumbJoy.Machine;

import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;


public class ThumbReader {

    public static class InvalidEscapeSequence extends TFE {public InvalidEscapeSequence(String s){super(s);}};

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

    public Object read() {
        int c;
        try {
            while((c = is.read()) >= 0) {
                if(isWs(c)) continue;
                switch(c) {
                case '"': return readString(-1);
                case ':': return Keypress.fromString(readUntilWS(-1));
                case '?': throw new ReservedToken(c);
                case '@': throw new ReservedToken(c);
                case ']': return CLOSE_PAREN;
                case '[': return readQuotedList();
                case '0':case'1':case'2':case'3':case'4':
                case '5':case'6':case'7':case'8':case'9':
                        final String num = readUntilWS(c);
                        try {
                            return new Long(num);
                        } catch (NumberFormatException e) {
                            return new Double(num);
                        }
                default:
                    final String name = readUntilWS(c);
                    if("true".equals(name)) return Boolean.TRUE;
                    if("false".equals(name)) return Boolean.FALSE;
                    return new Word(name);
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

    // stolen from https://stackoverflow.com/questions/220547/printable-char-in-java
    public static boolean isPrintableChar( int c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    public static String writeString(String s) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ByteArrayInputStream bai = new ByteArrayInputStream(s.getBytes());
        int c;
        try {
            while((c = bai.read()) >= 0) {
                switch(c) {
                    case 0x07: bao.write('\\'); c = 'a' ; break;
                    case 0x08: bao.write('\\'); c = 'b' ; break;
                    case 0x0C: bao.write('\\'); c = 'f' ; break;
                    case 0x0A: bao.write('\\'); c = 'n' ; break;
                    case 0x0D: bao.write('\\'); c = 'r' ; break;
                    case 0x09: bao.write('\\'); c = 't' ; break;
                    case 0x0B: bao.write('\\'); c = 'v' ; break;
                    case 0x5C: bao.write('\\'); c = '\\' ; break;
                    case 0x27: bao.write('\\'); c = '\''; break;
                    case 0x22: bao.write('\\'); c = '"' ; break;
                    case 0x3F: bao.write('\\'); c = '?' ; break;
                    default:
                }
                if(isPrintableChar(c)) {
                    bao.write(c);
                } else {
                    bao.write(String.format("\\x%02x", c).getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bao.toString();
    }

    public Str readString(int init) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        if(init >= 0) bao.write(init);
        int c;
        while((c = is.read()) >= 0) {
            if(c == '"') break;
            if(c == '\\') {
                switch (c = is.read()) { // list taken from https://en.wikipedia.org/wiki/Escape_sequences_in_C
                    case 'a' : c = 0x07; break;
                    case 'b' : c = 0x08; break;
                    case 'f' : c = 0x0C; break;
                    case 'n' : c = 0x0A; break;
                    case 'r' : c = 0x0D; break;
                    case 't' : c = 0x09; break;
                    case 'v' : c = 0x0B; break;
                    case '\\' : c = 0x5C; break;
                    case '\'' : c = 0x27; break;
                    case '"' : c = 0x22; break;
                    case '?' : c = 0x3F; break;
                    case 'x': throw new RuntimeException("TODO");
                    case 'u': throw new RuntimeException("TODO");
                    case 'U': throw new RuntimeException("TODO");
                    default:
                        throw new InvalidEscapeSequence(String.valueOf((char)c));
                }
            }
            bao.write(c);
        }
        return new Str(bao.toString());
    }

    public String readUntilWS (int init) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        if(init >= 0) bao.write(init);
        int c;
        while((c = is.read()) >= 0) {
            if(isWs(c)) break;
            bao.write(c);
        }
        return bao.toString();
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
                ps.print(Pair.reverse(box.m.stk) + " ");
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
            } catch (TFE e) {
                ps.println("error: " + e);
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

    private class ReservedToken extends TFE {
        public ReservedToken(int c) { super("reserved for future use " + String.valueOf((char)c));  }
    }
}

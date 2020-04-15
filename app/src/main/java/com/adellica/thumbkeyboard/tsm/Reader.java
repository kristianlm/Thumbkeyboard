package com.adellica.thumbkeyboard.tsm;

import com.adellica.thumbkeyboard.tsm.Machine.Str;
import com.adellica.thumbkeyboard.tsm.stack.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.adellica.thumbkeyboard.tsm.Machine.Quoted;
import static com.adellica.thumbkeyboard.tsm.Machine.TFE;
import static com.adellica.thumbkeyboard.tsm.Machine.Word;
import static com.adellica.thumbkeyboard.tsm.stack.Pair.cons;
import static com.adellica.thumbkeyboard.tsm.stack.Pair.reverse;

/**
 * Thumb Stack Machine reader
 *
 * takes an InputStream and gives you tokens (like Keypress, Boolean and Integer).
 */
public class Reader {

    public static class InvalidEscapeSequence extends TFE {public InvalidEscapeSequence(String s){super(s);}}

    private class ReservedToken extends TFE {
        public ReservedToken(int c) { super("reserved for future use " + (char) c);  }
    }


    // dummy token for ']'
    private static Object CLOSE_PAREN = new Object() {};
    public static int [] whitespaces = new int [] {' ', '\t', '\r', '\n'};

    final InputStream is;
    public Reader(InputStream is) {
        this.is = is;
    }

    /**\
     *
     * @return read object like Word("drop") or null for EOF.
     */
    public Object read() {
        int c;
        try {
            while((c = is.read()) >= 0) {
                if(inArray(whitespaces, c)) continue;
                switch(c) {
                    case '"': return readString(-1);
                    case ':': return Keypress.fromString(readUntilWS(-1));
                    case '\'': return new Quoted(read());
                    case '{':
                    case '(':
                    case '#':
                    case '?':
                    case '@': throw new ReservedToken(c);
                    case ']': return CLOSE_PAREN;
                    case '[': return readQuotedList();
                    case '0':case'1':case'2':case'3':case'4':
                    case '5':case'6':case'7':case'8':case'9':
                    case '-':
                        final String num = readUntilWS(c);
                        try {
                            return new Integer(num);
                        } catch (NumberFormatException e) {
                            return new Double(num);
                        }
                    case ';': readUntil(-1, new int[]{'\n'}); return read();
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
    // note that we don't have syntax for reading non-quoted lists! because that would be completely useless (I think).
    public Quoted readQuotedList() {
        List<Object> lst = new ArrayList<Object>();
        while(true) {
            Object o = read();
            if(o == null) throw new TFE("unexpected EOF while reading []");
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
        if (true) {
            return s;
        }
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
                    case 'x': throw new RuntimeException("\\x parsing TODO");
                    case 'u': throw new RuntimeException("\\u parsing TODO");
                    case 'U': throw new RuntimeException("\\U parsing TODO");
                    default:
                        throw new InvalidEscapeSequence(String.valueOf((char)c));
                }
            }
            bao.write(c);
        }
        return new Str(bao.toString());
    }

    private String readUntilWS(int init) throws IOException {
        return readUntil(init, whitespaces);
    }

    public String readUntil (int init, int [] breaks) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        if(init >= 0) bao.write(init);
        int c;
        while((c = is.read()) >= 0) {
            if(inArray(breaks, c)) break;
            bao.write(c);
        }
        return bao.toString();
    }

    private boolean inArray(int[] breaks, int c) {
        for(int i = 0 ; i < breaks.length ; i++)
            if (breaks[i] == c)
                return true;
        return false;
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

    public static void repl(final Machine m, InputStream is, OutputStream os) {

        final PrintStream ps = new PrintStream(os);

        InputStream pis = new InputStreamEnterCallback(is, new Runnable() {
            public void run() {
                ps.print("\u001b[35m[ \u001b[0m" + reverse(m.stk.peek()).toStringParenless() + "\u001b[35m ]\u001b[0m ");
            }
        });

        Reader reader = new Reader(pis);
        m.dict.put("out", os);

        while(true) {
            try {
                final Object read = reader.read();
                if(read == null) break;
                m.eval(read);
            } catch (TFE e) {
                ps.println("error: " + e);
            } catch (Throwable e) {
                e.printStackTrace(ps);
            }
        }
    }

    public static void serve(final Machine m, int port) {
        try {
            final ServerSocket ss = new ServerSocket(port);

            while(true) {
                final Socket s = ss.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final PrintStream pw = new PrintStream(s.getOutputStream());
                            pw.println("\u001b[32mThumb StackMachine\u001b[0m");

                            Reader.repl(m, s.getInputStream(), pw);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        final int port = 2345;
        System.out.println("\u001b[32mThumb StackMachine\u001b[0m (REPL on port " + port + ")");
        final Machine m = new Machine();

        Thread trepl = new Thread(new Runnable() {
            @Override
            public void run() {
                serve(m, port);
            }
        });
        trepl.start();

        repl(m, System.in, System.out);
        System.exit(0); // don't wait for repl
    }

}

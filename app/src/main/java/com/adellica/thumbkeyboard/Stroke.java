package com.adellica.thumbkeyboard;

import com.adellica.thumbkeyboard.tsm.Machine;
import com.adellica.thumbkeyboard.tsm.stack.IPair;
import com.adellica.thumbkeyboard.tsm.stack.Stack;

import java.util.Arrays;

/**
 * Created by klm on 12/28/16.
 */
class Stroke {
    public final int[] taps;
    public final int[] ups;
    public final int[] downs;
    public final int[] lefts;
    public final int[] rights;

    public Stroke(int len) {
        taps = new int[len];
        ups = new int[len];
        downs = new int[len];
        lefts = new int[len];
        rights = new int[len];
    }

    public Stroke clone() {
        Stroke new_stroke = new Stroke(taps.length);
        for (int i = 0; i < taps.length; i++) {
            new_stroke.taps[i] = taps[i];
            new_stroke.ups[i] = ups[i];
            new_stroke.downs[i] = downs[i];
            new_stroke.lefts[i] = lefts[i];
            new_stroke.rights[i] = rights[i];
        }
        return new_stroke;
    }

    public static int count(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private static void assertBar(Stack s) {
        if (!"|".equals(popw(s)))
            throw new Machine.TFE("missing expected | in stroke");
    }

    public static Stroke fromPair(final IPair input) {
        final Stroke t = new Stroke(12);
        final Stack s = new Stack(input);
        t.per(popw(s), 0);
        t.per(popw(s), 1);
        assertBar(s);
        t.per(popw(s), 2);
        t.per(popw(s), 3);
        t.per(popw(s), 4);
        t.per(popw(s), 5);
        assertBar(s);
        t.per(popw(s), 6);
        t.per(popw(s), 7);
        t.per(popw(s), 8);
        t.per(popw(s), 9);
        assertBar(s);
        t.per(popw(s), 10);
        t.per(popw(s), 11);

        return t;
    }

    private String align(int size, String content) {
        int padding = Math.max(size - content.length(), 0);
        return repeat(" ", padding) + content;
    }

    // read a line like 00000-00000:00000-00000 00000-00000:00000-00000 00100-00000:00000-00000 key DPAD_UP
    // and return it's stroke part and its token-part. This is tightly connected to Stroke.toString implementation.
    public static String parse(final String line) {
        if (line.length() == 72) {
            String stroke = line.substring(0, 72);
            return stroke;
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < taps.length; i++) {
            taps[i] = 0;
            ups[i] = 0;
            downs[i] = 0;
            lefts[i] = 0;
            rights[i] = 0;
        }
    }

    private void per(String token, int i) {
        taps[i] = count(token, 'x');
        ups[i] = count(token, '^');
        downs[i] = count(token, 'v');
        rights[i] = count(token, '>');
        lefts[i] = count(token, '<');
    }

    private static String popw(Stack s) {
        return s.pop(Machine.Word.class).value;
    }

    private String repeat(String r, int count) {
        String result = "";
        for (int i = 0; i < count; i++)
            result += r;
        return result;
    }

    private String rep(int index) {
        String result = "";
        result += repeat("x", taps[index]);
        result += repeat("v", downs[index]);
        result += repeat("^", ups[index]);
        result += repeat("<", lefts[index]);
        result += repeat(">", rights[index]);
        if ("".equals(result)) result = ".";
        return result;
    }

    @Override
    public String toString() {
        int x = 1;
        for (int i = 0; i < 12; i++) if (rep(i).length() > x) x = rep(i).length();
        x++; // ensure we have at least one space in there
        String s = "\n";
        s += "#S[" + align(x, rep(0)) + align(x, rep(1)) + " |" + align(x, rep(2)) + align(x, rep(3)) + "\n";
        s += "   " + align(x, rep(4)) + align(x, rep(5)) + " |" + align(x, rep(6)) + align(x, rep(7)) + "\n";
        s += "   " + align(x, rep(8)) + align(x, rep(9)) + " |" + align(x, rep(10)) + align(x, rep(11)) + " ]";
        return s;
    }

    public void copyFrom(Stroke stroke) {
        if (stroke.taps.length != taps.length)
            throw new RuntimeException("stroke size mismatch");
        for (int i = 0; i < taps.length; i++) {
            taps[i] = stroke.taps[i];
            lefts[i] = stroke.lefts[i];
            ups[i] = stroke.ups[i];
            downs[i] = stroke.downs[i];
            rights[i] = stroke.rights[i];
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ups) + Arrays.hashCode(downs) +
                Arrays.hashCode(lefts) + Arrays.hashCode(rights) +
                Arrays.hashCode(taps);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Stroke)) return false;
        Stroke s = (Stroke) o;
        return Arrays.equals(ups, s.ups) && Arrays.equals(downs, s.downs) &&
                Arrays.equals(lefts, s.lefts) && Arrays.equals(rights, s.rights) &&
                Arrays.equals(taps, s.taps);
    }
}

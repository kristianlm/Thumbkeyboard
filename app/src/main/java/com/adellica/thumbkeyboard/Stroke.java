package com.adellica.thumbkeyboard;

/**
* Created by klm on 12/28/16.
*/
class Stroke {
    public int taps[];
    public int ups[];
    public int downs[];
    public int lefts[];
    public int rights[];

    public Stroke(int len) {
        taps = new int[len];
        ups = new int[len];
        downs = new int[len];
        lefts = new int[len];
        rights = new int[len];

    }
    public void clear() {
        for(int i = 0 ; i < taps.length ; i++) {
            taps[i] = 0;
            ups[i] = 0;
            downs[i] = 0;
            lefts[i] = 0;
            rights[i] = 0;
        }
    }

    @Override
    public String toString() {
        String s = "";
        for(int j = 0 ; j < taps.length ; j += 4) {
            s += ""
                + taps[j + 0] + lefts[j+0] + ups[j+0] + downs[j+0] + rights[j+0] + "-"
                + taps[j + 1] + lefts[j+1] + ups[j+1] + downs[j+1] + rights[j+1]
                + ":"
                + taps[j + 2] + lefts[j+2] + ups[j+2] + downs[j+2] + rights[j+2] + "-"
                + taps[j + 3] + lefts[j+3] + ups[j+3] + downs[j+3] + rights[j+3]
                + " ";
        }
        return s;
    }
    // read a line like 00000-00000:00000-00000 00000-00000:00000-00000 00100-00000:00000-00000 key DPAD_UP
    // and return it's stroke part and its token-part. This is tightly connected to Stroke.toString implementation.
    public static String parse(final String line) {
        if(line.length() == 72) {
            String stroke = line.substring(0, 72);
            String token = line.substring(72);
            return stroke;
        }
        return null;//throw new RuntimeException("invalid stroke string length " + line);
    }

    public void copyFrom(Stroke stroke) {
        if(stroke.taps.length != taps.length)
            throw new RuntimeException("stroke size mismatch");
        for(int i = 0 ; i < taps.length ; i++) {
            taps[i] = stroke.taps[i];
            lefts[i] = stroke.lefts[i];
            ups[i] = stroke.ups[i];
            downs[i] = stroke.downs[i];
            rights[i] = stroke.rights[i];
        }
    }
}

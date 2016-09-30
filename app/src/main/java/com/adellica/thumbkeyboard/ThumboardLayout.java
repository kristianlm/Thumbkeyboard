// generated from gen-ThumboardLayout.java.scm and layout.scm
package com.adellica.thumbkeyboard;
import java.util.Arrays;
public class ThumboardLayout {
  private static boolean match(String [] p0, String [] p1) { return Arrays.equals(p0, p1); }
  /** returns string-representation of key as defined by KeyEvent KEYCODE_'s.
  */
  public static String parse(String [] p) {
    if(false) {}

    else if(match(p, new String[]{
          "... xx."
    }))                                return "TOGGLE HELP";
    // unused ("x.. ...")
    else if(match(p, new String[]{
          ".x. ..."
    }))                                return "E";
    else if(match(p, new String[]{
          "..x ..."
    }))                                return "A";
    else if(match(p, new String[]{
          "... ..x"
    }))                                return "REPEAT";
    else if(match(p, new String[]{
          "... .x."
    }))                                return "SPACE";
    else if(match(p, new String[]{
          "... x.."
    }))                                return "T";
    else if(match(p, new String[]{
          "..x x.."
    }))                                return "O";
    else if(match(p, new String[]{
          ".x. .x."
    }))                                return "I";
    else if(match(p, new String[]{
          ".x. ..x"
    }))                                return "N";
    else if(match(p, new String[]{
          "..x .x."
    }))                                return "S";
    else if(match(p, new String[]{
          ".x. x.."
    }))                                return "H";
    else if(match(p, new String[]{
          "x.. ..x"
    }))                                return "R";
    else if(match(p, new String[]{
          "..x ..x"
    }))                                return "D";
    else if(match(p, new String[]{
          "x.. x.."
    }))                                return "L";
    else if(match(p, new String[]{
          "x.. .x."
    }))                                return "C";
    else if(match(p, new String[]{
          "..x ...",
          "..x .x."
    }))                                return "U";
    else if(match(p, new String[]{
          ".x. ...",
          ".x. .x."
    }))                                return "M";
    else if(match(p, new String[]{
          "x.. ...",
          "x.. .x."
    }))                                return "K";
    else if(match(p, new String[]{
          "... ..x",
          ".x. ..x"
    }))                                return "W";
    else if(match(p, new String[]{
          "... x..",
          ".x. x.."
    }))                                return "F";
    else if(match(p, new String[]{
          "... .x.",
          ".x. .x."
    }))                                return "J";
    else if(match(p, new String[]{
          "... .x.",
          "x.. .x."
    }))                                return "P";
    else if(match(p, new String[]{
          "... .x.",
          "..x .x."
    }))                                return "G";
    else if(match(p, new String[]{
          ".x. ...",
          ".x. x.."
    }))                                return "Y";
    else if(match(p, new String[]{
          ".x. ...",
          ".x. ..x"
    }))                                return "V";
    else if(match(p, new String[]{
          "x.. ...",
          "x.. ..x"
    }))                                return "B";
    else if(match(p, new String[]{
          "... ..x",
          "x.. ..x"
    }))                                return "Q";
    else if(match(p, new String[]{
          "... x..",
          "..x x.."
    }))                                return "X";
    else if(match(p, new String[]{
          "..x ...",
          "..x x.."
    }))                                return "Z";
    else if(match(p, new String[]{
          "... .x.",
          ".x. .x.",
          "... .x."
    }))                                return "DEL";
    else if(match(p, new String[]{
          "... .x.",
          "..x .x.",
          "... .x."
    }))                                return "ENTER";
    return null;
  }

  public static String help() {
    return " TOGGLE HELP ... xx.\n           E .x. ...\n           A ..x ...\n      REPEAT ... ..x\n       SPACE ... .x.\n           T ... x..\n           O ..x x..\n           I .x. .x.\n           N .x. ..x\n           S ..x .x.\n           H .x. x..\n           R x.. ..x\n           D ..x ..x\n           L x.. x..\n           C x.. .x.\n           U ..x ...\n             ..x .x.\n           M .x. ...\n             .x. .x.\n           K x.. ...\n             x.. .x.\n           W ... ..x\n             .x. ..x\n           F ... x..\n             .x. x..\n           J ... .x.\n             .x. .x.\n           P ... .x.\n             x.. .x.\n           G ... .x.\n             ..x .x.\n           Y .x. ...\n             .x. x..\n           V .x. ...\n             .x. ..x\n           B x.. ...\n             x.. ..x\n           Q ... ..x\n             x.. ..x\n           X ... x..\n             ..x x..\n           Z ..x ...\n             ..x x..\n         DEL ... .x.\n             .x. .x.\n             ... .x.\n       ENTER ... .x.\n             ..x .x.\n             ... .x.\n     DPAD_UP #f\n   DPAD_DOWN #f\n   DPAD_LEFT #f\n  DPAD_RIGHT #f\n";
  }
}

// generated from gen-ThumboardLayout.java.scm and layout.scm
package com.adellica.thumbkeyboard;
import java.util.Arrays;
import android.view.KeyEvent;
public class ThumboardLayout {
  private static boolean match(String [] p0, String [] p1) { return Arrays.equals(p0, p1); }
  /** returns string-representation of key as defined by KeyEvent KEYCODE_'s.
  */
  public static String parse(String [] p) {
    if(false) {}

    else if(match(p, new String[]{
          "..xx"
    }))                                return "TOGGLE HELP";
    else if(match(p, new String[]{
          "..x."
    }))                                return "SPACE";
    else if(match(p, new String[]{
          "...x"
    }))                                return "REPEAT";
    // unused ("x...")
    else if(match(p, new String[]{
          ".x.."
    }))                                return "E";
    // unused ("x..x")
    else if(match(p, new String[]{
          "x.x."
    }))                                return "A";
    else if(match(p, new String[]{
          ".x.x"
    }))                                return "T";
    else if(match(p, new String[]{
          ".xx."
    }))                                return "O";
    else if(match(p, new String[]{
          "x...",
          "x.x."
    }))                                return "I";
    else if(match(p, new String[]{
          "x...",
          "x..x"
    }))                                return "D";
    else if(match(p, new String[]{
          ".x..",
          ".xx."
    }))                                return "N";
    else if(match(p, new String[]{
          ".x..",
          ".x.x"
    }))                                return "H";
    else if(match(p, new String[]{
          "..x.",
          "x.x."
    }))                                return "S";
    else if(match(p, new String[]{
          "..x.",
          ".xx."
    }))                                return "L";
    else if(match(p, new String[]{
          "...x",
          "x..x"
    }))                                return "F";
    else if(match(p, new String[]{
          "...x",
          ".x.x"
    }))                                return "G";
    else if(match(p, new String[]{
          "x.x.",
          "..x."
    }))                                return "R";
    else if(match(p, new String[]{
          "x..x",
          "...x"
    }))                                return "M";
    else if(match(p, new String[]{
          ".xx.",
          "..x."
    }))                                return "K";
    else if(match(p, new String[]{
          ".x.x",
          "...x"
    }))                                return "B";
    else if(match(p, new String[]{
          "x.x.",
          "x..."
    }))                                return "C";
    else if(match(p, new String[]{
          "x..x",
          "x..."
    }))                                return "J";
    // unused (".xx." ".x..")
    else if(match(p, new String[]{
          ".x.x",
          ".x.."
    }))                                return "P";
    else if(match(p, new String[]{
          "x...",
          "x.x.",
          "..x."
    }))                                return "U";
    // unused ("x..." "x..x" "...x")
    else if(match(p, new String[]{
          ".x..",
          ".xx.",
          "..x."
    }))                                return "Y";
    else if(match(p, new String[]{
          ".x..",
          ".x.x",
          "...x"
    }))                                return "V";
    else if(match(p, new String[]{
          "..x.",
          "x.x.",
          "x..."
    }))                                return "W";
    else if(match(p, new String[]{
          "..x.",
          ".xx.",
          ".x.."
    }))                                return "Q";
    else if(match(p, new String[]{
          "...x",
          "x..x",
          "x..."
    }))                                return "X";
    else if(match(p, new String[]{
          "...x",
          ".x.x",
          ".x.."
    }))                                return "Z";
    else if(match(p, new String[]{
          "...x",
          "x..x",
          "...x"
    }))                                return "DEL";
    else if(match(p, new String[]{
          "xx.."
    }))                                return "DEL";
    else if(match(p, new String[]{
          "...x",
          ".x.x",
          "...x"
    }))                                return "ENTER";
    else if(match(p, new String[]{
          ".x..",
          ".x.x",
          ".x.."
    }))                                return "DPAD_UP";
    else if(match(p, new String[]{
          "x...",
          "x.x.",
          "x..."
    }))                                return "DPAD_DOWN";
    else if(match(p, new String[]{
          ".x..",
          ".xx.",
          ".x.."
    }))                                return "DPAD_LEFT";
    else if(match(p, new String[]{
          "x...",
          "x..x",
          "x..."
    }))                                return "DPAD_RIGHT";
    return null;
  }

  public static String help() {
    return " TOGGLE HELP ..xx\n       SPACE ..x.\n      REPEAT ...x\n           E .x..\n           A x.x.\n           T .x.x\n           O .xx.\n           I x...\n             x.x.\n           D x...\n             x..x\n           N .x..\n             .xx.\n           H .x..\n             .x.x\n           S ..x.\n             x.x.\n           L ..x.\n             .xx.\n           F ...x\n             x..x\n           G ...x\n             .x.x\n           R x.x.\n             ..x.\n           M x..x\n             ...x\n           K .xx.\n             ..x.\n           B .x.x\n             ...x\n           C x.x.\n             x...\n           J x..x\n             x...\n           P .x.x\n             .x..\n           U x...\n             x.x.\n             ..x.\n           Y .x..\n             .xx.\n             ..x.\n           V .x..\n             .x.x\n             ...x\n           W ..x.\n             x.x.\n             x...\n           Q ..x.\n             .xx.\n             .x..\n           X ...x\n             x..x\n             x...\n           Z ...x\n             .x.x\n             .x..\n         DEL ...x\n             x..x\n             ...x\n         DEL xx..\n       ENTER ...x\n             .x.x\n             ...x\n     DPAD_UP .x..\n             .x.x\n             .x..\n   DPAD_DOWN x...\n             x.x.\n             x...\n   DPAD_LEFT .x..\n             .xx.\n             .x..\n  DPAD_RIGHT x...\n             x..x\n             x...\n";
  }
}

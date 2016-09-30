// generated from gen-ThumboardLayout.java.scm and layout.scm
package com.adellica.thumbkeyboard;
public class ThumboardLayout {
  /** returns string-representation of key as defined by KeyEvent KEYCODE_'s.
  */
  public static String parse(String p) {
    if(false) {}

    else if(p.matches("\\.\\.\\. xx\\." )) return "TOGGLE HELP";
    // unused ("x.. ...")
    else if(p.matches("\\.x\\. \\.\\.\\." )) return "E";
    else if(p.matches("\\.\\.x \\.\\.\\." )) return "A";
    else if(p.matches("\\.\\.\\. \\.\\.x" )) return "REPEAT";
    else if(p.matches("\\.\\.\\. \\.x\\." )) return "SPACE";
    else if(p.matches("\\.\\.\\. x\\.\\." )) return "T";
    else if(p.matches("\\.\\.x x\\.\\." )) return "O";
    else if(p.matches("\\.x\\. \\.x\\." )) return "I";
    else if(p.matches("\\.x\\. \\.\\.x" )) return "N";
    else if(p.matches("\\.\\.x \\.x\\." )) return "S";
    else if(p.matches("\\.x\\. x\\.\\." )) return "H";
    else if(p.matches("x\\.\\. \\.\\.x" )) return "R";
    else if(p.matches("\\.\\.x \\.\\.x" )) return "D";
    else if(p.matches("x\\.\\. x\\.\\." )) return "L";
    else if(p.matches("x\\.\\. \\.x\\." )) return "C";
    else if(p.matches("\\.\\.x \\.\\.\\.\\n\\.\\.x \\.x\\." )) return "U";
    else if(p.matches("\\.x\\. \\.\\.\\.\\n\\.x\\. \\.x\\." )) return "M";
    else if(p.matches("x\\.\\. \\.\\.\\.\\nx\\.\\. \\.x\\." )) return "K";
    else if(p.matches("\\.\\.\\. \\.\\.x\\n\\.x\\. \\.\\.x" )) return "W";
    else if(p.matches("\\.\\.\\. x\\.\\.\\n\\.x\\. x\\.\\." )) return "F";
    else if(p.matches("\\.\\.\\. \\.x\\.\\n\\.x\\. \\.x\\." )) return "J";
    else if(p.matches("\\.\\.\\. \\.x\\.\\nx\\.\\. \\.x\\." )) return "P";
    else if(p.matches("\\.\\.\\. \\.x\\.\\n\\.\\.x \\.x\\." )) return "G";
    else if(p.matches("\\.x\\. \\.\\.\\.\\n\\.x\\. x\\.\\." )) return "Y";
    else if(p.matches("\\.x\\. \\.\\.\\.\\n\\.x\\. \\.\\.x" )) return "V";
    else if(p.matches("x\\.\\. \\.\\.\\.\\nx\\.\\. \\.\\.x" )) return "B";
    else if(p.matches("\\.\\.\\. \\.\\.x\\nx\\.\\. \\.\\.x" )) return "Q";
    else if(p.matches("\\.\\.\\. x\\.\\.\\n\\.\\.x x\\.\\." )) return "X";
    else if(p.matches("\\.\\.x \\.\\.\\.\\n\\.\\.x x\\.\\." )) return "Z";
    return null;
  }

  public static String help() {
    return " TOGGLE HELP ... xx.\n           E .x. ...\n           A ..x ...\n      REPEAT ... ..x\n       SPACE ... .x.\n           T ... x..\n           O ..x x..\n           I .x. .x.\n           N .x. ..x\n           S ..x .x.\n           H .x. x..\n           R x.. ..x\n           D ..x ..x\n           L x.. x..\n           C x.. .x.\n           U ..x ...\n             ..x .x.\n           M .x. ...\n             .x. .x.\n           K x.. ...\n             x.. .x.\n           W ... ..x\n             .x. ..x\n           F ... x..\n             .x. x..\n           J ... .x.\n             .x. .x.\n           P ... .x.\n             x.. .x.\n           G ... .x.\n             ..x .x.\n           Y .x. ...\n             .x. x..\n           V .x. ...\n             .x. ..x\n           B x.. ...\n             x.. ..x\n           Q ... ..x\n             x.. ..x\n           X ... x..\n             ..x x..\n           Z ..x ...\n             ..x x..\n     DPAD_UP #f\n   DPAD_DOWN #f\n   DPAD_LEFT #f\n  DPAD_RIGHT #f\n";
  }
}

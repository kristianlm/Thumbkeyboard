// generated from gen-ThumboardLayout.java.scm and layout.scm
package com.adellica.thumbkeyboard;
public class ThumboardLayout {
  /** returns string-representation of key as defined by KeyEvent KEYCODE_'s.
  */
  public static String parse(String p) {
    if(false) {}

    else if(p.matches("(?s)\\.\\.\\. xx\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "TOGGLE HELP";
    // unused ("x.. ...")
    else if(p.matches("(?s)\\.x\\. \\.\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "E";
    else if(p.matches("(?s)\\.\\.x \\.\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "A";
    else if(p.matches("(?s)\\.\\.\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "REPEAT";
    else if(p.matches("(?s)\\.\\.\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "SPACE";
    else if(p.matches("(?s)\\.\\.\\. x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "T";
    else if(p.matches("(?s)\\.\\.x x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "O";
    else if(p.matches("(?s)\\.x\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "I";
    else if(p.matches("(?s)\\.x\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "N";
    else if(p.matches("(?s)\\.\\.x \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "S";
    else if(p.matches("(?s)\\.x\\. x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "H";
    else if(p.matches("(?s)x\\.\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "R";
    else if(p.matches("(?s)\\.\\.x \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "D";
    else if(p.matches("(?s)x\\.\\. x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "L";
    else if(p.matches("(?s)x\\.\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "C";
    else if(p.matches("(?s)\\.\\.x \\.\\.\\.\\n\\.\\.x \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "U";
    else if(p.matches("(?s)\\.x\\. \\.\\.\\.\\n\\.x\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "M";
    else if(p.matches("(?s)x\\.\\. \\.\\.\\.\\nx\\.\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "K";
    else if(p.matches("(?s)\\.\\.\\. \\.\\.x\\n\\.x\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "W";
    else if(p.matches("(?s)\\.\\.\\. x\\.\\.\\n\\.x\\. x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "F";
    else if(p.matches("(?s)\\.\\.\\. \\.x\\.\\n\\.x\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "J";
    else if(p.matches("(?s)\\.\\.\\. \\.x\\.\\nx\\.\\. \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "P";
    else if(p.matches("(?s)\\.\\.\\. \\.x\\.\\n\\.\\.x \\.x\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "G";
    else if(p.matches("(?s)\\.x\\. \\.\\.\\.\\n\\.x\\. x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "Y";
    else if(p.matches("(?s)\\.x\\. \\.\\.\\.\\n\\.x\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "V";
    else if(p.matches("(?s)x\\.\\. \\.\\.\\.\\nx\\.\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "B";
    else if(p.matches("(?s)\\.\\.\\. \\.\\.x\\nx\\.\\. \\.\\.x\\n\\.\\.\\. \\.\\.\\.\\n" )) return "Q";
    else if(p.matches("(?s)\\.\\.\\. x\\.\\.\\n\\.\\.x x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "X";
    else if(p.matches("(?s)\\.\\.x \\.\\.\\.\\n\\.\\.x x\\.\\.\\n\\.\\.\\. \\.\\.\\.\\n" )) return "Z";
    else if(p.matches("(?s)(.)*\\.x\\. \\.x\\.\\n\\.\\.\\. \\.x\\.\\n" )) return "DEL";
    else if(p.matches("(?s)(.)*\\.\\.x \\.x\\.\\n\\.\\.\\. \\.x\\.\\n" )) return "ENTER";
    else if(p.matches("(?s)(.)*x\\.\\. \\.x\\.\\n\\.\\.\\. \\.x\\.\\n" )) return "TAB";
    else if(p.matches("(?s)(.)*\\.x\\. \\.\\.x\\n\\.x\\. \\.\\.\\.\\n" )) return "DPAD_UP";
    else if(p.matches("(?s)(.)*\\.x\\. \\.x\\.\\n\\.x\\. \\.\\.\\.\\n" )) return "DPAD_DOWN";
    else if(p.matches("(?s)(.)*x\\.\\. \\.x\\.\\nx\\.\\. \\.\\.\\.\\n" )) return "DPAD_LEFT";
    else if(p.matches("(?s)(.)*x\\.\\. \\.\\.x\\nx\\.\\. \\.\\.\\.\\n" )) return "DPAD_RIGHT";
    return null;
  }

  public static String help() {
    return " TOGGLE HELP ... xx.\n           E .x. ...\n           A ..x ...\n      REPEAT ... ..x\n       SPACE ... .x.\n           T ... x..\n           O ..x x..\n           I .x. .x.\n           N .x. ..x\n           S ..x .x.\n           H .x. x..\n           R x.. ..x\n           D ..x ..x\n           L x.. x..\n           C x.. .x.\n           U ..x ...\n             ..x .x.\n           M .x. ...\n             .x. .x.\n           K x.. ...\n             x.. .x.\n           W ... ..x\n             .x. ..x\n           F ... x..\n             .x. x..\n           J ... .x.\n             .x. .x.\n           P ... .x.\n             x.. .x.\n           G ... .x.\n             ..x .x.\n           Y .x. ...\n             .x. x..\n           V .x. ...\n             .x. ..x\n           B x.. ...\n             x.. ..x\n           Q ... ..x\n             x.. ..x\n           X ... x..\n             ..x x..\n           Z ..x ...\n             ..x x..\n         DEL (: (* any) .x. .x. ... .x.)\n       ENTER (: (* any) ..x .x. ... .x.)\n         TAB (: (* any) x.. .x. ... .x.)\n     DPAD_UP (: (* any) .x. ..x .x. ...)\n   DPAD_DOWN (: (* any) .x. .x. .x. ...)\n   DPAD_LEFT (: (* any) x.. .x. x.. ...)\n  DPAD_RIGHT (: (* any) x.. ..x x.. ...)\n";
  }
}

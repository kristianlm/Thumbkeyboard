// 06jul16abu
// (c) Software Lab. Alexander Burger

package com.adellica.thumbkeyboard;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import android.util.*;
import android.view.*;
import android.graphics.*;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;

public class TestThumbkeyboardView extends View {
   ThumbkeyboardIME Ime;
   Blob[] Blobs;
   MotionEvent Ev;
   int Chord, Shift, Punct, Digit, Cntrl, AltGr, Funct;
   long Beg, End;
   boolean Off, Rev;
   int Num, Repeat, Repeat2, Rpt, RptN;
   float OrgX, OrgY;
   Paint Text1 = new Paint();
   Paint Text2 = new Paint();
   Paint Circle1 = new Paint();
   Paint Circle2 = new Paint();
   Paint CircleFill = new Paint();
   static final int CANDIDATES = 32;
   String Dict[], Candidates[] = new String[CANDIDATES];
   float CandX[] = new float[CANDIDATES];
   float CandY;
   Paint Help;

   final static int Penti[] = new int[] {
      0, 'n', 'i', 'g', 'e', 0, 'o', 'm',
      's', 'j', 'c', 'v', 'l', 0, 'u', 'k',
      32, 'd', 'a', 'y', 'r', 0, 'b', 't',
      'f', 'h', 'q', 'x', 'z', 0, 'p', 'w'
   };
   final static int PentiPunct[] = new int[] {
      0, ')', '!', '=', '[', 0, '|', '>',
      '*', ';', ']', '(', '_', 0, '&', '@',
      32, '/', '`', '^', '$', 0, '{', '%',
      '?', '#', '\'', '\\', '"', 0, '}', '<'
   };
   final static int PentiDigit[] = new int[] {
      0, -KeyEvent.KEYCODE_DPAD_RIGHT, '3', '9', '2', 0, '8', '-',
      '1', ':', ',', -KeyEvent.KEYCODE_DPAD_DOWN, '7', 0, '0', 127,
      32, '.', '6', -KeyEvent.KEYCODE_DPAD_UP, '5', 0, -KeyEvent.KEYCODE_DPAD_LEFT, '~',
      '4', -KeyEvent.KEYCODE_MOVE_HOME, -KeyEvent.KEYCODE_PAGE_UP, -KeyEvent.KEYCODE_PAGE_DOWN, -KeyEvent.KEYCODE_MOVE_END, 0, '+', -KeyEvent.KEYCODE_INSERT
   };
   final static int PentiAltGr[] = new int[] {
      0, 'ñ', 'í', 0, 'é', 0, 'ö', 0,
      'ß', 0, 'ç', 0, 0, 0, 'ü', 0,
      32, 0, 'ä', 'ÿ', '€', 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0
   };

   final static int PentiFunct[] = new int[] {
      0, 0x10004E, -KeyEvent.KEYCODE_F3, -KeyEvent.KEYCODE_F9, -KeyEvent.KEYCODE_F2, 0, -KeyEvent.KEYCODE_F8, -KeyEvent.KEYCODE_F12,
      -KeyEvent.KEYCODE_F1, 0, 0x100043, 0, -KeyEvent.KEYCODE_F7, 0, -KeyEvent.KEYCODE_F10, 0,
      0x100020, 0, -KeyEvent.KEYCODE_F6, 0, -KeyEvent.KEYCODE_F5, 0, 0, 0,
      -KeyEvent.KEYCODE_F4, 0x100048, 0x100051, 0, -KeyEvent.KEYCODE_F11, 0, 0x100050, 0
   };

   final static String PentiHelp[][] = new String[][] {
      {"Chord", "S", "P", "D", "A", "F", null, "Arpeggio"},
      {"# ----", "SPACE", null, null, null, "NEW", null, "# #---", "SHIFT"},
      {"# --#-", "A", "`", "6", "ä", "F6", null, "# -#--", "PUNCT"},
      {"# -##-", "B", "{", "LEFT", null, null, null, "# --#-", "DIGIT"},
      {"- #-#-", "C", "]", ",", "ç", "COPY", null, "# ---#", "CNTRL"},
      {"# ---#", "D", "/", ".", null, null, null, "- #--#", "ALTGR"},
      {"- -#--", "E", "[", "2", "é", "F2", null, "- -##-", "FUNCT"},
      {"# #---", "F", "?", "4", null, "F4"},
      {"- --##", "G", "=", "9", null, "F9", null, "- #-#-", "RET/ESC"},
      {"# #--#", "H", "#", "HOME", null, "HELP", null, "- ##--", "TAB/BS"},
      {"- --#-", "I", "!", "3", "í", "F3"},
      {"- #--#", "J", ";", ":"},
      {"- ####", "K", "@", "DEL"},
      {"- ##--", "L", "_", "7", null, "F7"},
      {"- -###", "M", ">", "-", null, "F12"},
      {"- ---#", "N", ")", "RIGHT", "ñ", "NUM"},
      {"- -##-", "O", "|", "8", "ö", "F8"},
      {"# ###-", "P", "}", "+", null, "PASTE"},
      {"# #-#-", "Q", "'", "PGUP", null, "QUIT"},
      {"# -#--", "R", "$", "5", "€", "F5"},
      {"- #---", "S", "*", "1", "ß", "F1"},
      {"# -###", "T", "%", "~"},
      {"- ###-", "U", "&", "0", "ü", "F10"},
      {"- #-##", "V", "(", "DOWN"},
      {"# ####", "W", "<", "INS"},
      {"# #-##", "X", "\\", "PGDOWN"},
      {"# --##", "Y", "^", "UP", "ÿ", ""},
      {"# ##--", "Z", "\"", "END", null, "F11"}
   };

   public TestThumbkeyboardView(Context context, AttributeSet attrs) {
      super(context, attrs);
      Text1.setColor(Color.BLACK);
      Text1.setStyle(Paint.Style.STROKE);
      Text1.setTextAlign(Paint.Align.CENTER);
      Text2.setColor(Color.WHITE);
      Text2.setTextAlign(Paint.Align.CENTER);
      Circle1.setColor(Color.BLACK);
      Circle1.setPathEffect(new DashPathEffect(new float[]{7,7}, 0));
      Circle1.setStyle(Paint.Style.STROKE);
      Circle1.setStrokeWidth(4);
      Circle2.setColor(Color.WHITE);
      Circle2.setPathEffect(new DashPathEffect(new float[]{7,7}, 7));
      Circle2.setStyle(Paint.Style.STROKE);
      Circle2.setStrokeWidth(4);
   }

   @Override public boolean onTouchEvent(MotionEvent ev) {
      switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
         Beg = (Ev = ev).getEventTime();
         Chord = 0;
         Rev = false;
      case MotionEvent.ACTION_POINTER_DOWN:
         int cnt = ev.getPointerCount();

         End = ev.getEventTime();
         if (Blobs != null) {
            int p = ev.getActionIndex();
            float x = ev.getX(p);
            float y = ev.getY(p);
            int i;

            if (Candidates[0] != null  &&  y < CandY) {
               for (i = 0;  i < CANDIDATES;  ++i)
                  if (x < CandX[i]) {
                     String s = Candidates[i];

                     for (i = 0;  i < s.length();  ++i)
                        Ime.sendKeyChar(s.charAt(i));
                     break;
                  }
               for (i = 0;  i < CANDIDATES;  ++i)
                  Candidates[i] = null;
            }
            else {
               for (i = 0; i < 6; ++i) {
                  if (Blobs[i].contains(x, y)) {
                     if (i == 5) {
                        final int c = (Chord & 0x10) == 0? Repeat : Repeat2;

                        if (c != 0) {
                           Rpt = ++RptN;
                           (new Thread() {
                              public void run() {
                                 int r = Rpt;

                                 send1(c);
                                 try {
                                    sleep(320);
                                    while (Rpt == r) {
                                       send1(c);
                                       sleep(80);
                                    }
                                 }
                                 catch (InterruptedException e) {}
                              }
                           } ).start();
                        }
                     }
                     else {
                        int n = 1 << i;

                        if ((Chord & n-1) != 0)
                           Rev = true;
                        Chord |= n;
                     }
                     if (cnt == 1  ||  cnt == 2  &&  End - Beg >= 60)
                        feedback();
                     break;
                  }
               }
               if (i == 6) {
                  Off = true;
                  if (Help == null)
                     Ime.setCandidatesViewShown(false);
               }
            }
         }
         else if (cnt == 5) {
            Blobs = new Blob[6];
            OrgY = 0;
            for (int p = 0; p < 5; ++p) {
               float x = ev.getX(p);
               float y = ev.getY(p);

               if (y > OrgY) {
                  OrgX = x;
                  OrgY = y;
               }
               Blobs[p] = new Blob(this, x, y);
            }
            Arrays.sort(Blobs, 0, 5);
            Blobs[0].R = Blobs[0].dist(Blobs[1].X, Blobs[1].Y) / 2;
            for (int i = 1; i <= 3; ++i)
               Blobs[i].R = Math.min(
                  Blobs[i].dist(Blobs[i-1].X, Blobs[i-1].Y) / 2,
                  Blobs[i].dist(Blobs[i+1].X, Blobs[i+1].Y) / 2 );
            Blobs[4].R = Blobs[4].dist(Blobs[3].X, Blobs[3].Y) / 2;
            if (conflict()) {
               Blobs = null;
               break;
            }
            Blobs[5] = new Blob(this,
               ((Blobs[0].X + Blobs[4].X) / 2 + Blobs[2].X) / 2,
               ((Blobs[0].Y + Blobs[4].Y) / 2 + Blobs[2].Y) / 2 );
            Blobs[5].R = Math.min(
               Blobs[5].dist(Blobs[1].X, Blobs[1].Y) - Blobs[1].R,
               Math.min(
                  Blobs[5].dist(Blobs[2].X, Blobs[2].Y) - Blobs[2].R,
                  Blobs[5].dist(Blobs[3].X, Blobs[3].Y) - Blobs[3].R ) );
            feedback();
         }
         break;
      case MotionEvent.ACTION_POINTER_UP:
         if (Rpt != 0)
            Rpt = -1;
         break;
      case MotionEvent.ACTION_UP:
         Ime.setCandidatesViewShown(true);
         if (Off || Rpt != 0) {
            Off = false;
            Chord = Rpt = 0;
         }
         else if (End - Beg >= 60  &&  ev.getEventTime() - End < 420) {
            switch (Chord) {
            case 0x18:
               if (!Rev)
                  Shift = -1;
               else if (Shift == 0)
                  Shift = 1;
               else
                  Shift = 0;
               break;
            case 0x14:
               if (!Rev)
                  Punct = -1;
               else if (Punct == 0)
                  Punct = 1;
               else
                  Punct = 0;
               break;
            case 0x12:
               if (!Rev)
                  Digit = -1;
               else if (Digit == 0)
                  Digit = 1;
               else
                  Digit = 0;
               break;
            case 0x11:
               if (!Rev)
                  Cntrl = -1;
               else if (Cntrl == 0)
                  Cntrl = 1;
               else
                  Cntrl = 0;
               break;
            case 0x09:
               if (!Rev)
                  AltGr = -1;
               else if (AltGr == 0)
                  AltGr = 1;
               else
                  AltGr = 0;
               break;
            case 0x0A:
               reset();
               send(Rev? -KeyEvent.KEYCODE_ESCAPE: -KeyEvent.KEYCODE_ENTER);
               break;
            case 0x0C:
               send(Rev? -KeyEvent.KEYCODE_DEL : -KeyEvent.KEYCODE_TAB);
               break;
            case 0x06:
               if (!Rev)
                  Funct = -1;
               else if (Funct == 0)
                  Funct = 1;
               else
                  Funct = 0;
               break;
            default:
               chord();
            }
         }
         else
            chord();
      case MotionEvent.ACTION_CANCEL:
         Ev = null;
         break;
      }
      postInvalidate();
      return true;
   }

   void chord() {
      int c;

      if (Num >= 0)                    // Direct keycode
         c = PentiDigit[Chord];
      else if (Funct < 0)              // Single shots
         c = PentiFunct[Chord];
      else if (Punct < 0) {
         c = PentiPunct[Chord];
         if (Cntrl != 0)
            c &= 0x1F;
      }
      else if (Digit < 0)
         c = PentiDigit[Chord];
      else if (AltGr < 0) {
         c = PentiAltGr[Chord];
         if (c != 0  &&  Shift != 0)
            c = Character.toUpperCase((char)c);
      }
      else if (Cntrl < 0) {
         if ((c = Penti[Chord]) == ' ')
            c = 0x100000;
         else
            c &= 0x1F;
      }
      else if (Shift < 0) {
         c = Character.toUpperCase((char)Penti[Chord]);
         if (Cntrl != 0)
            c &= 0x1F;
      }
      else if (Funct != 0)             // Locks
         c = PentiFunct[Chord];
      else if (Punct != 0) {
         c = PentiPunct[Chord];
         if (Cntrl != 0)
            c &= 0x1F;
      }
      else if (Digit != 0)
         c = PentiDigit[Chord];
      else if (AltGr != 0) {
         c = PentiAltGr[Chord];
         if (c != 0  &&  Shift != 0)
            c = Character.toUpperCase((char)c);
      }
      else {
         c = Penti[Chord];
         if (Cntrl != 0)
            c &= 0x1F;
         else if (c != 0  &&  Shift != 0)
            c = Character.toUpperCase((char)c);
      }
      send(c);
      if (Shift < 0)
         Shift = 0;
      if (Punct < 0)
         Punct = 0;
      if (Digit < 0)
         Digit = 0;
      if (Cntrl < 0)
         Cntrl = 0;
      if (AltGr < 0)
         AltGr = 0;
      if (Funct < 0)
         Funct = 0;
   }

   void send(int c) {
      if (Chord != 0) {
         setBackgroundResource(0);
         Help = null;
      }
      if (c != 0  &&  (c <= 0x100000)  &&  c != Repeat) {
         Repeat2 = Repeat;
         Repeat = c;
      }
      send1(c);
   }

   void send1(int c) {
      if (Num >= 0) {
         if (c == -KeyEvent.KEYCODE_DEL) {
            Num /= 10;
            return;
         }
         if (c >= '0'  &&  c <= '9') {
            Num = Num * 10 + c - '0';
            return;
         }
         if (c != 0) {
            c = Num;
            Num = -1;
         }
      }
      if (c == 0)
         reset();
      else {
         ClipboardManager cm;
         CharSequence s;

         if (Candidates[0] != null) {
            int i, j;

            if (c == 0x100000)
               for (i = 0;  i < CANDIDATES;  ++i)
                  Candidates[i] = null;
            else {
               if (c == -KeyEvent.KEYCODE_DEL  &&  (i = Candidates[0].length()) > 0)
                  Candidates[0] = Candidates[0].substring(0, i-1);
               else if (c >= (Candidates[0].length() > 0? 32 : 33)  &&  c < 0x100000) {
                  Candidates[0] = Candidates[0] + (char)c;
                  if (Dict == null) {
                     // delete the japanses dics part here, because I don't need it and because
                     // it's a large file
                  }
               }
               j = 1;
               if (Candidates[0].length() > 0) {
                  String pat = Candidates[0].toLowerCase();
                  int a = 0;
                  int z = Dict.length - 1;

                  while (a <= z) {
                     i = (a + z) / 2;
                     if (Dict[i].startsWith(pat)) {
                        while (i > 0  &&  Dict[i-1].startsWith(pat))
                           --i;
                        do
                           Candidates[j++] = Dict[i].substring(Dict[i].indexOf('\t') + 1);
                        while (j < CANDIDATES  &&  ++i < Dict.length  &&  Dict[i].startsWith(pat));
                        break;
                     }
                     if (Dict[i].compareTo(pat) > 0)
                        z = i - 1;
                     else
                        a = i + 1;
                  }
               }
               while (j < CANDIDATES)
                  Candidates[j++] = null;
            }
         }
         else if (c < 0)
            Ime.sendDownUpKeyEvents(-c);
         else if (c < 0x100000)
            Ime.sendKeyChar((char)c);
         else {
            switch (c) {
            case 0x100000:  // CNTRL-SPACE
               Candidates[0] = "";
               break;
            case 0x100020:  // NEW (FUNCT-SPACE)
               Blobs = null;
               break;
            case 0x100043:  // COPY (FUNCT-C)
               cm = (ClipboardManager)Ime.getSystemService(Context.CLIPBOARD_SERVICE);
               if ((s = Ime.getCurrentInputConnection().getSelectedText(0)) != null)
                  cm.setPrimaryClip(ClipData.newPlainText("Penti", s));
               break;
            case 0x10004E:  // NUM (FUNCT-N)
               Num = 0;
               break;
            case 0x100048:  // HELP (FUNCT-H)
               if (Help == null) {
                  Help = new Paint();
                   //setBackgroundResource(R.drawable.help);
                  Help.setTextAlign(Paint.Align.CENTER);
               }
               break;
            case 0x100050:  // PASTE (FUNCT-P)
               cm = (ClipboardManager)Ime.getSystemService(Context.CLIPBOARD_SERVICE);
               if (cm.getPrimaryClipDescription().hasMimeType("text/plain")  &&
                        (s = cm.getPrimaryClip().getItemAt(0).getText()) != null )
                  Ime.getCurrentInputConnection().commitText(s, 1);
               break;
            case 0x100051:  // QUIT (FUNCT-Q)
               Ime.requestHideSelf(0);
               break;
            }
         }
      }
   }

   boolean conflict() {
      for (int i = 0; i <= 4; ++i)
         for (int j = 0; i <= 4; ++i)
            if (i != j  &&  Blobs[i].dist(Blobs[j].X, Blobs[j].Y) < Blobs[i].R + Blobs[j].R)
               return true;
      return false;
   }

   void feedback() {
      performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
      playSoundEffect(SoundEffectConstants.CLICK);
   }

   void fillCircle(Canvas canvas, float x, float y, float r) {
      CircleFill.setShader(new RadialGradient(x, y, r, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
      canvas.drawCircle(x, y, r, CircleFill);
   }

   @Override protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      float w = canvas.getWidth();
      float h = canvas.getHeight();

      if (Blobs != null) {
         String s;

         if (Help != null) {
            float n = h / (PentiHelp.length + 2);
            int pos[] = {7, 19, 29, 39, 49, 59, 69, 79, 92};

            Help.setTextSize(h > w? n * 2 / 3 : n * 3 / 4);
            Help.setColor(Color.RED);
            Help.setTypeface(Typeface.SERIF);
            for (int row = 0; row < PentiHelp.length; ++row) {
               for (int col = 0; col < PentiHelp[row].length; ++col)
                  if (PentiHelp[row][col] != null)
                     canvas.drawText(PentiHelp[row][col], w/100 * pos[col], n + row * n, Help);
               Help.setColor(Color.BLUE);
               Help.setTypeface(Typeface.MONOSPACE);
            }
         }
         else if ((s = Candidates[0]) != null) {
            Paint p = new Paint();
            float x, y, n;

            p.setColor(Color.rgb(0xFF, 0xD5, 0x88));
            for (int i = 0;  i < CANDIDATES;  ++i)
               CandX[i] = w;
            y = Math.min(w, h);
            canvas.drawRect(0, 0, w, CandY = y/24, p);
            p.setTypeface(Typeface.SERIF);
            p.setColor(Color.BLUE);
            p.setTextSize(y / 36);
            canvas.drawText(s, x = 6, y / 32, p);
            p.setStrokeWidth(4);
            n = p.measureText(s);
            for (int i = 1;  i < CANDIDATES;  ++i) {
               if ((s = Candidates[i]) == null  ||  (x += n + 12) >= w)
                  break;
               CandX[i-1] = x;
               canvas.drawLine(x, 0, x, y/24, p);
               if ((x += 12) + (n = p.measureText(s)) >= w)
                  break;
               canvas.drawText(s, x, y / 32, p);
            }
         }
         if (!Off) {
            if (Num >= 0)                 // Direct keycode
               s = Integer.toString(Num);
            else if (Funct < 0)           // Single shots
               s = "F";
            else if (Punct < 0)
               s = Cntrl == 0? "P" : "CP";
            else if (Digit < 0)
               s = "D";
            else if (AltGr < 0)
               s = Shift == 0? "A" : "SA";
            else if (Cntrl < 0)
               s = "C";
            else if (Shift < 0)
               s = "S";
            else if (Funct != 0)          // Locks
               s = "F";
            else if (Punct != 0)
               s = Cntrl == 0? "P" : "CP";
            else if (Digit != 0)
               s = "D";
            else if (AltGr != 0)
               s = Shift == 0? "A" : "SA";
            else if (Cntrl != 0)
               s = "C";
            else if (Shift != 0)
               s = "S";
            else
               s = "";
            if (s.length() != 0) {
               float x = Blobs[5].X;
               float y = Blobs[5].Y;
               float f = Blobs[2].dist(x, y) / 4;

               x = 2 * Blobs[2].X - x;
               y = 2 * Blobs[2].Y - y;
               Text1.setTextSize(f);
               Text1.setStrokeWidth(f / 8);
               canvas.drawText(s, x, y, Text1);
               Text2.setTextSize(f);
               canvas.drawText(s, x, y, Text2);
            }
            for (int i = 0; i < 6; ++i)
               Blobs[i].draw(canvas);
         }
      }
      else if (Ev != null) {
         int r = canvas.getWidth() / 8;

         for (int p = Ev.getPointerCount(); --p >= 0;)
            fillCircle(canvas, Ev.getX(p), Ev.getY(p), r);
      }
      else {
         Paint p = new Paint();

         p.setTextSize(w / 8);
         p.setTypeface(Typeface.SERIF);
         p.setTextAlign(Paint.Align.CENTER);
         p.setShader(new LinearGradient(w/4, 0, w*3/4, 0, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
         canvas.drawText("Thumbkeyboard", w/2, h/2, p);
      }
   }

   public void reset() {
      Shift = Punct = Digit = Cntrl = AltGr = Funct = 0;
      Num = -1;
   }

   public class Blob implements Comparable<Blob> {
      TestThumbkeyboardView PV;
      float X, Y, R;

      Blob(TestThumbkeyboardView pv, float x, float y) {
         PV = pv;
         X = x;
         Y = y;
      }

      public boolean contains(float x, float y) {
         float dx = x - X;
         float dy = y - Y;

         return Math.sqrt(dx * dx + dy * dy) <= R;
      }

      public void draw(Canvas canvas) {
         if (PV.Ev != null) {
            for (int p = Ev.getPointerCount(); --p >= 0;)
               if (contains(Ev.getX(p), Ev.getY(p))) {
                  fillCircle(canvas, X, Y, R);
                  return;
               }
         }
         canvas.drawCircle(X, Y, R, Circle1);
         canvas.drawCircle(X, Y, R, Circle2);
      }

      public int dist(float x, float y) {
         x -= X;
         y -= Y;
         return (int)Math.sqrt(x * x + y * y);
      }

      public int compareTo(Blob p) {
         return p.dist(PV.OrgX, PV.OrgY) - dist(PV.OrgX, PV.OrgY);
      }
   }
}

// 16may16abu
// (c) Software Lab. Alexander Burger

package com.adellica.thumbkeyboard;

import android.content.Intent;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.adellica.thumbkeyboard.ThumbJoy.Applicable;
import com.adellica.thumbkeyboard.ThumbJoy.Datum;
import com.adellica.thumbkeyboard.ThumbJoy.IPair;
import com.adellica.thumbkeyboard.ThumbJoy.Keyword;
import com.adellica.thumbkeyboard.ThumbJoy.Machine;
import com.adellica.thumbkeyboard.ThumbJoy.Pair;
import com.adellica.thumbkeyboard.ThumbJoy.Str;
import com.adellica.thumbkeyboard.ThumbJoy.Word;
import com.adellica.thumbkeyboard3.R;

import static android.R.attr.keycode;
import static com.adellica.thumbkeyboard.ThumbJoy.Machine.M;
import static com.adellica.thumbkeyboard.ThumbJoy.Pair.cons;
import static com.adellica.thumbkeyboard.ThumboardKeycodes.init;


public class ThumbkeyboardIME extends InputMethodService {
    private static final String TAG = "ThumbkeyboardIME";
    ThumbkeyboardView PV;

    private static Machine m = null;
    private final JoyLibrary mylib = new JoyIME();

    public static Machine m() {
        if(m == null) {
            m = new Machine(Pair.nil, Pair.nil, Machine.dictDefault());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ThumbReader.serve(m, 1234);
                }
            }).start();
        }
        return m;
    }
    public ThumbkeyboardIME() {
        JoyLibrary.fillDict(m().dict, mylib);
    }

    @Override public View onCreateInputView() {
      return null;
   }

   @Override public View onCreateCandidatesView() {  // http://stackoverflow.com/a/20319466/1160216
      PV = (ThumbkeyboardView)getLayoutInflater().inflate(R.layout.input, null);
      PV.Ime = this;
      return PV;
   }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        Log.i(TAG, ""
                + " oldSelStart " + oldSelStart
                + "oldSelEnd " + oldSelEnd
                + "newSelStart " + newSelStart
                + "newSelEnd " + newSelEnd
                + "candidatesStart " + candidatesStart
                + "candidatesEnd " + candidatesEnd);
    }


    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
      super.onStartInputView(info, restarting);
      setCandidatesViewShown(true);
   }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {


        Log.d(TAG, "onStartInput " + attribute + " (restarting " + restarting + ")");
        super.onStartInput(attribute, restarting);

        ThumboardKeycodes.init();
    }

    @Override
    public void onUpdateCursor(Rect newCursor) {
        Log.d(TAG, "onUpdateCursor " + newCursor);
        super.onUpdateCursor(newCursor);
    }

    @Override public boolean onEvaluateFullscreenMode() {
      return false;
   }

   @Override public void onFinishInput() {
      setCandidatesViewShown(false);
      super.onFinishInput();
   }

    String readForwardsUntil(String p, boolean eof) {
        final InputConnection ic = getCurrentInputConnection();
        if(ic == null) return null;
        int size = 32;
        String c = null;
        while(size < 4096) {
            c = ic.getTextAfterCursor(size, 0).toString();
            int idx = c.indexOf(p);
            if(idx >= 0) { return c.substring(0, idx); }
            size *= 2;
        }
        return eof ? c : null;
    }

    // utils
    String readBackwardsUntil(String p, boolean eof) {
        final InputConnection ic = getCurrentInputConnection();

        if(ic == null) return null;
        int size = 32;
        String c = null;
        while(size < 4096) {
            c = ic.getTextBeforeCursor(size, 0).toString();
            int idx = c.lastIndexOf(p);
            if(idx >= 0) { return c.substring(idx + 1); }
            size *= 2;
        }
        return eof ? c : null;
    }



//    public static int getMetaState( ) {
//        int win = 0;
//        if (modShift()) win |= KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
//        if (modCtrl())  win |= KeyEvent.META_CTRL_ON  | KeyEvent.META_CTRL_LEFT_ON;
//        if (modAlt())   win |= KeyEvent.META_ALT_ON   | KeyEvent.META_ALT_LEFT_ON;
//        if (modMeta())  win |= KeyEvent.META_META_ON  | KeyEvent.META_META_LEFT_ON;
//        return win;
//    }

    private boolean deleteSurroundingText(int before, int after) {
        final InputConnection ic = getCurrentInputConnection();
        if(ic != null) {
            ic.deleteSurroundingText(before, after);
            return true;
        }
        return false;
    }

    /**
     * This guy isn't great. You can't specify SPACE|ENTER. So, for example,
     * a word boundary can only be " " and not " |\n|\t" which is very limiting.
     * @return The string that was deleted
     */
    private String deleteSurroundingUntil(final String pre, boolean bof, final String post, boolean eof, boolean trimLeft, boolean trimRight) {
        final String preline = readBackwardsUntil(pre, bof);
        final String postline = readForwardsUntil(post, eof);
        if(deleteSurroundingText(
                preline == null ? 0 : preline.length()   + (trimLeft  ? 1 : 0),
                postline == null ? 0 : postline.length() + (trimRight ? 1 : 0)))
            return preline + postline;
        return "";
    }

    void press(final Keypress keypress) {
        final long now = System.currentTimeMillis();
        final InputConnection ic = getCurrentInputConnection();
        if(ic != null) {
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keypress.keycode, 0, keypress.getMetaState()));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,   keypress.keycode, 0, keypress.getMetaState()));
        } else Log.e(TAG, "obs: current input connection is null");
    }

    public void handleStroke(Stroke stroke) {
        Object o = m().dict.get("handle");
        if(o instanceof Applicable) {
            M(cons(new StrokeWrapper(stroke.toString()), m().stk), Pair.nil, m()).eval(o);
        } else {
            Log.d(TAG, "“handle” is undefined in Machine, try [ ' handle [ println ] set ] i");
        }
    }

    public static class StrokeWrapper extends Datum<String> {
        public StrokeWrapper(String value) {super(value);}
        @Override public String toString() {return "@" + value;}
    }

    @SuppressWarnings("unused") // used by reflection
    public class JoyIME extends JoyLibrary {

        public NamedApplicable lword = new NamedApplicable() {
            public Machine exe(Machine m) {return M(cons(new Str(readBackwardsUntil(" ", true)), m.stk), m);}
        };
        public NamedApplicable rword = new NamedApplicable() {
            public Machine exe(Machine m) {return M(cons(new Str(readForwardsUntil(" ", true)), m.stk), m);}
        };

        public IPair word = Pair.list(lword, rword, Strings.concat);
        public NamedApplicable insert = new NamedApplicable() {
            public Machine exe(Machine m) {
                final String input = m.stk.car(Str.class).value;
                getCurrentInputConnection().commitText(input, 0);
                return M(m.stk.cdr(), m);
            }
        };
        public NamedApplicable press = new NamedApplicable() {
            public Machine exe(Machine m) {
                IPair p = m.stk;
                final Keypress o = p.car(Keypress.class); p = p.cdr();
                press(o);
                return M(p, m);
            }
        };
        public NamedApplicable app = new NamedApplicable() {
            @Override
            public Machine exe(Machine m) {
                return M(cons(null, m.stk), m);
            }
        };
    }
}

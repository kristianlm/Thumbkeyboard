// 16may16abu
// (c) Software Lab. Alexander Burger

package com.adellica.thumbkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;

import com.adellica.thumbkeyboard3.R;


public class ThumbkeyboardIME extends InputMethodService {
   ThumbkeyboardView PV;

    private static ThumbForth.Machine m = null;

    public static ThumbForth.Machine m() {
        if(m == null) {
            m = new ThumbForth.Machine();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ThumbForthServer.serve(m, 1234);
                }
            }).start();
        }
        return m;
    }

   @Override public View onCreateInputView() {
      return null;
   }

   @Override public View onCreateCandidatesView() {  // http://stackoverflow.com/a/20319466/1160216
      PV = (ThumbkeyboardView)getLayoutInflater().inflate(R.layout.input, null);
      PV.Ime = this;
      return PV;
   }

   @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
      super.onStartInputView(info, restarting);
      setCandidatesViewShown(true);
   }

   @Override public boolean onEvaluateFullscreenMode() {
      return false;
   }

   @Override public void onFinishInput() {
      setCandidatesViewShown(false);
      super.onFinishInput();
   }
}

// 16may16abu
// (c) Software Lab. Alexander Burger

package com.adellica.thumbkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;

public class ThumbkeyboardIME extends InputMethodService {
   ThumbkeyboardView PV;

   @Override public View onCreateInputView() {
      return null;
   }

   @Override public View onCreateCandidatesView() {  // http://stackoverflow.com/a/20319466/1160216
      PV = (ThumbkeyboardView)getLayoutInflater().inflate(R.layout.input, null);
      PV.Ime = this;
      PV.reset();
      return PV;
   }

   @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
      super.onStartInputView(info, restarting);
      setCandidatesViewShown(true);
   }

   @Override public boolean onEvaluateFullscreenMode() {
      return false;
   }

   @Override public void requestHideSelf(int flags) {
      super.requestHideSelf(flags);
      PV.setBackgroundResource(0);
      PV.Help = null;
      PV.reset();
   }

   @Override public void onFinishInput() {
      setCandidatesViewShown(false);
      super.onFinishInput();
   }
}

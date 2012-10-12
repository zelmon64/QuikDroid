/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package sk.ksp.riso.quikdroid;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import sk.ksp.riso.quikdroid.KeyboardView;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.res.Configuration;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class Quikdroid extends InputMethodService {
    
    private KeyboardView myInputView;
    
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
    }
    
    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        super.onInitializeInterface();
    }

    static final String prefname = "KbdView";
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
        // super.onCreateInputView();
        myInputView = (KeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        myInputView.loadPreferences(getSharedPreferences(prefname, 0));
        myInputView.setInputConnection(getCurrentInputConnection());
        return myInputView;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        return null;
    }

    @Override public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        WindowManager.LayoutParams lp = win.getAttributes();

        lp.verticalMargin = 0;
        lp.horizontalMargin = 0;

        win.setAttributes(lp);
        win.setLayout(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        if (myInputView != null) {
          myInputView.setInputConnection(getCurrentInputConnection());
        }
    }

    public void updateInputViewShown() {
      View oldInputView = myInputView;
      super.updateInputViewShown();
      if (myInputView != null && oldInputView != myInputView) {
        if (Build.VERSION.SDK_INT < 11) {
          myInputView.setLayoutParams( new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                    LayoutParams.WRAP_CONTENT));
          if (myInputView.getParent() != null) {
            ViewGroup inputFrame = (ViewGroup)(myInputView.getParent());
            inputFrame.removeAllViews();
            if (inputFrame.getParent() != null) {
              ViewGroup inputFrameP = (ViewGroup)(inputFrame.getParent());
              inputFrameP.removeAllViews();
              inputFrameP.addView(myInputView, new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            }
          }
        } else {
          // no ugly hack necessary for Honeycomb and up.
        }
      }

    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        if (myInputView != null) {
          myInputView.setInputConnection(null);
        }
        super.onFinishInput();
    }
    
/*    
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        //myInputView.closing();
    }
*/
    
    public boolean onEvaluateFullscreenMode() {
      return Build.VERSION.SDK_INT < 11;
    }

    public void onUpdateExtractingViews(EditorInfo ei) {
      setExtractViewShown(false);
    }

    public void onUpdateExtractingVisibility(EditorInfo ei) {
      setExtractViewShown(false);
    }

    public void onComputeInsets(Insets outInsets) {
      if (myInputView == null) return;

      int h = getWindow().getWindow().getDecorView().getHeight();
      if (!myInputView.isTransparent()) {
        outInsets.contentTopInsets = outInsets.visibleTopInsets = 0;
      } else {
        outInsets.contentTopInsets = outInsets.visibleTopInsets = h;
      }
      if (Build.VERSION.SDK_INT < 110) {
        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_FRAME;
      } else {
        /*
        int w = getWindow().getWindow().getDecorView().getWidth();
        int l = getWindow().getWindow().getDecorView().getLeft();
        Log.v("quikdroid", "onComputeInsets: w = " + w + " h = " + h +
            " mw = " + myInputView.getWidth() + " mh = " + myInputView.getHeight());
        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION;
        outInsets.touchableRegion.set(l, 0, h, l + w);
        */
      }
    }

    public void onWindowHidden() {
      if (myInputView != null ) {
        myInputView.savePreferences(getSharedPreferences(prefname, 0));
      }
      super.onWindowHidden();
    }

    public void onConfigurationChanged(Configuration newConfig) {
      if (myInputView != null ) {
        myInputView.savePreferences(getSharedPreferences(prefname, 0));
      }
      super.onConfigurationChanged(newConfig);
    }

}

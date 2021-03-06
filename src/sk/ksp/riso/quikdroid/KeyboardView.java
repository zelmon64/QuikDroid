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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputConnection;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import java.lang.Math;
import android.graphics.drawable.BitmapDrawable;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;

public class KeyboardView extends View {
  Vibrator vib;

  public KeyboardView(Context context) {
    super(context);
    commonInit(context);
  }

  public KeyboardView(Context context, AttributeSet attr) {
    super(context, attr);
    commonInit(context);
  }

  public KeyboardView(Context context, AttributeSet attr, int defStyle) {
    super(context, attr, defStyle);
    commonInit(context);
  }

  public void commonInit(Context context) {
    initRegions();
    vib = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
  }

  InputConnection ic;

  public void setInputConnection(InputConnection nic) {
    ic = nic;
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    DisplayMetrics metrics = new DisplayMetrics();
    ((android.view.WindowManager)getContext().getSystemService(InputMethodService.WINDOW_SERVICE))
        .getDefaultDisplay().getMetrics(metrics);

    int w;
    w = (Math.min(metrics.widthPixels, metrics.heightPixels)*scale) / 10;
    size = w;
    Log.v("quikdroid", "setMeasuredDimensions: orig = " + View.MeasureSpec.getSize(widthMeasureSpec) + " w = " + w);
    setMeasuredDimension(w, w);
  }

  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    Log.v("quikdroid", "view onSizeChanged: w = " + w);
    size = w;
    resetRegions();
  }

  Bitmap content = null;

  void refreshContent() {
    content = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(content);

    Paint p = new Paint();
    Resources res = getResources();
    int i,j,bg;

    if (!isTransparent()) {
      p.setARGB(255,255,255,255);
      p.setStyle(Paint.Style.FILL);
      canvas.drawRect(0,0, size, size, p);
    }

    p.setARGB((255*Math.min(10,alpha))/10,255,255,255);
    if (special) bg = sk.ksp.riso.quikdroid.R.drawable.kbd_special;
    else if (shift==caps) bg = sk.ksp.riso.quikdroid.R.drawable.kbd_main;
    else bg = sk.ksp.riso.quikdroid.R.drawable.kbd_shift;

    canvas.drawBitmap( ((BitmapDrawable)(res.getDrawable(bg))).getBitmap(), null, new Rect(0, 0, size, size), p);

    p.setARGB((255*Math.min(10,alpha))/10,0,0,255);
    p.setStrokeWidth(2);
    p.setStyle(Paint.Style.STROKE);
    for (i=0; i<REGIONS; i++)
      for (j=0; j<R[i].n-1; j++) 
        canvas.drawLine(R[i].P[j<<1], R[i].P[j<<1 | 1], 
                        R[i].P[(j+1)<<1], R[i].P[(j+1)<<1|1], p);
  }

  int highlighted_region = -1;

  protected void onDraw(Canvas canvas) {
    if (content == null) refreshContent();
    Paint p = new Paint();
    p.setARGB(255,255,255,255);
    p.setStyle(Paint.Style.FILL);
    canvas.drawBitmap( content, null, new Rect(0, 0, size, size), p);

    if (highlighted_region >= 0) {
      p.setARGB(128, 0, 255, 0);
      p.setStyle(Paint.Style.FILL);
      Path path = new Path();
      path.moveTo(R[highlighted_region].P[0], R[highlighted_region].P[1]);
      for (int j = 1; j < R[highlighted_region].n; j++) {
        path.lineTo(R[highlighted_region].P[j<<1], R[highlighted_region].P[j<<1 | 1]);
      }
      path.close();
      canvas.drawPath(path, p);
    }
  }

  static class Region {
    static int MAX_POINTS = 16;

    static int[] DX = { 1, 0, -1, 0 };
    static int[] DY = { 0, 1, 0, -1 };
    static int[] OFFX = { 0, 1, 1, 0 };
    static int[] OFFY = { 0, 0, 1, 1 };

    public int x,y;
    public int n;

    public int[] P = new int[MAX_POINTS*2];

    private int getX(int size, double x, double y, int rot) { 
      return (int)(size*( x*DX[rot] + y*DX[(rot+1)&3] + OFFX[rot] )); 
    }

    private int getY(int size, double x, double y, int rot) { 
      return (int)(size*( x*DY[rot] + y*DY[(rot+1)&3] + OFFY[rot] )); 
    }

    private void addPt(int size, double x, double y, int rot) {
      P[n<<1    ] = getX(size, x, y, rot);
      P[n<<1 | 1] = getY(size, x, y, rot);
      n++;
    }

    public Region() {
      n = 0;
    }

    public void init1(int size, int rot) {
      n = 0;
      addPt(size, 0.30, 0, rot);
      addPt(size, 0.35, 0.20, rot);
      addPt(size, 0.35, 0.28, rot);
      addPt(size, 0.34, 0.34, rot);
      addPt(size, 0.28, 0.35, rot);
      addPt(size, 0.20, 0.35, rot);
      addPt(size, 0, 0.30, rot);
      x = getX(size, 1.0/8, 1.0/8, rot);
      y = getY(size, 1.0/8, 1.0/8, rot);
    }

    public void init2(int size, int rot) {
      n = 0;
      addPt(size, 0.68, 0, rot);
      addPt(size, 0.60, 0.24, rot);
      addPt(size, 0.53, 0.29, rot);
      addPt(size, 0.5, 0.30, rot);
      addPt(size, 0.47, 0.29, rot);
      addPt(size, 0.40, 0.24, rot);
      addPt(size, 0.32, 0, rot);
      x = getX(size, 0.5, 1.0/8, rot);
      y = getX(size, 0.5, 1.0/8, rot);
    }

    public void init0(int size, int rot) {
      int i;
      n = 0;
      for (i=MAX_POINTS-1; i>=0; i--) {
        addPt(size, 0.5 + 0.19*Math.sin( i*2*Math.PI/(MAX_POINTS-1) - 0.5*(MAX_POINTS-1)*2*Math.PI ),
                    0.5 + 0.19*Math.cos( i*2*Math.PI/(MAX_POINTS-1) - 0.5*(MAX_POINTS-1)*2*Math.PI ),
                    rot );
      }
      x = getX(size, 0.5, 0.5, rot);
      y = getY(size, 0.5, 0.5, rot);
    }

    int vmul(int x1, int y1, int x2, int y2) {
      return x1*y2 - x2*y1;
    }
    
    public boolean inside(int x, int y) {
      int i;
      for (i=1; i<n; i++) {
        if (vmul( P[i<<1  ] - P[(i-1)<<1  ],
                  P[i<<1|1] - P[(i-1)<<1|1],
                          x - P[(i-1)<<1  ],
                          y - P[(i-1)<<1|1] ) < 0) return false;
      }
      return true;
    }
  }

  static int REGIONS = 9;
  Region[] R = new Region[REGIONS];

  int size;
  int scale = 10;
  int alpha = 5;
  int orig_alpha = alpha;

  void initRegions() {
    int i;
    size = 0;
    for (i=0; i<REGIONS; i++)
      R[i] = new Region();
    resetRegions();
  }

  void resetRegions() {
    int i;
    R[0].init0(size, 0);
    for (i=0; i<4; i++) {
      R[2*i+1].init1(size, i);
      R[2*i+2].init2(size, i);
    }
  }

  int getRegion(int x, int y) {
    int i;
    for (i=0; i<REGIONS; i++)
      if (R[i].inside(x,y)) return i;
    return -1;
  }

  static int BUFSIZE = 64;
  int[] buffer = new int[BUFSIZE];
  int buflen = 0;
  int vib_len = 30;
  int orig_vib_len = vib_len;
  static int VIB_MIN = 5;
  static int VIB_MAX = 200;
  static int DOWN = -1;
  static int UP = -2;

  public boolean onTouchEvent(MotionEvent event) {
    if (ic != null) {
      if (event.getAction() == event.ACTION_DOWN) {
        buflen = 0;
        buffer[buflen++] = DOWN;
      }
      if (event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) {
        int r = getRegion( (int)event.getX(), (int)event.getY() );
        if (r != highlighted_region) {
          highlighted_region = r;
          invalidate();
        }
        if (r != -1) {
          if (buflen==BUFSIZE-1) buflen = 0;
          if (buflen == 0 || buffer[buflen-1] != r) {
            buffer[buflen++] = r;
            vib.vibrate(vib_len);
          }
        }
      }
      if (event.getAction() == event.ACTION_UP) {
        if (buflen==BUFSIZE-1) buflen = 0;
        buffer[buflen++] = UP;
        highlighted_region = -1;
        invalidate();
      }
      processBuffer();
    }
    return true;
  }

  static int SHIFT = 65537;
  static int CAPS = 65538;
  static int SPECIAL = 65539;
  static int MOVE_HOME = 65540;
  static int MOVE_END = 65541;
  static int CARON = 65542;
  static int ACUTE = 65543;
  static int UMLAUT = 65544;
  static int VOKAN = 65545;
  boolean shift = false;
  boolean caps = false;
  boolean special = false;
  int accent = 0;

  static int[][] open = { 
    { '5', 0, -KeyEvent.KEYCODE_DPAD_UP, 0, -KeyEvent.KEYCODE_DPAD_RIGHT, '\\', '0', '/', -KeyEvent.KEYCODE_DPAD_LEFT },
    { '`', '1', -KeyEvent.KEYCODE_TAB, '=', 0, '{', '[', '(', '|' },
    { -KeyEvent.KEYCODE_DPAD_DOWN, '^', '2', '-', '>', 0, 0/*-KeyEvent.KEYCODE_PAGE_DOWN*/, 0, '<' },
    { '\'', '%', '$', '3', /*paste*/0, ')', ']', '}', 0 },
    { -KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, '!', '6', -KeyEvent.KEYCODE_ENTER, /*alt*/ 0, 0, MOVE_HOME },
    { CARON, VOKAN, 0, ':', ';', '9', '*', '&', 0 },
    { 0, 0, 0/*-KeyEvent.KEYCODE_PAGE_UP*/, 0, 0, '_', '8', '@', 0 },
    { ACUTE, '#', 0, UMLAUT, 0, '+', '~', '7', '"' },
    { -KeyEvent.KEYCODE_DPAD_RIGHT, 0/*-KeyEvent.KEYCODE_ESCAPE*/, 0, 0, MOVE_END, 0, /*ctrl*/0, SPECIAL, '4' },
  }; 

  static int[][] closed = { 
    { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 'a', ',', 'z', 'z', 0, CAPS, SHIFT, 'c' },
    { 0, 'd', 'e', 'b', 'b', 'b', 0, 'd', 'd' },
    { 0, 'g', 'f', 'i', 'h', 'j', 'j', 0, 'g' },
    { 0, 'l', 'l', 'l', ' ', 'k', 'k', 'k', 0 },
    { 0, 0, 'm', 'm', 'n', 'o', 'p', 'q', 'q' },
    { 0, 'r', 0, 't', 't', 't', 's', 'r', 'r' },
    { 0, '?', '?', 0, '.', '.', 'v', 'u', 'w' },
    { 0, 'y', 'y', 'y', 0, 'x', 'x', 'x', -KeyEvent.KEYCODE_DEL },
  }; 

  void send(int c) {
    if (c==SHIFT) { 
      shift = !shift;  
      special = false;
      display();
    } else if (c==CAPS) { 
      caps = !caps; 
      special = false;
      display();
    } else if (c==SPECIAL) { 
      special = !special;
      display();
    } else if (c==MOVE_HOME) { 
      ic.commitText( "", -1024);
    } else if (c==MOVE_END) { 
      ic.commitText( "", 1024);
    } else if (c==CARON || c==ACUTE || c==UMLAUT || c==VOKAN) { 
      if (accent==0) accent = c;
      else accent = 0;
    } else if (c>0) {
      String s = new String( new char[] { (char)c } );

      if (accent == CARON) {
        if (s.equals("c")) s = "č";
        if (s.equals("d")) s = "ď";
        if (s.equals("e")) s = "ě";
        if (s.equals("l")) s = "ľ";
        if (s.equals("n")) s = "ň";
        if (s.equals("r")) s = "ř";
        if (s.equals("s")) s = "š";
        if (s.equals("t")) s = "ť";
        if (s.equals("z")) s = "ž";
      } else if (accent == ACUTE) {
        if (s.equals("a")) s = "á";
        if (s.equals("e")) s = "é";
        if (s.equals("i")) s = "í";
        if (s.equals("l")) s = "ĺ";
        if (s.equals("o")) s = "ó";
        if (s.equals("r")) s = "ŕ";
        if (s.equals("u")) s = "ú";
        if (s.equals("y")) s = "ý";
      } else if (accent == UMLAUT) {
        if (s.equals("a")) s = "ä";
        if (s.equals("e")) s = "ë";
        if (s.equals("u")) s = "ü";
        if (s.equals("o")) s = "ö";
      } else if (accent == VOKAN) {
        if (s.equals("o")) s = "ô";
      }
      accent = 0;
      if (shift != caps)
        ic.commitText( s.toUpperCase(), 1);
      else ic.commitText( s, 1);
      shift = false;
      display();
    } else if (c<0) {
      ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, -c));
      ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, -c));
    }
  }

  int bufDir(int p) {
    if (buffer[p] >=1 && buffer[p] <= 8 && buffer[p-1] >= 1 && buffer[p-1] <= 8 ) {
      return ((buffer[p] - buffer[p-1] + 8) % 8 == 1) ? 1 : -1;
    }
    return 0;
  }

  void processBuffer() {

    if (buffer[0] == DOWN && buflen > 9 && buffer[1] != 0 ) {
      int d = 0;
      for (int i = 9; i < buflen; ++i) {
        d += bufDir(i);
      }

      if (buffer[1] == 4 || buffer[1] == 8) {
        d -= bufDir(buflen - 1);
        reviblen(d);
      }
      else if (buffer[1] > 4 ) realpha(d);

      if (buffer[buflen - 1] == UP) {
        if (buffer[1] <= 3) resize(d);
        buflen = 0;
        orig_alpha = alpha;
        orig_vib_len = vib_len;
      }
      return;
    }
    if (buffer[0] == DOWN && buffer[buflen-1] == UP) {
      if (buflen==3) {
        send( open[buffer[1]][buffer[1]] );
      } else if (buflen >=4) {
        send( open[buffer[1]][buffer[buflen-2]] );
      }
      buflen = 0;
      return;
    }
    if (buffer[buflen-1] == UP) {
      if (buflen>=2 && buffer[buflen-2]!=0) {
        send( open[buffer[0]][buffer[buflen-2]] );
      } 
      buflen = 0;
      return;
    }
    if (buflen>=3 && buffer[0] == 0 && buffer[buflen-1] == 0) {
      send( closed[buffer[1]][buffer[buflen-2]] );
      buflen = 1;
      return;
    }
    if (buflen>=5 && buffer[2] == 0 && buffer[buflen-1] == 0 &&
                     buffer[0] == DOWN) {
      send( open[buffer[1]][0] );
      send( closed[buffer[3]][buffer[buflen-2]] );
      buflen = 1;
      buffer[0] = 0;
      return;
    }
    if (buflen>=4 && buffer[1] == 0 && buffer[buflen-1] == 0) {
      send( closed[buffer[2]][buffer[buflen-2]] );
      buflen = 1;
      buffer[0] = 0;
      return;
    }
      // ic.commitText( new String( new char[] { (char)('a'+r) } ), 1);
  }

  void display() {
    refreshContent();
    invalidate();
  }

  void resize(int inc) {
    scale += inc;
    if (scale>10) scale = 10;
    if (scale<4) scale = 4;
    content = null;
    requestLayout();
  }

  void realpha(int inc) {
    alpha = orig_alpha + inc;
    if (alpha>11) alpha = 11;
    if (alpha<1) alpha = 1;
    display();
  }

  void reviblen(int inc) {
    if (orig_vib_len == 0) {
      vib_len = (int)(VIB_MIN * Math.pow(1.2, inc));
    } else {
      vib_len = (int)(orig_vib_len * Math.pow(1.2, inc));
    }
    if (vib_len < VIB_MIN) vib_len = 0;
    if (vib_len > VIB_MAX) vib_len = VIB_MAX;
  }

  public boolean isTransparent() {
    return alpha<=10;
  }

  public void savePreferences(SharedPreferences settings) {
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt("alpha", alpha);
    editor.putInt("scale", scale);
    editor.putInt("vib_len", vib_len);
    // Commit the edits!
    editor.commit();
  }

  public void loadPreferences(SharedPreferences settings) {
    alpha = settings.getInt("alpha", 5);
    scale = settings.getInt("scale", 10);
    vib_len = settings.getInt("vib_len", 30);

    orig_alpha = alpha;
    orig_vib_len = vib_len;
    content = null;
    requestLayout();
  }
}

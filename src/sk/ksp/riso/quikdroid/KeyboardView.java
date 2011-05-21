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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.util.AttributeSet;
import java.lang.Math;

public class KeyboardView extends View {
  public KeyboardView(Context context) {
    super(context);
    initRegions();
  }

  public KeyboardView(Context context, AttributeSet attr) {
    super(context, attr);
    initRegions();
  }

  public KeyboardView(Context context, AttributeSet attr, int defStyle) {
    super(context, attr, defStyle);
    initRegions();
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int w = Math.min( View.MeasureSpec.getSize(widthMeasureSpec), 
                      View.MeasureSpec.getSize(heightMeasureSpec) );
    setMeasuredDimension(w, w);
  }

  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    size = w;
    resetRegions();
  }

  protected void onDraw(Canvas canvas) {
    Paint p = new Paint();
    int i,j;

    p.setARGB(255,255,0,0);
    p.setStrokeWidth(1);
    for (i=0; i<REGIONS; i++)
      for (j=0; j<R[i].n-1; j++) 
        canvas.drawLine(R[i].P[j<<1], R[i].P[j<<1 | 1], 
                        R[i].P[(j+1)<<1], R[i].P[(j+1)<<1|1], p);
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
      addPt(size, 0.36, 0.15, rot);
      addPt(size, 0.33, 0.33, rot);
      addPt(size, 0.15, 0.36, rot);
      addPt(size, 0, 0.30, rot);
      x = getX(size, 1.0/8, 1.0/8, rot);
      y = getY(size, 1.0/8, 1.0/8, rot);
    }

    public void init2(int size, int rot) {
      n = 0;
      addPt(size, 0.66, 0, rot);
      addPt(size, 0.58, 0.21, rot);
      addPt(size, 0.53, 0.26, rot);
      addPt(size, 0.5, 0.27, rot);
      addPt(size, 0.47, 0.26, rot);
      addPt(size, 0.42, 0.21, rot);
      addPt(size, 0.33, 0, rot);
      x = getX(size, 0.5, 1.0/8, rot);
      y = getX(size, 0.5, 1.0/8, rot);
    }

    public void init0(int size, int rot) {
      int i;
      n = 0;
      for (i=0; i<MAX_POINTS; i++) {
        addPt(size, 0.5 + 0.22*Math.sin( i*2*Math.PI/(MAX_POINTS-1) - 0.5*(MAX_POINTS-1)*2*Math.PI ),
                    0.5 + 0.22*Math.cos( i*2*Math.PI/(MAX_POINTS-1) - 0.5*(MAX_POINTS-1)*2*Math.PI ),
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
        if (vmul( P[i<<2  ] - P[(i-1)<<2  ],
                  P[i<<2|1] - P[(i-1)<<2|1],
                          x - P[(i-1)<<2  ],
                          y - P[(i-1)<<2|1] ) < 0) return false;
      }
      return true;
    }
  }

  static int REGIONS = 9;
  Region[] R = new Region[REGIONS];

  int size;
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

  static int BUFSIZE = 10;
  int[] buffer = new int[BUFSIZE];
  int buflen = 0;

  public boolean onTouchEvent(MotionEvent event) {
    
  }


}

/*
 * Copyright 2012-2013, Arno Puder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.puder.trs80.keyboard;

import org.puder.trs80.TRS80Application;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

/**
 * http://www.trs-80.com/wordpress/zaps-patches-pokes-tips/internals/#keyboard13
 */
public class Joystick extends View {

    protected static final String KEY_LEFT  = "\u2190";
    protected static final String KEY_RIGHT = "\u2192";
    protected static final String KEY_UP    = "\u2191";
    protected static final String KEY_DOWN  = "\u2193";

    protected Paint               paint;
    protected KeyboardManager     keyboardManager;

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);

        float arrowSize = pxFromDp(25);

        paint = new Paint();
        paint.setTypeface(TRS80Application.getTypefaceBold());
        paint.setTextSize(arrowSize);
        paint.setTextAlign(Align.CENTER);
        paint.setAntiAlias(true);

        keyboardManager = TRS80Application.getKeyboardManager();
    }

    protected void allKeysUp() {
        keyboardManager.allCursorKeysUp();
    }

    protected void pressKeyDown() {
        keyboardManager.pressKeyDown();
    }

    protected void pressKeyUp() {
        keyboardManager.pressKeyUp();
    }

    protected void pressKeyLeft() {
        keyboardManager.pressKeyLeft();
    }

    protected void pressKeyRight() {
        keyboardManager.pressKeyRight();
    }

    protected float pxFromDp(float dp) {
        return dp * this.getContext().getResources().getDisplayMetrics().density;
    }
}

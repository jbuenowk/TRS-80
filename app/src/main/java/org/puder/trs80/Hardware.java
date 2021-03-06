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

package org.puder.trs80;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;

import org.puder.trs80.configuration.Configuration;
import org.puder.trs80.configuration.KeyboardLayout;

/**
 * Class Hardware is the base class for the various different TRS-80 models. It
 * encapsulates various hardware characteristics of different TRS-80 models. In
 * particular, it computes bitmaps to be used during rendering. The size of the
 * font is determined by the size of the screen and whether the emulator runs in
 * landscape or portrait mode. The goal is to scale the size nicely for
 * different screen resolutions. Each bitmap of 'font' represents one character
 * of the ASCII code. For alphanumeric characters the bundled font
 * asset/fonts/DejaVuSansMono.ttf is used (see generateASCIIFont()). For the
 * TRS-80 pseudo-graphics we compute the bitmaps for the 2x3-per character
 * pseudo pixel graphics (see generateGraphicsFont()).
 */
public class Hardware {
    static class ScreenConfiguration {
        final int   trsScreenCols;
        final int   trsScreenRows;
        final float aspectRatio;

        ScreenConfiguration(int trsScreenCols, int trsScreenRows, float aspectRatio) {
            this.trsScreenCols = trsScreenCols;
            this.trsScreenRows = trsScreenRows;
            this.aspectRatio = aspectRatio;
        }
    }


    final private float     maxKeyBoxSize = 55; // 55dp

    public static final int MODEL_NONE    = 0;
    public static final int MODEL1        = 1;
    public static final int MODEL3        = 3;
    public static final int MODEL4        = 4;
    public static final int MODEL4P       = 5;

    private final Configuration configuration;
    private int             trsScreenWidth;
    private int             trsScreenHeight;
    private int             trsCharWidth;
    private int             trsCharHeight;

    private int             keyWidth;
    private int             keyHeight;
    private int             keyMargin;

    private Bitmap[]        font;


    protected Hardware(Configuration configuration) {
        this.configuration = configuration;
        font = new Bitmap[256];
    }

    public int getModel() {
        return configuration.getModel();
    }

    ScreenConfiguration getScreenConfiguration() {
        switch (configuration.getModel()) {
        case Hardware.MODEL1:
        case Hardware.MODEL3:
            final int trsScreenCols = 64;
            final int trsScreenRows = 16;
            final float aspectRatio = 3f;
            return new ScreenConfiguration(trsScreenCols, trsScreenRows, aspectRatio);
        }
        return null;
    }

    int getScreenWidth() {
        return trsScreenWidth;
    }

    int getScreenHeight() {
        return trsScreenHeight;
    }

    int getCharWidth() {
        return trsCharWidth;
    }

    int getCharHeight() {
        return trsCharHeight;
    }

    int getKeyWidth() {
        return keyWidth;
    }

    int getKeyHeight() {
        return keyHeight;
    }

    int getKeyMargin() {
        return keyMargin;
    }

    Bitmap[] getFont() {
        return font;
    }

    void generateFont(Rect rect, AsyncTask task) {
        ScreenConfiguration screenConfig = getScreenConfiguration();
        int contentViewTop = rect.top;
        int contentHeight = rect.bottom - contentViewTop;
        int contentWidth = rect.right;
        if ((contentWidth / screenConfig.trsScreenCols)
                * screenConfig.aspectRatio > (contentHeight / screenConfig.trsScreenRows)) {
            // Screen height is not sufficient to let the TRS80 screen span the
            // whole width
            trsCharHeight = contentHeight / screenConfig.trsScreenRows;
            // Make sure trsCharHeight is divisible by 3
            while (trsCharHeight % 3 != 0) {
                trsCharHeight--;
            }
            trsScreenHeight = trsCharHeight * screenConfig.trsScreenRows;
            trsCharWidth = (int) (trsCharHeight / screenConfig.aspectRatio);
            trsScreenWidth = trsCharWidth * screenConfig.trsScreenCols;
        } else {
            // Screen width is not sufficient to let the TRS80 screen span the
            // whole height
            trsCharWidth = contentWidth / screenConfig.trsScreenCols;
            while (trsCharWidth % 2 != 0) {
                trsCharWidth--;
            }
            trsScreenWidth = trsCharWidth * screenConfig.trsScreenCols;
            trsCharHeight = (int) (trsCharWidth * screenConfig.aspectRatio);
            trsScreenHeight = trsCharHeight * screenConfig.trsScreenRows;
        }

        generateGraphicsFont(task);
        generateASCIIFont(task);
    }

    void computeKeyDimensions(Rect rect, KeyboardLayout keyboardLayout) {
        // The maximum number of key "boxes" per row
        int maxKeyBoxes = 15;
        switch (keyboardLayout) {
        case KEYBOARD_LAYOUT_COMPACT:
            maxKeyBoxes = 10;
            break;
        case KEYBOARD_LAYOUT_ORIGINAL:
            maxKeyBoxes = 15;
            break;
        case KEYBOARD_LAYOUT_JOYSTICK:
            maxKeyBoxes = 8;
            break;
        }
        int boxWidth = rect.right / maxKeyBoxes;
        float threshold = pxFromDp(maxKeyBoxSize);
        if (boxWidth > threshold) {
            boxWidth = (int) threshold;
        }
        keyWidth = keyHeight = (int) (boxWidth * 0.9f);
        keyMargin = (boxWidth - keyWidth) / 2;
    }

    private void generateASCIIFont(AsyncTask task) {
        Paint p = new Paint();
        p.setTextAlign(Align.CENTER);
        Typeface tf = Fonts.getTypeface(configuration.getModel());
        p.setTypeface(tf);
        p.setTextScaleX(1.0f);
        p.setColor(configuration.getCharacterColorAsRGB());
        p.setAntiAlias(true);
        setFontSize(p);
        int xPos = trsCharWidth / 2;
        int yPos = (int) ((trsCharHeight / 2) - ((p.descent() + p.ascent()) / 2));
        for (int i = 0; i <= 0xff; i++) {
            if (task.isCancelled()) {
                return;
            }
            if (i >= 128 && i <= 191) {
                // Graphic character. Skip. Will be generated in generateGraphicsFont()
                continue;
            }
            Bitmap b = Bitmap.createBitmap(trsCharWidth, trsCharHeight, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            c.drawColor(configuration.getScreenColorAsRGB());
            // http://www.kreativekorp.com/software/fonts/trs80.shtml
            // TRS font starts at code point 0xe000
            int codePoint = 0xe000 + i;
            char[] charPair = Character.toChars(codePoint);
            c.drawText(charPair, 0, charPair.length, xPos, yPos, p);
            font[i] = b;
        }
    }

    /**
     * Compute the correct font size. The font size designates the height of the
     * font. trsCharHeight will be much bigger than trsCharWidth because of the
     * aspect ration. For this reason we cannot use trsCharHeight as the font
     * size. Instead we measure the width of string "X" and incrementally
     * increase the font size until we hit trsCharWidth.
     */
    private void setFontSize(Paint p) {
        float fontSize = trsCharWidth;
        final float delta = 0.1f;
        float width;
        do {
            fontSize += delta;
            p.setTextSize(fontSize);
            width = p.measureText("X");
        } while (width <= trsCharWidth);
        p.setTextSize(fontSize - delta);
    }

    private void generateGraphicsFont(AsyncTask task) {
        Paint p = new Paint();
        for (int i = 128; i <= 191; i++) {
            if (task.isCancelled()) {
                return;
            }
            Bitmap b = Bitmap.createBitmap(trsCharWidth, trsCharHeight, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            c.drawColor(configuration.getScreenColorAsRGB());
            p.setColor(configuration.getCharacterColorAsRGB());
            Rect r = new Rect();
            // Top-left
            if ((i & 1) != 0) {
                r.left = r.top = 0;
                r.right = trsCharWidth / 2;
                r.bottom = trsCharHeight / 3;
                c.drawRect(r, p);
            }

            // Top-right
            if ((i & 2) != 0) {
                r.left = trsCharWidth / 2;
                r.right = trsCharWidth;
                r.top = 0;
                r.bottom = trsCharHeight / 3;
                c.drawRect(r, p);
            }

            // Middle-left
            if ((i & 4) != 0) {
                r.left = 0;
                r.right = trsCharWidth / 2;
                r.top = trsCharHeight / 3;
                r.bottom = trsCharHeight / 3 * 2;
                c.drawRect(r, p);
            }

            // Middle-right
            if ((i & 8) != 0) {
                r.left = trsCharWidth / 2;
                r.right = trsCharWidth;
                r.top = trsCharHeight / 3;
                r.bottom = trsCharHeight / 3 * 2;
                c.drawRect(r, p);
            }

            // Bottom-left
            if ((i & 16) != 0) {
                r.left = 0;
                r.right = trsCharWidth / 2;
                r.top = trsCharHeight / 3 * 2;
                r.bottom = trsCharHeight;
                c.drawRect(r, p);
            }

            // Bottom-right
            if ((i & 32) != 0) {
                r.left = trsCharWidth / 2;
                r.right = trsCharWidth;
                r.top = trsCharHeight / 3 * 2;
                r.bottom = trsCharHeight;
                c.drawRect(r, p);
            }

            font[i] = b;
        }
    }

    private float pxFromDp(float dp) {
        return dp * TRS80Application.getAppContext().getResources().getDisplayMetrics().density;
    }

}

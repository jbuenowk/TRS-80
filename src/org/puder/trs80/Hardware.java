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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.Window;

abstract public class Hardware {

    public enum Model {
        NONE(0), MODEL1(1), MODEL3(3), MODEL4(4), MODEL4P(5);
        private int model;

        private Model(int model) {
            this.model = model;
        }

        public int getModelValue() {
            return model;
        }
    };

    protected Model  model;
    protected byte[] screenBuffer;
    private int      entryAddr;

    protected Hardware(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    protected void setScreenBuffer(int size) {
        screenBuffer = new byte[size];
    }

    public byte[] getScreenBuffer() {
        return this.screenBuffer;
    }

    public void setEntryAddress(int addr) {
        this.entryAddr = addr;
    }

    public int getEntryAddress() {
        return entryAddr;
    }

    abstract public void computeFontDimensions(Window window);

    abstract public int getScreenCols();

    abstract public int getScreenRows();

    abstract public int getScreenWidth();

    abstract public int getScreenHeight();

    abstract public int getCharWidth();

    abstract public int getCharHeight();

    abstract public int getKeyHeight();

    abstract public int getKeyWidth();

    abstract public int getKeyMargin();

    abstract public Bitmap[] getFont();

}

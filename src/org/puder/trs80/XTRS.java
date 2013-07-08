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

public class XTRS {

    static {
        System.loadLibrary("xtrs");
    }

    private static RenderThread     renderer      = null;

    private static Audio            audioRunnable = null;

    private static Thread           audioThread   = null;

    private static EmulatorActivity emulator      = null;

    public static native void setRunning(boolean run);

    public static native int init(int model, int sizeROM, int entryAddr, byte[] mem, byte[] screen);

    public static native void reset();

    public static native int setAudioBuffer(byte[] data);

    public static native void fillAudioBuffer();

    public static native void cleanup();

    public static native void run();

    public static void setRenderer(RenderThread r) {
        renderer = r;
    }

    public static void setEmulatorActivity(EmulatorActivity activity) {
        emulator = activity;
    }

    public static String getDiskPath(int disk) {
        return TRS80Application.getCurrentConfiguration().getDiskPath(disk);
    }

    public static boolean isRendering() {
        return (renderer == null) ? true : renderer.isRendering();
    }

    public static void updateScreen() {
        if (renderer != null) {
            renderer.triggerScreenUpdate();
        }
    }

    public static void initAudio(int rate, int channels, int encoding, int bufSize) {
        closeAudio();
        audioRunnable = new Audio(rate, channels, encoding, bufSize);
    }

    public static void closeAudio() {
        if (audioRunnable != null) {
            audioRunnable.deinitAudio();
            try {
                if (audioThread != null) {
                    audioThread.join();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            audioThread = null;
            audioRunnable = null;
        }
    }

    public static void pauseAudio(int pauseOn) {
        if (audioRunnable == null) {
            return;
        }
        audioRunnable.setRunning(false);
        if (pauseOn == 0) {
            audioRunnable.setRunning(true);
            audioThread = new Thread(audioRunnable);
            audioThread.start();
        }
    }

    public static void xlog(String msg) {
        if (emulator != null) {
            emulator.log(msg);
        }
    }
}

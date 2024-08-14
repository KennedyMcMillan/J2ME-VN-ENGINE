// POWERED BY CHATGPT 4o
package com.visualnovel.alpha;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class VisualNovelEngine extends MIDlet {
    private Display display;
    private GameCanvas canvas;
    private SoundManager soundManager;

    public void startApp() {
        display = Display.getDisplay(this);
        soundManager = new SoundManager();
        canvas = new GameCanvas(soundManager);
        display.setCurrent(canvas);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {
        soundManager.stopMusic();
    }
}


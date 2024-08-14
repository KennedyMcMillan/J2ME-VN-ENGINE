// POWERED BY CHATGPT 4o
package com.visualnovel.alpha;

import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.control.VolumeControl;
import java.io.InputStream;

public class SoundManager {
    private Player player;

    public void playMusic(String path) {
        if (player != null) {
            stopMusic();
        }

        try {
            // Determine the MIME type based on file extension
            String mimeType = getMimeType(path);
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            player = Manager.createPlayer(is, mimeType);
            player.realize();
            VolumeControl volumeControl = (VolumeControl) player.getControl("VolumeControl");
            if (volumeControl != null) {
                volumeControl.setLevel(100); // Set volume level
            }
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMimeType(String path) {
        if (path.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (path.endsWith(".wav")) {
            return "audio/x-wav";
        } else if (path.endsWith(".midi") || path.endsWith(".mid")) {
            return "audio/midi";
        } else {
            throw new IllegalArgumentException("Unsupported audio format: " + path);
        }
    }

    public void stopMusic() {
        if (player != null) {
            try {
                player.stop();
                player.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            player = null;
        }
    }
}


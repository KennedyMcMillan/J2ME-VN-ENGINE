// POWERED BY CHATGPT 4o
package com.visualnovel.alpha;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.VolumeControl;
import java.io.InputStream;
import java.util.Vector;
import java.util.Random;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GameCanvas extends Canvas {
    private Image currentBackground, textbox;
    private Hashtable images = new Hashtable(); // For storing images
    private Hashtable musics = new Hashtable(); // For storing music
    private Hashtable sounds = new Hashtable(); // For storing sound effects
    private String sceneText = "";
    private Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private int textX;
    private int textY;
    private boolean isDissolve = false;
    private boolean isShake = false;
    private boolean isVibrationOn = false; // flag for enable vibration
    private int dissolveStep = 0;
    private int shakeOffsetX = 0, shakeOffsetY = 0;
    private Random random = new Random();
    private int shakeCounter = 0;
    private int sceneIndex = 0;
    private Vector lines;
    private Player musicPlayer;
    private Player soundPlayer;

    public GameCanvas() {
        try {
            textbox = Image.createImage("/textbox.png");
            textX = 10; // Small margin from the left side
            textY = getHeight() - textbox.getHeight() + 10; // 10 pixels from the top of the textbox

            loadScene("/script.scene"); // Load initial scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                processCommand(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processCommand(String command) {
        if (command.startsWith("scn ")) {
            String imageName = command.substring(4).trim();
            currentBackground = (Image) images.get(imageName);

            if (command.contains("and vibr_on")) {
                startVibration(); // Включаем вибрацию, если она указана в команде
            }
        } else if (command.startsWith("wt ")) {
            String effect = command.substring(3).trim();
            if (effect.equals("dissolve")) {
                isDissolve = true;
            }
        } else if (command.startsWith("show_image ")) {
            String imageName = command.substring(11).trim();
            // Show image logic (implement as needed)
        } else if (command.startsWith("hide_image ")) {
            String imageName = command.substring(11).trim();
            // Hide image logic (implement as needed)
        } else if (command.startsWith("pl msc ")) {
            String musicName = command.substring(7).trim();
            playMusic((String) musics.get(musicName));
        } else if (command.startsWith("pl sd ")) {
            String soundName = command.substring(6).trim();
            playSound((String) sounds.get(soundName));
        } else if (command.startsWith("img ")) {
            String[] parts = command.substring(4).split(" = ");
            String imageName = parts[0].trim();
            String imagePath = parts[1].trim();
            try {
                images.put(imageName, Image.createImage(imagePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command.startsWith("msc ")) {
            String[] parts = command.substring(4).split(" = ");
            String musicName = parts[0].trim();
            String musicPath = parts[1].trim();
            musics.put(musicName, musicPath);
        } else if (command.startsWith("sd ")) {
            String[] parts = command.substring(3).split(" = ");
            String soundName = parts[0].trim();
            String soundPath = parts[1].trim();
            sounds.put(soundName, soundPath);
        } else if (command.startsWith("text ")) {
            sceneText = command.substring(5).trim();
            lines = splitTextToLines(sceneText, getWidth() - 20); // Update lines for new text
        } else if (command.startsWith("lbl ")) {
            // Handle label commands as needed
        }
    }

    private void startVibration() {
        Display.getDisplay(null).vibrate(500); // Вибрация на 500 мс
    }

    protected void paint(Graphics g) {
        // Clear screen
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw current background
        if (currentBackground != null) {
            g.drawImage(currentBackground, shakeOffsetX, shakeOffsetY, Graphics.TOP | Graphics.LEFT);
        }

        // Draw textbox
        if (textbox != null) {
            g.drawImage(textbox, 0, getHeight() - textbox.getHeight(), Graphics.TOP | Graphics.LEFT);
        }

        // Draw text
        if (lines != null) {
            g.setColor(255, 255, 255); // White color for text
            g.setFont(font);
            int currentY = textY;
            for (int i = 0; i < lines.size(); i++) {
                g.drawString((String) lines.elementAt(i), textX, currentY, Graphics.TOP | Graphics.LEFT);
                currentY += font.getHeight(); // Move to the next line
            }
        }

        // Apply effects
        if (isShake) {
            shakeEffect();
        } else if (isDissolve) {
            dissolveEffect(g);
        }
    }

    private Vector splitTextToLines(String text, int maxWidth) {
        Vector result = new Vector();
        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = start;
            int lineWidth = 0;

            while (end < length) {
                char c = text.charAt(end);
                lineWidth += font.charWidth(c);
                if (lineWidth > maxWidth) {
                    break;
                }
                end++;
            }

            if (end == start) { // If a single character is too wide, move to next
                end++;
            }

            result.addElement(text.substring(start, end).trim());
            start = end;
        }

        return result;
    }

    private void dissolveEffect(Graphics g) {
        if (dissolveStep < 255) {
            int alpha = (255 - dissolveStep) / 16; // Simple alpha effect
            g.setColor(0, 0, 0);
            for (int y = 0; y < getHeight(); y += 2) {
                for (int x = 0; x < getWidth(); x += 2) {
                    if (random.nextInt(16) < alpha) { // Randomize the dissolve effect
                        g.drawLine(x, y, x, y);
                    }
                }
            }
            dissolveStep += 10;
        } else {
            isDissolve = false;
            dissolveStep = 0;
            advanceScene();
        }
        repaint();
    }

    private void shakeEffect() {
        if (shakeCounter < 20) { // Shake for 20 frames
            shakeOffsetX = random.nextInt(10) - 5;
            shakeOffsetY = random.nextInt(10) - 5;
            shakeCounter++;
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
            isShake = false;
            shakeCounter = 0;
        }
        repaint();
    }

    private void advanceScene() {
        sceneIndex++;
        repaint();
    }

    private void playMusic(String path) {
        if (musicPlayer != null) {
            stopMusic();
        }
        try {
            musicPlayer = Manager.createPlayer(getClass().getResourceAsStream(path), "audio/mpeg");
            musicPlayer.realize();
            VolumeControl volumeControl = (VolumeControl) musicPlayer.getControl("VolumeControl");
            if (volumeControl != null) {
                volumeControl.setLevel(100); // Set volume level
            }
            musicPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playSound(String path) {
        if (soundPlayer != null) {
            stopSound();
        }
        try {
            soundPlayer = Manager.createPlayer(getClass().getResourceAsStream(path), "audio/x-wav");
            soundPlayer.realize();
            VolumeControl volumeControl = (VolumeControl) soundPlayer.getControl("VolumeControl");
            if (volumeControl != null) {
                volumeControl.setLevel(100); // Set volume level
            }
            soundPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                musicPlayer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            musicPlayer = null;
        }
    }

    private void stopSound() {
        if (soundPlayer != null) {
            try {
                soundPlayer.stop();
                soundPlayer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            soundPlayer = null;
        }
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        if (gameAction == FIRE && !isDissolve && !isShake) {
            isDissolve = true;
            repaint();
        }
    }
}


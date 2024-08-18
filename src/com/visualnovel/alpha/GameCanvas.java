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
    private Image currentBackground, nextBackground, textbox;
    private Hashtable images = new Hashtable(); // For storing images
    private Hashtable musics = new Hashtable(); // For storing music
    private Hashtable sounds = new Hashtable(); // For storing sound effects
    private String sceneText = "";
    private Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private int textX;
    private int textY;
    private boolean isDissolve = false;
    private boolean isShake = false;
    private boolean isFade = false;
    private int dissolveStep = 0;
    private int fadeStep = 0;
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
            nextBackground = (Image) images.get(imageName);
        } else if (command.startsWith("wt ")) {
            String effect = command.substring(3).trim();
            if (effect.equals("dissolve")) {
                isDissolve = true;
            } else if (effect.equals("shake")) {
                isShake = true;
                // Trigger vibration when shake effect is used
                Display.getDisplay().vibrate(200); // Vibrate for 200ms
            } else if (effect.equals("fade")) {
                isFade = true;
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
        } else if (isFade) {
            fadeEffect(g);
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

    private void fadeEffect(Graphics g) {
        // Basic fade effect implementation without alpha support
        if (fadeStep < 10) { // Approximate number of steps for the fade effect
            if (nextBackground != null) {
                // Gradually replace pixels of the current background with the next one
                int alpha = (fadeStep * 255) / 10; // Calculate transparency as a fraction of 255
                for (int y = 0; y < getHeight(); y += 2) {
                    for (int x = 0; x < getWidth(); x += 2) {
                        if (random.nextInt(10) < alpha / 25) { // Decide which image's pixel to show
                            g.drawImage(nextBackground, x, y, Graphics.TOP | Graphics.LEFT);
                        } else {
                            g.drawImage(currentBackground, x, y, Graphics.TOP | Graphics.LEFT);
                        }
                    }
                }
            }

            // Black screen transition effect to increase new image visibility
            g.setColor(0, 0, 0);
            int coverage = (255 - fadeStep * 25); // Adjust black cover to unveil the new image
            for (int i = 0; i < coverage; i++) {
                g.drawLine(random.nextInt(getWidth()), random.nextInt(getHeight()), random.nextInt(getWidth()), random.nextInt(getHeight()));
            }
            fadeStep++;
        } else {
            isFade = false;
            fadeStep = 0;
            currentBackground = nextBackground;
            nextBackground = null;
            advanceScene();
        }
        repaint();
    }

    private void shakeEffect() {
        ifзапущен эффект тряски (shake), положение фона будет смещаться случайным образом в небольшом диапазоне, что создаёт эффект тряски. Также, когда этот эффект активирован, будет запускаться вибрация устройства для усиления эффекта.

Вот продолжение и окончание кода:

```java
    private void shakeEffect() {
        if (shakeCounter < 10) { // Number of shakes
            shakeOffsetX = random.nextInt(10) - 5; // Shake horizontally
            shakeOffsetY = random.nextInt(10) - 5; // Shake vertically
            shakeCounter++;
        } else {
            isShake = false;
            shakeCounter = 0;
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }
        repaint(); // Repaint the screen to apply the shake effect
    }

    private void advanceScene() {
        if (nextBackground != null) {
            currentBackground = nextBackground;
            nextBackground = null;
        }
    }

    private void playMusic(String musicPath) {
        stopMusic();
        try {
            InputStream is = getClass().getResourceAsStream(musicPath);
            musicPlayer = Manager.createPlayer(is, "audio/mpeg");
            musicPlayer.setLoopCount(-1); // Loop music indefinitely
            musicPlayer.realize();
            VolumeControl vc = (VolumeControl) musicPlayer.getControl("VolumeControl");
            if (vc != null) {
                vc.setLevel(100); // Set volume to maximum
            }
            musicPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.close();
            musicPlayer = null;
        }
    }

    private void playSound(String soundPath) {
        try {
            InputStream is = getClass().getResourceAsStream(soundPath);
            soundPlayer = Manager.createPlayer(is, "audio/mpeg");
            soundPlayer.realize();
            VolumeControl vc = (VolumeControl) soundPlayer.getControl("VolumeControl");
            if (vc != null) {
                vc.setLevel(100); // Set volume to maximum
            }
            soundPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void keyPressed(int keyCode) {
        // Implement interaction logic (e.g., advancing scenes on key press)
        advanceScene();
    }

    protected void pointerPressed(int x, int y) {
        // Implement interaction logic for touch input if necessary
        advanceScene();
    }
}


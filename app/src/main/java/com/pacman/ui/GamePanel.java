package com.pacman.ui;

import com.pacman.logic.PelletSystem;
import com.pacman.model.TileSet;
import com.pacman.model.TileType;
import com.pacman.model.Map;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

   // --- Constants ---
    public static final int TILE_SIZE = 8;
    public static final int COLS = 28;
    public static final int ROWS = 36;
    public static final int SCALE = 2; // upscale for visibility
    public static final int SCREEN_WIDTH = TILE_SIZE * COLS;
    public static final int SCREEN_HEIGHT = TILE_SIZE * ROWS;
    public static final int FPS = 60;
    private static final int HUD_HEIGHT = 12;

    // --- Game objects ---
    private Thread gameThread;
    private boolean running = false;

    private Map map;
    private TileSet tileSet;

    // --- HUD / score ---
    private int score = 0;
    private int pelletsLeft = 0;
    private int lives = 3;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH * SCALE, SCREEN_HEIGHT * SCALE));
        setBackground(Color.BLACK);

        tileSet = new TileSet();
        map = new Map(TILE_SIZE);

        recountPellets();

        setFocusable(true);
    }

      public void startGame() {
        if (gameThread == null || !running) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

     @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                repaint(); 
                delta--;
            }
        }
    }

    /** Paints the map using the TileSet PNGs */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.scale(SCALE, SCALE);

        map.draw(g2, tileSet);

        g2.setColor(Color.WHITE);
        int hudY = SCREEN_HEIGHT + 9; // one text line under the map (pre-scale)
        g2.drawString("Score: " + score + "   Lives: " + lives + "   Pellets: " + pelletsLeft, 4, hudY);

        g2.dispose();
    }

    private void recountPellets() {
        pelletsLeft = 0;
        // If Map has getWidth()/getHeight(), use those; else keep COLS/ROWS.
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (map.getTile(x, y) == TileType.PELLET) {
                    pelletsLeft++;
                }
            }
        }
    }

    private int eatPelletAt(int cx, int cy) {
        if (map.getTile(cx, cy) == TileType.PELLET) {
            map.setTile(cx, cy, TileType.EMPTY);
            pelletsLeft--;
            score += 10;
            return 10;
        }
        return 0;
    }


}

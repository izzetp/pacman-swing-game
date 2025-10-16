package com.pacman.ui;

import com.pacman.model.Map;
import com.pacman.model.TileSet;
import com.pacman.model.TileType;
import com.pacman.model.TilemapLoader;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;           // <-- add
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {

    private static final int TILE_SIZE = 16;

    private Map map;
    private TileSet tileSet;

    private static final int COLS = 28;
    private static final int ROWS = 31;

    // --- ADD: simple Swing timer for a repaint "game loop"
    private Timer gameTimer;        // <-- add

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setDoubleBuffered(true);

        tileSet = new TileSet();

        BufferedImage img = tryLoadDefaultPngTilemap();
        if (img != null) {
            map = new Map(TilemapLoader.fromImage(img), TILE_SIZE);
        } else {
            map = new Map(generateProceduralMaze(COLS, ROWS), TILE_SIZE);
        }
    }

    // --- ADD: called by Main.java to start the loop
    public void startGame() {
        if (gameTimer != null && gameTimer.isRunning()) return;
        // ~60 FPS repaint loop for now. Later, call update systems here too.
        gameTimer = new Timer(16, e -> {
            // TODO: call update systems (movement, collisions, timers) once they exist
            repaint();
        });
        gameTimer.start();
    }

    // --- Optional helper if you ever need to stop the loop
    public void stopGame() {
        if (gameTimer != null) gameTimer.stop();
    }

    private BufferedImage tryLoadDefaultPngTilemap() {
        try {
            return ImageIO.read(getClass().getResource("/maps/level1.png"));
        } catch (Exception e) {
            return null;
        }
    }

    private TileType[][] generateProceduralMaze(int cols, int rows) {
        TileType[][] g = new TileType[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) g[r][c] = TileType.PACDOT;
        }
        for (int c = 0; c < cols; c++) { g[0][c] = TileType.WALL; g[rows-1][c] = TileType.WALL; }
        for (int r = 0; r < rows; r++) { g[r][0] = TileType.WALL; g[r][cols-1] = TileType.WALL; }
        for (int c = 4; c < cols - 4; c += 4) {
            for (int r = 2; r < rows - 2; r++) {
                if (r % 6 == 0) continue;
                g[r][c] = TileType.WALL;
            }
        }
        for (int r = 6; r < rows - 6; r += 6) {
            for (int c = 2; c < cols - 2; c++) {
                if (c % 6 == 0) continue;
                g[r][c] = TileType.WALL;
            }
        }
        int midC = cols / 2, midR = rows / 2;
        g[midR][midC - 1] = TileType.GHOST_GATE;
        g[midR][midC]     = TileType.GHOST_GATE;
        g[midR][midC + 1] = TileType.GHOST_GATE;
        for (int r = midR + 1; r <= midR + 3; r++) {
            for (int c = midC - 2; c <= midC + 2; c++) g[r][c] = TileType.EMPTY;
        }
        g[1][1] = TileType.POWER_PACDOT;
        g[1][cols - 2] = TileType.POWER_PACDOT;
        g[rows - 2][1] = TileType.POWER_PACDOT;
        g[rows - 2][cols - 2] = TileType.POWER_PACDOT;
        return g;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (map == null || tileSet == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            map.draw(g2, tileSet);
        } finally {
            g2.dispose();
        }
    }
}

package com.pacman.ui;

import com.pacman.model.Map;
import com.pacman.model.MapLoader;
import com.pacman.model.TileSet;
import com.pacman.model.TileType;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private static final int TILE_SIZE = 16;

    private Map map;
    private TileSet tileSet;

    private static final int COLS = 28;
    private static final int ROWS = 31;

    private Timer gameTimer;

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setDoubleBuffered(true);

        tileSet = new TileSet();

        Map txtMap = tryLoadTxtMap("/maps/original_pacman.txt");
        if (txtMap != null) {
            map = txtMap;
        } else {
            map = new Map(generateProceduralMaze(COLS, ROWS), TILE_SIZE);
        }
    }

    public void startGame() {
        if (gameTimer != null && gameTimer.isRunning()) return;

        gameTimer = new Timer(16, e -> {
            // TODO: call movement & collision updates here
            repaint();
        });
        gameTimer.start();
    }

    public void stopGame() {
        if (gameTimer != null) gameTimer.stop();
    }

    private Map tryLoadTxtMap(String resourcePath) {
        try {
            // Convert resource to a file path
            String path = getClass().getResource(resourcePath).getPath();
            return MapLoader.fromTextFile(path, TILE_SIZE);
        } catch (Exception e) {
            System.err.println("Failed to load TXT map: " + e.getMessage());
            return null;
        }
    }

    private TileType[][] generateProceduralMaze(int cols, int rows) {
        TileType[][] g = new TileType[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                g[r][c] = TileType.PACDOT;

        for (int c = 0; c < cols; c++) { g[0][c] = TileType.WALL; g[rows-1][c] = TileType.WALL; }
        for (int r = 0; r < rows; r++) { g[r][0] = TileType.WALL; g[r][cols-1] = TileType.WALL; }

        int midC = cols / 2, midR = rows / 2;
        g[midR][midC - 1] = TileType.GHOST_GATE;
        g[midR][midC] = TileType.GHOST_GATE;
        g[midR][midC + 1] = TileType.GHOST_GATE;

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

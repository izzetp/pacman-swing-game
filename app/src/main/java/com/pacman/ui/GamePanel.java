package com.pacman.ui;

import com.pacman.logic.*;
import com.pacman.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private static final int TILE_SIZE = 16;
    private static final int COLS = 28;
    private static final int ROWS = 29;

    private final TileSet tileSet = new TileSet();
    private Map map;
    private GameSession session;
    private MovementSystem player;
    private List<Ghost> ghosts = new ArrayList<>();
    private final Score score = new Score();

    private final GameClock fixedClock = () -> 1.0 / 60.0;
    private Timer gameTimer;

    private FrightenedTimer frightenedTimer;

    private final int ghostRow = 14;
    private final int ghostStartCol = 12;

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        map = tryLoadTxtMap("/maps/original_pacman.txt");
        if (map == null) map = new Map(generateProceduralMaze(COLS, ROWS), TILE_SIZE);

        // Session spawn coordinates
        session = new GameSession(map, ghostStartCol, ghostRow, 13, 21);

        // Player setup
        player = new MovementSystem(map, 8.0);
        player.setPosition(session.playerSpawnTileX(), session.playerSpawnTileY());

        // Ghosts setup
        ghosts = new ArrayList<>();
        Ghost blinky = new Ghost(map, 6.0, ghostStartCol, ghostRow);
        Ghost pinky  = new Ghost(map, 6.0, ghostStartCol + 1, ghostRow);
        Ghost inky   = new Ghost(map, 6.0, ghostStartCol + 2, ghostRow);
        Ghost clyde  = new Ghost(map, 6.0, ghostStartCol + 3, ghostRow);
        ghosts.add(blinky);
        ghosts.add(pinky);
        ghosts.add(inky);
        ghosts.add(clyde);

        frightenedTimer = new FrightenedTimer(ghosts);

        setupInput();
    }

    private void setupInput() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "start");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "moveUp");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "restart");

        getActionMap().put("start", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { session.start(); }
        });
        getActionMap().put("moveUp", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { player.request(Direction.UP); }
        });
        getActionMap().put("moveDown", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { player.request(Direction.DOWN); }
        });
        getActionMap().put("moveLeft", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { player.request(Direction.LEFT); }
        });
        getActionMap().put("moveRight", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { player.request(Direction.RIGHT); }
        });
        getActionMap().put("restart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (session.state() == GameSession.State.GAME_OVER || session.state() == GameSession.State.WIN) {
                session.restart();
                resetPositions();
        }
    }
});

    }

    public void startGame() {
        Thread gameLoop = new Thread(() -> {
            long lastTime = System.nanoTime();
            final double nsPerUpdate = 1_000_000_000.0 / 120.0; 

            while (true) {
               long now = System.nanoTime();
                double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (session.state() == GameSession.State.PLAYING) {
                    updateGameLogic(deltaSeconds);
                }

                repaint();

                try {
                    Thread.sleep(2); // reduce CPU usage
                } catch (InterruptedException ignored) {}
            }
        });
        gameLoop.setDaemon(true);
        gameLoop.start();
        requestFocusInWindow();
    }   

    private void updateGameLogic(double deltaSeconds) {
        if (session.state() != GameSession.State.PLAYING) return;

        player.tick(() -> deltaSeconds);

        int gained = PlayerPickupSystem.eatAt(map, player.tileX(), player.tileY());
        if (gained > 0) {
            score.add(gained);
            if (gained == 50) frightenedTimer.start(7.0);
        }

        frightenedTimer.tick(() -> deltaSeconds);

        for (Ghost g : ghosts) {
            g.updateTarget(player.tileX(), player.tileY());
            g.tick(() -> deltaSeconds);
        }   

        int livesBefore = session.lives();
        boolean hit = CollisionSystem.checkCollisions(session, player, ghosts, score, frightenedTimer);

        if (hit && session.lives() < livesBefore && session.state() == GameSession.State.PLAYING) {
            resetPositions();
            frightenedTimer.cancel();
        }

        if (countPellets(map) == 0) {
            session.win();
            resetPositions();
        }
    }


    /** Reset player and ghosts safely to spawn positions */
    private void resetPositions() {
        // Player respawn
        player.setToTileCenter(session.playerSpawnTileX(), session.playerSpawnTileY());

        // Ghost respawn
        int c = ghostStartCol;
        int r = ghostRow;
        if (ghosts.size() > 0) ghosts.get(0).setPosition(c, r);
        if (ghosts.size() > 1) ghosts.get(1).setPosition(c + 1, r);
        if (ghosts.size() > 2) ghosts.get(2).setPosition(c + 2, r);
        if (ghosts.size() > 3) ghosts.get(3).setPosition(c + 3, r);
        for (Ghost g : ghosts) g.setMode(Ghost.Mode.SCATTER);
    }

    private int countPellets(Map m) {
        int n = 0;
        for (int r = 0; r < m.rows(); r++)
            for (int c = 0; c < m.cols(); c++) {
                TileType t = m.getTile(r, c);
                if (t == TileType.PACDOT || t == TileType.POWER_PACDOT) n++;
            }
        return n;
    }

    private Map tryLoadTxtMap(String resourcePath) {
        try {
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
            for (int c = 0; c < cols; c++) g[r][c] = TileType.PACDOT;

        for (int c = 0; c < cols; c++) { g[0][c] = TileType.WALL; g[rows-1][c] = TileType.WALL; }
        for (int r = 0; r < rows; r++) { g[r][0] = TileType.WALL; g[r][cols-1] = TileType.WALL; }

        int midC = cols/2, midR = rows/2;
        g[midR][midC-1] = TileType.GHOST_GATE;
        g[midR][midC] = TileType.GHOST_GATE;
        g[midR][midC+1] = TileType.GHOST_GATE;

        g[1][1] = TileType.POWER_PACDOT;
        g[1][cols-2] = TileType.POWER_PACDOT;
        g[rows-2][1] = TileType.POWER_PACDOT;
        g[rows-2][cols-2] = TileType.POWER_PACDOT;

        return g;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (map == null || tileSet == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            map.draw(g2, tileSet);

            // Draw player
            g2.setColor(Color.GRAY);
            g2.fillOval(player.tileX() * TILE_SIZE, player.tileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            // Draw ghosts
            int idx = 0;
            for (Ghost ghost : ghosts) {
                switch (idx) {
                    case 0 -> g2.setColor(Color.RED);
                    case 1 -> g2.setColor(Color.PINK);
                    case 2 -> g2.setColor(Color.CYAN);
                    case 3 -> g2.setColor(new Color(255,165,0));
                    default -> g2.setColor(Color.WHITE);
                }
                if (ghost.mode() == Ghost.Mode.FRIGHTENED) g2.setColor(Color.BLUE);
                g2.fillOval(ghost.tileX() * TILE_SIZE, ghost.tileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                idx++;
            }

            // HUD
            g2.setColor(Color.WHITE);
            g2.drawString("Score: " + score.value(), 8, 14);
            g2.drawString("Lives: " + session.lives(), 8, 28);
            g2.drawString("Blue: " + (frightenedTimer.active() ? "ON" : "OFF"), 8, 42);
            g2.drawString("Timer: " + (int)Math.ceil(frightenedTimer.secondsLeft()), 8, 56);

            // Menu, game over and Win 
            if (session.state() == GameSession.State.MENU) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
                g2.drawString("PAC-MAN", getWidth()/2 - 48, getHeight()/2 - 20);
                g2.drawString("Press ENTER to start", getWidth()/2 - 90, getHeight()/2 + 10);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
                g2.drawString("Use arrow keys to move", getWidth()/2 - 60, getHeight()/2 + 30);

            } else if (session.state() == GameSession.State.GAME_OVER) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
                g2.drawString("GAME OVER", getWidth()/2 - 60, getHeight()/2 - 10); 
                g2.drawString("Press SPACE to play again", getWidth()/2 - 100, getHeight()/2 + 20);
            } else if (session.state() == GameSession.State.WIN) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
                g2.setColor(Color.YELLOW);
                g2.drawString("CONGRATULATIONS! YOU WON!", getWidth()/2 - 180, getHeight()/2 - 20);

                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
                g2.setColor(Color.WHITE);
                g2.drawString("Score: " + score.value(), getWidth()/2 - 50, getHeight()/2 + 10);
                g2.drawString("Lives Remaining: " + session.lives(), getWidth()/2 - 80, getHeight()/2 + 40);
            }
        } finally {
            g2.dispose();
        }
    }
}

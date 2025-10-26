package com.pacman.ui;

import com.pacman.logic.CollisionSystem;
import com.pacman.logic.FrightenedTimer;
import com.pacman.logic.GameClock;
import com.pacman.logic.GameSession;
import com.pacman.logic.Ghost;
import com.pacman.logic.MovementSystem;
import com.pacman.logic.PlayerPickupSystem;
import com.pacman.model.Direction;
import com.pacman.model.Map;
import com.pacman.model.MapLoader;
import com.pacman.model.Score;
import com.pacman.model.TileSet;
import com.pacman.model.TileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private static final int TILE_SIZE = 16;
    private static final int COLS = 28;
    private static final int ROWS = 31;

    private final TileSet tileSet = new TileSet();
    private Map map;
    private GameSession session;
    private MovementSystem player;
    private List<Ghost> ghosts = new ArrayList<>();
    private final Score score = new Score();

    private final GameClock fixedClock = () -> 1.0 / 60.0;
    private Timer gameTimer;

    private FrightenedTimer frightenedTimer;

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        Map txtMap = tryLoadTxtMap("/maps/original_pacman.txt");
        map = (txtMap != null) ? txtMap : new Map(generateProceduralMaze(COLS, ROWS), TILE_SIZE);

        session = new GameSession(map, 13, 23);
        session.start();

        player = new MovementSystem(map, 8.0);
        player.setPosition(13, 21);

        ghosts = new ArrayList<>();
        int ghostRow = 14;
        int ghostStartCol = 12;

        Ghost blinky = new Ghost(map, 6.0);
        blinky.setPosition(ghostStartCol, ghostRow);
        blinky.setMode(Ghost.Mode.SCATTER);

        Ghost pinky = new Ghost(map, 6.0);
        pinky.setPosition(ghostStartCol + 2, ghostRow);
        pinky.setMode(Ghost.Mode.SCATTER);

        Ghost inky = new Ghost(map, 6.0);
        inky.setPosition(ghostStartCol + 4, ghostRow);
        inky.setMode(Ghost.Mode.SCATTER);

        Ghost clyde = new Ghost(map, 6.0);
        clyde.setPosition(ghostStartCol + 6, ghostRow);
        clyde.setMode(Ghost.Mode.SCATTER);

        ghosts.add(blinky);
        ghosts.add(pinky);
        ghosts.add(inky);
        ghosts.add(clyde);

        frightenedTimer = new FrightenedTimer(ghosts);

        setupInput();
    }

    private void setupInput() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "moveUp");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "restart");

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
            @Override public void actionPerformed(ActionEvent e) { session.restart(); }
        });
    }

    public void startGame() {
        if (gameTimer != null && gameTimer.isRunning()) return;

        gameTimer = new Timer(16, e -> {
            if (session.state() == GameSession.State.PLAYING) {
                updateGameLogic();
            }
            repaint();
        });
        gameTimer.start();
    }

    private void updateGameLogic() {
        if (session.state() != GameSession.State.PLAYING) return;

        player.tick(fixedClock);

        int gained = PlayerPickupSystem.eatAt(map, player.tileX(), player.tileY());
        if (gained > 0) {
            score.add(gained);
            if (gained == 50) frightenedTimer.start(7.0);
        }

        frightenedTimer.tick(fixedClock);

        for (Ghost g : ghosts) {
            g.updateTarget(player.tileX(), player.tileY());
            g.tick(fixedClock);
        }

        CollisionSystem.checkCollisions(session, player, ghosts, score);

        if (countPellets(map) == 0) {
            while (session.lives() > 0) session.loseLife();
        }
    }


    private int countPellets(Map m) {
        int n = 0;
        for (int r = 0; r < m.rows(); r++) {
            for (int c = 0; c < m.cols(); c++) {
                TileType t = m.getTile(r, c);
                if (t == TileType.PACDOT || t == TileType.POWER_PACDOT) n++;
            }
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
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) g[r][c] = TileType.PACDOT;
        }
        for (int c = 0; c < cols; c++) { g[0][c] = TileType.WALL; g[rows - 1][c] = TileType.WALL; }
        for (int r = 0; r < rows; r++) { g[r][0] = TileType.WALL; g[r][cols - 1] = TileType.WALL; }

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

            g2.setColor(Color.GRAY);
            g2.fillOval(player.tileX() * TILE_SIZE, player.tileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            int idx = 0;
            for (Ghost ghost : ghosts) {
                switch (idx) {
                    case 0 -> g2.setColor(Color.RED);
                    case 1 -> g2.setColor(Color.PINK);
                    case 2 -> g2.setColor(Color.CYAN);
                    case 3 -> g2.setColor(new Color(255, 165, 0));
                    default -> g2.setColor(Color.WHITE);
                }
                if (ghost.mode() == Ghost.Mode.FRIGHTENED) g2.setColor(Color.BLUE);
                g2.fillOval(ghost.tileX() * TILE_SIZE, ghost.tileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                idx++;
            }

            g2.setColor(Color.WHITE);
            g2.drawString("Score: " + score.value(), 8, 14);
            g2.drawString("Lives: " + session.lives(), 8, 28);
            g2.drawString("Blue: " + (frightenedTimer.active() ? "ON" : "OFF"), 8, 42);

            if (session.state() == GameSession.State.GAME_OVER) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
                g2.drawString("GAME OVER - Press SPACE", getWidth() / 4, getHeight() / 2);
            }
        } finally {
            g2.dispose();
        }
    }
}

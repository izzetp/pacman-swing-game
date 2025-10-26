package com.pacman.ui;

import com.pacman.logic.*;
import com.pacman.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * GamePanel with Pac-Man and ghost spawns according to classic layout.
 */
public class GamePanel extends JPanel {

    private static final int TILE_SIZE = 16;
    private static final int COLS = 28;
    private static final int ROWS = 31;

    private final TileSet tileSet = new TileSet();
    private Map map;
    private GameSession session;
    private MovementSystem player;
    private List<Ghost> ghosts = new ArrayList<>();
    private Score score = new Score();

    private Timer gameTimer;
    private final GameClock fixedClock = () -> 1.0 / 60.0; // ~60 FPS

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        // --- Load map ---
        Map txtMap = tryLoadTxtMap("/maps/original_pacman.txt");
        map = (txtMap != null) ? txtMap : new Map(generateProceduralMaze(COLS, ROWS), TILE_SIZE);

        // --- Initialize session ---
        session = new GameSession(map, 13, 23); // Pac-Man spawn
        session.start();

        // --- Player setup ---
        player = new MovementSystem(map, 8.0);
        player.setPosition(13, 21); // Pac-Man spawn

        // --- Ghost setup (center pen) ---
        ghosts = new ArrayList<>();
        int ghostRow = 14;
        int ghostStartCol = 12;

        Ghost blinky = new Ghost(map, 6.0);
        blinky.setPosition(ghostStartCol, ghostRow);
        blinky.setMode(Ghost.Mode.CHASE);

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

        // --- Keyboard bindings ---
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
        // --- Update player ---
        player.tick(fixedClock);

        // --- Eat pellets / power pellets ---
        int points = PlayerPickupSystem.eatAt(map, player.tileX(), player.tileY());
        if (points > 0) {
            score.add(points);
            if (points == 50) {
                // Power pellet: ghosts frightened
                for (Ghost g : ghosts) g.setMode(Ghost.Mode.FRIGHTENED);
            }
        }

        // --- Update ghosts ---
        for (Ghost g : ghosts) {
            g.updateTarget(player.tileX(), player.tileY());
            g.tick(fixedClock);
        }

        // --- Collisions ---
        CollisionSystem.checkCollisions(session, player, ghosts);

        // --- Win condition ---
        if (map.countPellets() == 0) {
            session.loseLife(); // placeholder for level complete
        }
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

            // --- Draw player ---
            g2.setColor(Color.GRAY);
            g2.fillOval(player.tileX() * TILE_SIZE, player.tileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            // --- Draw ghosts with unique colors ---
            int idx = 0;
            for (Ghost ghost : ghosts) {
                switch (idx) {
                    case 0 -> g2.setColor(Color.RED);          // Blinky
                    case 1 -> g2.setColor(Color.PINK);         // Pinky
                    case 2 -> g2.setColor(Color.CYAN);         // Inky
                    case 3 -> g2.setColor(new Color(255,165,0)); // Clyde
                }
                if (ghost.mode() == Ghost.Mode.FRIGHTENED) g2.setColor(Color.BLUE);
                g2.fillOval(ghost.tileX() * TILE_SIZE, ghost.tileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                idx++;
            }

            // --- HUD ---
            g2.setColor(Color.WHITE);
            g2.drawString("Score: " + score.value(), 8, 14);
            g2.drawString("Lives: " + session.lives(), 8, 28);
            if (session.state() == GameSession.State.GAME_OVER) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
                g2.drawString("GAME OVER - Press SPACE", getWidth()/4, getHeight()/2);
            }
        } finally {
            g2.dispose();
        }
    }
}

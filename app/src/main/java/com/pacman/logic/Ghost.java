package com.pacman.logic;

import com.pacman.model.Direction;
import com.pacman.model.Map;

import java.util.EnumSet;
import java.util.Random;

/**
 * Ghost logic for movement and basic movement modes.
 * Works with MovementSystem and Map to obey walls.
 */
public class Ghost {

    public enum Mode {
        CHASE, SCATTER, FRIGHTENED
    }

    private final MovementSystem movement;
    private final Map map;
    private final Random rng = new Random();
    private Mode mode = Mode.SCATTER;

    // Target position (used in chase or scatter)
    private int targetX;
    private int targetY;

    public Ghost(Map map, double speedTilesPerSec) {
        this.map = map;
        this.movement = new MovementSystem(map, speedTilesPerSec);
    }

    // --- Public API used by tests and gameplay ---

    public void setPosition(int tileX, int tileY) {
        movement.setPosition(tileX, tileY);
    }

    public void setDirection(Direction dir) {
        movement.request(dir);
        movement.tick(new FixedClock(0)); // commit request immediately
    }

    public void requestDirection(Direction dir) {
        movement.request(dir);
    }

    public Direction currentDirection() {
        // There's no direct getter, but we can add a small helper below.
        return movementDirection();
    }

    public int tileX() {
        return movement.tileX();
    }

    public int tileY() {
        return movement.tileY();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode mode() {
        return mode;
    }

    public void updateTarget(int tileX, int tileY) {
        this.targetX = tileX;
        this.targetY = tileY;
    }

    /**
     * Called every frame to update movement.
     */
    public void tick(GameClock clock) {
        switch (mode) {
            case FRIGHTENED -> handleFrightened();
            case CHASE -> handleChase();
            case SCATTER -> handleScatter();
        }
        movement.tick(clock);
    }

    // --- Internal helpers ---

    private void handleChase() {
        // Move roughly toward targetX, targetY using Manhattan distance
        Direction best = chooseBestDirectionToward(targetX, targetY);
        if (best != null) movement.request(best);
    }

    private void handleScatter() {
        // In scatter, just idle or circle — here we’ll default to previous direction
        // so ghosts still move in the map if possible.
        if (movementDirection() == Direction.NONE) {
            // pick a random valid direction if stopped
            movement.request(randomWalkableDirection());
        }
    }

    private void handleFrightened() {
        // Pick a random direction each tick (simple random AI)
        movement.request(randomWalkableDirection());
    }

    private Direction chooseBestDirectionToward(int tx, int ty) {
        int cx = movement.tileX();
        int cy = movement.tileY();

        Direction bestDir = null;
        double bestDist = Double.MAX_VALUE;

        for (Direction d : EnumSet.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)) {
            int nx = nextX(cx, d);
            int ny = nextY(cy, d);
            if (!map.isWalkable(nx, ny)) continue;
            double dist = Math.hypot(tx - nx, ty - ny);
            if (dist < bestDist) {
                bestDist = dist;
                bestDir = d;
            }
        }
        return bestDir;
    }

    private Direction randomWalkableDirection() {
        Direction[] dirs = Direction.values();
        for (int i = 0; i < 10; i++) {
            Direction d = dirs[rng.nextInt(dirs.length)];
            int nx = nextX(movement.tileX(), d);
            int ny = nextY(movement.tileY(), d);
            if (map.isWalkable(nx, ny)) return d;
        }
        return Direction.NONE;
    }

    private int nextX(int x, Direction d) {
        return switch (d) {
            case LEFT -> x - 1;
            case RIGHT -> x + 1;
            default -> x;
        };
    }

    private int nextY(int y, Direction d) {
        return switch (d) {
            case UP -> y - 1;
            case DOWN -> y + 1;
            default -> y;
        };
    }

    private Direction movementDirection() {
        // Since MovementSystem hides current dir, we track it by requesting direction last tick.
        // For TDD simplicity, we’ll add a tiny extension below if needed.
        try {
            var dirField = MovementSystem.class.getDeclaredField("dir");
            dirField.setAccessible(true);
            return (Direction) dirField.get(movement);
        } catch (Exception e) {
            return Direction.NONE;
        }
    }

    // Helper clock for instantaneous commit
    private static class FixedClock implements GameClock {
        private final double dt;
        FixedClock(double dt) { this.dt = dt; }
        @Override public double deltaSeconds() { return dt; }
    }
}

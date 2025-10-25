package com.pacman.logic;

import com.pacman.model.Direction;
import com.pacman.model.Map;

public class MovementSystem {
    private final Map map;
    private final double speedTilesPerSec;

    private int tileX;
    private int tileY;
    private double offX;
    private double offY;
    private Direction dir = Direction.NONE;
    private Direction requested = Direction.NONE;

    public MovementSystem(Map map, double speedTilesPerSec) {
        this.map = map;
        this.speedTilesPerSec = speedTilesPerSec;
    }

    // Set player position to a tile and stop all movement
    public void setPosition(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.offX = 0;
        this.offY = 0;
        this.dir = Direction.NONE;
        this.requested = Direction.NONE;
    }

    // Same as setPosition, clearer when resetting after losing a life
    public void setToTileCenter(int tileX, int tileY) {
        setPosition(tileX, tileY);
    }

    // Queue a new direction
    public void request(Direction d) {
        this.requested = d;
    }

    public int tileX() {
        return tileX;
    }

    public int tileY() {
        return tileY;
    }

    private boolean alignedToCenter() {
        return Math.abs(offX) < 1e-9 && Math.abs(offY) < 1e-9;
    }

    private boolean canEnter(int cx, int cy) {
        return map.isWalkable(cx, cy);
    }

    private int nextX(Direction d) {
        return switch (d) {
            case LEFT -> tileX - 1;
            case RIGHT -> tileX + 1;
            default -> tileX;
        };
    }

    private int nextY(Direction d) {
        return switch (d) {
            case UP -> tileY - 1;
            case DOWN -> tileY + 1;
            default -> tileY;
        };
    }

    // Move player based on time and direction
    public void tick(GameClock clock) {
        double tilesToAdvance = speedTilesPerSec * clock.deltaSeconds();

        if (requested != Direction.NONE && alignedToCenter()) {
            int nx = nextX(requested);
            int ny = nextY(requested);
            if (canEnter(nx, ny)) {
                dir = requested;
            }
        }

        while (tilesToAdvance > 0) {
            if (dir == Direction.NONE) {
                break;
            }

            int nx = nextX(dir);
            int ny = nextY(dir);
            if (!canEnter(nx, ny)) {
                dir = Direction.NONE;
                offX = 0;
                offY = 0;
                break;
            }

            double remaining = 1.0 - (Math.abs(offX) + Math.abs(offY));
            double step = Math.min(tilesToAdvance, remaining);

            switch (dir) {
                case LEFT -> offX -= step;
                case RIGHT -> offX += step;
                case UP -> offY -= step;
                case DOWN -> offY += step;
                default -> { }
            }

            tilesToAdvance -= step;

            if (Math.abs(offX) >= 1.0 || Math.abs(offY) >= 1.0) {
                if (dir == Direction.LEFT) {
                    tileX--;
                }
                if (dir == Direction.RIGHT) {
                    tileX++;
                }
                if (dir == Direction.UP) {
                    tileY--;
                }
                if (dir == Direction.DOWN) {
                    tileY++;
                }
                offX = 0.0;
                offY = 0.0;

                if (requested != Direction.NONE) {
                    int tx = nextX(requested);
                    int ty = nextY(requested);
                    if (canEnter(tx, ty)) {
                        dir = requested;
                    }
                }
            }
        }
    }
}

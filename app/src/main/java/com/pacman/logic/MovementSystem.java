package com.pacman.logic;

import com.pacman.model.Direction;
import com.pacman.model.Map;

public class MovementSystem {
    private final Map map;
    private final double speedTilesPerSec;

    private int tileX, tileY;
    private double offX, offY;
    private Direction dir = Direction.NONE;
    private Direction requested = Direction.NONE;

    public MovementSystem(Map map, double speedTilesPerSec) {
        this.map = map;
        this.speedTilesPerSec = speedTilesPerSec;
    }

    public void setPosition(int tileX, int tileY) {
        this.tileX = tileX; this.tileY = tileY;
        this.offX = 0; this.offY = 0;
        this.dir = Direction.NONE; this.requested = Direction.NONE;
    }

    public void setToTileCenter(int tx, int ty) {
        this.tileX = tx; this.tileY = ty;
        this.offX = 0; this.offY = 0;
    }

    public void request(Direction d) { this.requested = d; }

    public int tileX() { return tileX; }
    public int tileY() { return tileY; }

    private boolean alignedToCenter() {
        return Math.abs(offX) < 1e-9 && Math.abs(offY) < 1e-9;
    }

    private boolean canEnter(int cx, int cy) {
        return map.isWalkable(cx, cy);
    }

    private int nextX(Direction d) {
        return switch(d) {
            case LEFT -> tileX - 1;
            case RIGHT -> tileX + 1;
            default -> tileX;
        };
    }

    private int nextY(Direction d) {
        return switch(d) {
            case UP -> tileY - 1;
            case DOWN -> tileY + 1;
            default -> tileY;
        };
    }

    public void tick(GameClock clock) {
        double tilesToAdvance = speedTilesPerSec * clock.deltaSeconds();

        if (requested != Direction.NONE && alignedToCenter()) {
            int nx = nextX(requested), ny = nextY(requested);
            if (canEnter(nx, ny)) dir = requested;
        }

        while (tilesToAdvance > 0) {
            if (dir == Direction.NONE) break;

            int nx = nextX(dir), ny = nextY(dir);
            if (!canEnter(nx, ny)) {
                dir = Direction.NONE;
                offX = offY = 0;
                break;
            }

            double remainingToCenter = 1.0 - (Math.abs(offX) + Math.abs(offY));
            double step = Math.min(tilesToAdvance, remainingToCenter);
            switch (dir) {
                case LEFT  -> offX -= step;
                case RIGHT -> offX += step;
                case UP    -> offY -= step;
                case DOWN  -> offY += step;
            }
            tilesToAdvance -= step;

            if (Math.abs(offX) >= 1.0 || Math.abs(offY) >= 1.0) {
                if (dir == Direction.LEFT)  tileX--;
                if (dir == Direction.RIGHT) tileX++;
                if (dir == Direction.UP)    tileY--;
                if (dir == Direction.DOWN)  tileY++;
                offX = offY = 0.0;

                if (tileX < 0) tileX = map.cols() - 1;
                if (tileX >= map.cols()) tileX = 0;

                if (requested != Direction.NONE) {
                    int tx = nextX(requested), ty = nextY(requested);
                    if (canEnter(tx, ty)) dir = requested;
                }
            }
        }
    }
}

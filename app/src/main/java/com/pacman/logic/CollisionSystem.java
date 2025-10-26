package com.pacman.logic;

import java.util.List;

public final class CollisionSystem {
    private CollisionSystem() {}

    public static boolean checkCollisions(GameSession session, MovementSystem player, List<Ghost> ghosts) {
        if (session.state() != GameSession.State.PLAYING) return false;

        int px = player.tileX();
        int py = player.tileY();

        boolean collisionOccurred = false;

        for (Ghost g : ghosts) {
            if (g.tileX() == px && g.tileY() == py) {
                collisionOccurred = true;
                handleCollision(session, g);
            }
        }
        return collisionOccurred;
    }

    private static void handleCollision(GameSession session, Ghost ghost) {
        switch (ghost.mode()) {
            case CHASE, SCATTER -> {
                // Player dies
                session.loseLife();
            }
            case FRIGHTENED -> {
                // Ghost is eaten — respawn at ghost gate
                int spawnX = session.spawnTileX();
                int spawnY = session.spawnTileY();
                ghost.setPosition(spawnX, spawnY);
                ghost.setMode(Ghost.Mode.SCATTER);
            }
        }
    }
}

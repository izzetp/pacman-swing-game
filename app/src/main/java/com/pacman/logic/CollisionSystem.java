package com.pacman.logic;

import java.util.List;
import com.pacman.model.Score;

public final class CollisionSystem {
    private CollisionSystem() { }

    public static boolean checkCollisions(GameSession session,
                                          MovementSystem player,
                                          List<Ghost> ghosts) {
        return checkCollisions(session, player, ghosts, null, null);
    }

    public static boolean checkCollisions(GameSession session,
                                          MovementSystem player,
                                          List<Ghost> ghosts,
                                          Score score) {
        return checkCollisions(session, player, ghosts, score, null);
    }

    public static boolean checkCollisions(GameSession session,
                                          MovementSystem player,
                                          List<Ghost> ghosts,
                                          Score score,
                                          FrightenedTimer frightenedTimer) {
        if (session.state() != GameSession.State.PLAYING) return false;

        int px = player.tileX();
        int py = player.tileY();

        for (Ghost g : ghosts) {
            if (g.tileX() == px && g.tileY() == py) {
                if (g.mode() == Ghost.Mode.FRIGHTENED) {
                    g.respawn(session.ghostSpawnTileX(), session.ghostSpawnTileY());
                    if (score != null) {
                        int add = frightenedTimer != null ? frightenedTimer.nextEatScore() : 200;
                        score.add(add);
                        if (frightenedTimer != null) frightenedTimer.onGhostEaten();
                    }
                    return true;
                } else {
                    // Player hit a non-frightened ghost
                    session.loseLife();
                    // **RESPAWN PLAYER at initial player spawn**
                    player.setToTileCenter(session.playerSpawnTileX(), session.playerSpawnTileY());
                    return true;
                }
            }
        }
        return false;
    }
}

package com.pacman.logic;

import java.util.List;
import com.pacman.model.Score;

public final class CollisionSystem {
    private CollisionSystem() { }

    public static boolean checkCollisions(GameSession session,
                                          MovementSystem player,
                                          List<Ghost> ghosts) {
        return checkCollisions(session, player, ghosts, null);
    }

    public static boolean checkCollisions(GameSession session,
                                          MovementSystem player,
                                          List<Ghost> ghosts,
                                          Score score) {
        if (session.state() != GameSession.State.PLAYING) {
            return false;
        }

        int px = player.tileX();
        int py = player.tileY();

        for (Ghost g : ghosts) {
            if (g.tileX() == px && g.tileY() == py) {
                Ghost.Mode mode = g.mode();
                if (mode == Ghost.Mode.FRIGHTENED) {
                    int sx = session.spawnTileX();
                    int sy = session.spawnTileY();
                    g.setPosition(sx, sy);
                    g.setMode(Ghost.Mode.SCATTER);
                    if (score != null) {
                        score.add(200);
                    }
                    return true;
                } else {
                    session.loseLife();
                    player.setToTileCenter(session.spawnTileX(), session.spawnTileY());
                    return true;
                }
            }
        }
        return false;
    }
}

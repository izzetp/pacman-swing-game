package com.pacman;

import com.pacman.logic.CollisionSystem;
import com.pacman.logic.GameSession;
import com.pacman.logic.Ghost;
import com.pacman.logic.MovementSystem;
import com.pacman.model.Map;
import com.pacman.model.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollisionSystemTest {

    private Map map;
    private GameSession session;
    private MovementSystem player;
    private Ghost ghost;

    @BeforeEach
    void setUp() {
        // simple 5x5 map, all walkable
        TileType[][] grid = new TileType[5][5];
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                grid[r][c] = TileType.EMPTY;

        map = new Map(grid, 16);

        // GameSession 
        session = new GameSession(map, 0, 0, 2, 2);
        session.start();

        // Player setup at its spawn
        player = new MovementSystem(map, 1.0);
        player.setPosition(session.playerSpawnTileX(), session.playerSpawnTileY());

        // Ghost setup (we'll move it in tests if needed)
        ghost = new Ghost(map, 1.0, session.ghostSpawnTileX(), session.ghostSpawnTileY());
    }

    @Test
    void testCollisionWhileChaseModeCausesLifeLoss() {
        int initialLives = session.lives();

        ghost.setMode(Ghost.Mode.CHASE);
        ghost.setPosition(player.tileX(), player.tileY()); // move ghost to player tile

        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertTrue(collided, "Collision should be detected");
        assertEquals(initialLives - 1, session.lives(), "Player should lose one life");
    }

    @Test
    void testCollisionWhileScatterModeCausesLifeLoss() {
        int initialLives = session.lives();

        ghost.setMode(Ghost.Mode.SCATTER);
        ghost.setPosition(player.tileX(), player.tileY()); // move ghost to player tile

        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertTrue(collided, "Collision should be detected");
        assertEquals(initialLives - 1, session.lives(), "Player should lose one life");
    }

    @Test
    void testCollisionWhileFrightenedRespawnsGhostAndNoLifeLoss() {
        int initialLives = session.lives();

        ghost.setMode(Ghost.Mode.FRIGHTENED);
        ghost.setPosition(player.tileX(), player.tileY()); // move ghost to player tile

        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertTrue(collided, "Collision should be detected");
        assertEquals(initialLives, session.lives(), "Player should NOT lose a life when ghost is frightened");

        // Ghost should respawn at its own spawn position now (0,0)
        assertEquals(session.ghostSpawnTileX(), ghost.tileX(), "Ghost should respawn at its spawn X");
        assertEquals(session.ghostSpawnTileY(), ghost.tileY(), "Ghost should respawn at its spawn Y");
        assertEquals(Ghost.Mode.SCATTER, ghost.mode(), "Ghost should return to scatter mode after being eaten");
        assertTrue(ghost.isWaitingToMove(), "Ghost should be waiting before moving again");
    }

    @Test
    void testNoCollisionWhenDifferentTiles() {
        ghost.setPosition(0, 0); // far away from player
        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertFalse(collided, "No collision expected when not on same tile");
        assertEquals(3, session.lives(), "Lives should remain unchanged");
    }

    @Test
    void testNoCollisionWhenGameOver() {
        // simulate player already dead
        while (session.lives() > 0) session.loseLife();
        assertEquals(GameSession.State.GAME_OVER, session.state());

        ghost.setMode(Ghost.Mode.CHASE);
        ghost.setPosition(player.tileX(), player.tileY()); // even if on player tile

        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertFalse(collided, "No collision should occur when game is over");
    }
}

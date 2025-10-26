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
        session = new GameSession(map, 1, 1);
        session.start();

        player = new MovementSystem(map, 1.0);
        player.setPosition(2, 2);

        ghost = new Ghost(map, 1.0);
        ghost.setPosition(2, 2); // initially same tile for collision
    }

    @Test
    void testCollisionWhileChaseModeCausesLifeLoss() {
        int initialLives = session.lives();

        ghost.setMode(Ghost.Mode.CHASE);
        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertTrue(collided, "Collision should be detected");
        assertEquals(initialLives - 1, session.lives(), "Player should lose one life");
    }

    @Test
    void testCollisionWhileScatterModeCausesLifeLoss() {
        int initialLives = session.lives();

        ghost.setMode(Ghost.Mode.SCATTER);
        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertTrue(collided);
        assertEquals(initialLives - 1, session.lives());
    }

    @Test
    void testCollisionWhileFrightenedRespawnsGhostAndNoLifeLoss() {
        int initialLives = session.lives();

        ghost.setMode(Ghost.Mode.FRIGHTENED);
        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertTrue(collided, "Collision should be detected");
        assertEquals(initialLives, session.lives(), "Player should NOT lose a life when ghost is frightened");

        // Ghost should respawn at session spawn position
        assertEquals(session.spawnTileX(), ghost.tileX(), "Ghost should respawn at spawn X");
        assertEquals(session.spawnTileY(), ghost.tileY(), "Ghost should respawn at spawn Y");
        assertEquals(Ghost.Mode.SCATTER, ghost.mode(), "Ghost should return to scatter mode after being eaten");
    }

    @Test
    void testNoCollisionWhenDifferentTiles() {
        ghost.setPosition(0, 0); // far away
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
        boolean collided = CollisionSystem.checkCollisions(session, player, List.of(ghost));

        assertFalse(collided, "No collision should occur when game is over");
    }
}

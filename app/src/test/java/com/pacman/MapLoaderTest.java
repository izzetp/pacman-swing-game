package com.pacman;

import com.pacman.model.Map;
import com.pacman.model.MapLoader;
import com.pacman.model.TileType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MapLoaderTest {

    @Test
    void loadsTextMapWithWallsDotsAndPower() throws Exception {
        String txt =
                "#.#\n" +
                "#o#\n";
        Path tmp = Files.createTempFile("map_", ".txt");
        Files.writeString(tmp, txt, StandardCharsets.UTF_8);

        Map map = MapLoader.fromTextFile(tmp.toString(), 16);

        assertEquals(2, map.rows());
        assertEquals(3, map.cols());

        // row 0: # . #
        assertEquals(TileType.WALL, map.getTile(0, 0));
        assertEquals(TileType.PACDOT, map.getTile(0, 1));
        assertEquals(TileType.WALL, map.getTile(0, 2));

        // row 1: # o #
        assertEquals(TileType.WALL, map.getTile(1, 0));
        assertEquals(TileType.POWER_PACDOT, map.getTile(1, 1));
        assertEquals(TileType.WALL, map.getTile(1, 2));
    }

    @Test
    void tileSizeIsStored() throws Exception {
        String txt = "###\n###\n";
        Path tmp = Files.createTempFile("map_size_", ".txt");
        Files.writeString(tmp, txt, StandardCharsets.UTF_8);

        Map map = MapLoader.fromTextFile(tmp.toString(), 16);
        assertTrue(map.rows() > 0);
        assertTrue(map.cols() > 0);
    }
}

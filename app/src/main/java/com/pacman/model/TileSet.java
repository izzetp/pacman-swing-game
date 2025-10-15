package com.pacman.model;
    
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;


/**
* Loads PNG images for each TileType and provides them for rendering.
*/
public class TileSet {

    private final EnumMap<TileType, BufferedImage> tileImages = new EnumMap<>(TileType.class);

    public TileSet() {
        try {
            tileImages.put(TileType.EMPTY, ImageIO.read(getClass().getResource("/")));
            tileImages.put(TileType.WALL, ImageIO.read(getClass().getResource("/tiles/wall.png")));
            tileImages.put(TileType.PACDOT, ImageIO.read(getClass().getResource("/tiles/pacdot.png")));
            tileImages.put(TileType.POWER_PACDOT, ImageIO.read(getClass().getResource("/tiles/power_pacdot.png")));
            tileImages.put(TileType.GHOST_GATE, ImageIO.read(getClass().getResource("/")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(TileType tile) {
        return tileImages.get(tile);
    }
}


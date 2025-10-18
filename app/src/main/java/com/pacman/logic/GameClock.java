package com.pacman.logic;
public interface GameClock {
    /** Return delta time (seconds) for this tick. Tests can supply fixed values. */
    double deltaSeconds();
}

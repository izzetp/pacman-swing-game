package com.pacman.logic;

import java.util.List;

public class FrightenedTimer {
    private final List<Ghost> ghosts;
    private double remaining;

    public FrightenedTimer(List<Ghost> ghosts) {
        this.ghosts = ghosts;
    }

    public void start(double seconds) {
        if (seconds <= 0) return;
        remaining = seconds;
        for (Ghost g : ghosts) g.setMode(Ghost.Mode.FRIGHTENED);
    }

    public void tick(GameClock clock) {
        if (remaining <= 0) return;
        remaining -= clock.deltaSeconds();
        if (remaining <= 0) {
            for (Ghost g : ghosts) g.setMode(Ghost.Mode.SCATTER);
            remaining = 0;
        }
    }

    public boolean active() {
        return remaining > 0;
    }
}

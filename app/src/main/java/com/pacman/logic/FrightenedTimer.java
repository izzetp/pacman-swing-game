package com.pacman.logic;

import java.util.List;

public class FrightenedTimer {
    private final List<Ghost> ghosts;
    private double remaining;
    private int chain;

    public FrightenedTimer(List<Ghost> ghosts) {
        this.ghosts = ghosts;
    }

    public void start(double seconds) {
        if (seconds <= 0) return;
        remaining = seconds;
        chain = 0;
        for (Ghost g : ghosts) g.setMode(Ghost.Mode.FRIGHTENED);
    }

    public void tick(GameClock clock) {
        if (remaining <= 0) return;
        remaining -= clock.deltaSeconds();
        if (remaining <= 0) {
            remaining = 0;
            chain = 0;
            for (Ghost g : ghosts) g.setMode(Ghost.Mode.SCATTER);
        }
    }

    public boolean active() {
        return remaining > 0;
    }

    public double secondsLeft() {
        return Math.max(0.0, remaining);
    }

    public int nextEatScore() {
        return switch (chain) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            default -> 1600;
        };
    }

    public void onGhostEaten() {
        if (remaining > 0) chain++;
    }

    public void cancel() {
        remaining = 0;
        chain = 0;
        for (Ghost g : ghosts) g.setMode(Ghost.Mode.SCATTER);
    }
}

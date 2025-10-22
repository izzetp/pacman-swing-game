package com.pacman.model;

public final class Score {
  private int value;
  public void add(int points) { value += points; }
  public int value() { return value; }
}

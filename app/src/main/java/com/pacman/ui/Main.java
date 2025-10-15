package com.pacman.ui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("Pac-Man Map Test");

        // Create the GamePanel
        GamePanel panel = new GamePanel();

        // Configure JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();                 
        frame.setResizable(false);     
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);

        panel.startGame();
    }
}
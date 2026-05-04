package org.example;
import javax.swing.*;
import java.awt.*;
    public class SpaceMaze extends JFrame
    {
        public SpaceMaze() {
        this.setTitle("Space Maze - Final Project");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        CardLayout cl = new CardLayout();
        JPanel container = new JPanel(cl);
        SpaceGamePanel game = new SpaceGamePanel();
        game.setOnMenuReturn(() -> {
            cl.show(container, "Menu");
        });

        MenuPanel menu = new MenuPanel((mode, isHebrew) -> {
            game.setGameMode(mode);
            game.setHebrew(isHebrew); // מעביר את בחירת השפה למשחק
            cl.show(container, "Game");
            game.startGame();
            game.requestFocusInWindow();
        });
        container.add(menu, "Menu");
        container.add(game, "Game");
        this.add(container);
        this.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceMaze::new);
    }
}
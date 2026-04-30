package org.example;
import javax.swing.*;
import java.awt.*;
public class SpaceMaze extends JFrame {
    public SpaceMaze() {
        this.setTitle("Space Maze - Final Project");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        CardLayout cl = new CardLayout();
        JPanel container = new JPanel(cl);
        SpaceGamePanel game = new SpaceGamePanel();
        // יצירת התפריט והגדרת הפעולה בהתאם לבחירת מצב ע"י משתמש
        MenuPanel menu = new MenuPanel(mode -> {
            game.setGameMode(mode); // עדכון המצב בתוך המשחק
            cl.show(container, "Game"); // מעבר למסך המשחק
            game.startGame(); // התחלת המשחק לפי המצב שנבחר
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
package org.example;
import java.awt.*;
public class Player extends SpaceObject
{
    public  final static int WIDTH = 30;
    public  final static int HEIGHT = 30;
    public Player(int x, int y) {
        super(x, y, WIDTH, HEIGHT, Color.CYAN);
    }
    @Override
    public void draw(Graphics g) {
        // ציור הריבוע
        g.setColor(this.color);
        g.fillRect(x, y, width, height);
        // הוספת מסגרת לבנה
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
    }
}
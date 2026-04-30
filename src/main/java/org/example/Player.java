package org.example;
import java.awt.*;
public class Player extends SpaceObject
{
    public Player(int x, int y) {
        super(x, y, 30, 30, Color.CYAN);
    }
    @Override
    public void draw(Graphics g) {
        // ציור הריבוע
        g.setColor(this.color);
        g.fillRect(x, y, width, height);
        // הוספת מסגרת לבנה יפה שהייתה לך במקור
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
    }
}
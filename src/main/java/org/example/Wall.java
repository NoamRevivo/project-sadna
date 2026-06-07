package org.example;
import java.awt.*;
public class Wall extends SpaceObject
{
    // הבנאי של החומה
    // מעביר את נתוני המיקום והממדים למחלקת האב ומגדיר להם צבע אפור קבוע מראש
    public Wall(int x, int y, int width, int height) {
        super(x, y, width, height, new Color(60, 60, 60));
    }

    // מציירת את החומה על המסך
    // יוצרת מלבן מלא בצבע אפור ומוסיפה לו קו מתאר שחור להדגשת הגבולות
    @Override
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }
}
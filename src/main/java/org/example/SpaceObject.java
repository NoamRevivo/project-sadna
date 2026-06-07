package org.example;
import java.awt.*;
public abstract class SpaceObject
{
    protected int x, y, width, height;
    protected Color color;

    // הבנאי של המחלקה האבסטראקטית
    // מאתחל את מאפייני המיקום, הממדים והצבע הבסיסיים המשותפים לכל העצמים הפיזיים במשחק.
    public SpaceObject(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }
    // פונקציות גישה ועדכון (Getters & Setters) לקבלת ושינוי ערכי המיקום והגודל של העצם
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    // מחזירה אובייקט מסוג Rectangle המייצג את גבולות העצם;
    // משמשת לחישוב וזיהוי התנגשויות פיזיקליות בין עצמים.
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    // פונקציה מופשטת לציור העצם;
    // מחייבת כל מחלקה יורשת להגדיר בעצמה כיצד האלמנט הגרפי שלה ייראה על המסך.
    public abstract void draw(Graphics g);
}
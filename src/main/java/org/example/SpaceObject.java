package org.example;

import java.awt.*;

public abstract class SpaceObject {
    protected int x, y, width, height;
    protected Color color;

    public SpaceObject(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public abstract void draw(Graphics g);
}
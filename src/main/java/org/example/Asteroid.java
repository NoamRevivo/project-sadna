package org.example;
import java.awt.*;
    public class Asteroid extends SpaceObject {
     private int speedX, speedY;
        public Asteroid(int x, int y, int width, int height, int sx, int sy) {
        super(x, y, width, height, Color.RED);
        this.speedX = sx;
        this.speedY = sy;
    }
    public void move() {
        this.x += this.speedX;
        this.y += this.speedY;
    }
    public void reverseX() { this.speedX *= -1; }
    public void reverseY() { this.speedY *= -1; }
    @Override
    public void draw(Graphics g)
    {
        // ציור אסטרואיד כעיגול אדום חלק
        g.setColor(this.color);
        g.fillOval(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, width, height);
    }
}
package org.example;
import java.awt.*;
    public class Wall extends SpaceObject {
     public Wall(int x, int y, int width, int height)
     {
        super(x, y, width, height, new Color(60, 60, 60));
     }
    @Override
    public void draw(Graphics g)
    {
        g.setColor(this.color);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }
}
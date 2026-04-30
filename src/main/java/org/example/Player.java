package org.example;
import java.awt.*;
public class Player extends SpaceObject {
    public Player(int x, int y) {
        super(x, y, 25, 25, Color.CYAN);
    }
    @Override
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillRect(this.x, this.y, this.width, this.height);
        g.setColor(Color.WHITE);
        g.drawRect(this.x, this.y, this.width, this.height);
    }
}

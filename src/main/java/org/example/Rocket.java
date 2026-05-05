package org.example;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class Rocket extends SpaceObject {
    private double dx, dy, speed, angle = 0;
    private Image image;

    public Rocket(int x, int y, int width, int height, int speedX, int speedY) {
        super(x, y, width, height, Color.RED);
        this.dx = x; this.dy = y; this.speed = speedX;
        try {
            URL url = getClass().getResource("/rocket.png");
            if (url != null) image = ImageIO.read(url);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void trackPlayer(int px, int py) {
        double diffX = px - (dx + width / 2.0), diffY = py - (dy + height / 2.0);
        angle = Math.atan2(diffY, diffX);
        dx += Math.cos(angle) * speed; dy += Math.sin(angle) * speed;
        this.x = (int) dx; this.y = (int) dy;
    }

    @Override
    public void draw(Graphics g) {
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.rotate(angle, dx + width/2.0, dy + height/2.0);
            g2d.drawImage(image, (int)dx, (int)dy, width, height, null);
            g2d.dispose();
        } else { g.setColor(color); g.fillOval((int)dx, (int)dy, width, height); }
    }

    @Override
    public Rectangle getBounds() {
        // צמצום משמעותי יותר של ה-Hitbox בטיל בגלל הסיבוב
        int shrink = width * 4 / 10;
        return new Rectangle((int)dx + shrink/2, (int)dy + shrink/2, width - shrink, height - shrink);
    }
}
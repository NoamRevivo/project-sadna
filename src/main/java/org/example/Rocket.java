package org.example;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class Rocket {
    private static final String IMAGE_PATH = "/rocket.png";
    private static final Color FALLBACK_COLOR = Color.RED;

    private double x, y; // חייב להישאר double בשביל תנועה חלקה
    private int width, height, speedX, speedY; // שמרתי על המשתנים המקוריים
    private double speed = 3.0;
    private double angle = 0;
    private Image image;

    public Rocket(int x, int y, int width, int height, int speedX, int speedY) {
        this.x = (double) x;
        this.y = (double) y;
        this.width = width;
        this.height = height;
        this.speedX = speedX;
        this.speedY = speedY;

        try {
            URL imageUrl = getClass().getResource(IMAGE_PATH);
            if (imageUrl != null) {
                image = ImageIO.read(imageUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void trackPlayer(int playerX, int playerY) {
        // חישוב המרחק למרכז השחקן
        double diffX = (double) playerX - (this.x + (this.width / 2.0));
        double diffY = (double) playerY - (this.y + (this.height / 2.0));

        this.angle = Math.atan2(diffY, diffX);

        this.x = this.x + (Math.cos(angle) * speed);
        this.y = this.y + (Math.sin(angle) * speed);
    }

    public void draw(Graphics g) {
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // המרה ל-double לצורך חישוב מרכז הסיבוב
            double centerX = this.x + (this.width / 2.0);
            double centerY = this.y + (this.height / 2.0);

            g2d.rotate(angle, centerX, centerY);
            g2d.drawImage(image, (int) this.x, (int) this.y, width, height, null);

            g2d.dispose();
        } else {
            g.setColor(FALLBACK_COLOR);
            g.fillOval((int) this.x, (int) this.y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) this.x, (int) this.y, width, height);
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // שמרתי על המתודות המקוריות שלך למקרה שהן נקראות במקום אחר
    public void reverseX() { speedX = -speedX; }
    public void reverseY() { speedY = -speedY; }
}
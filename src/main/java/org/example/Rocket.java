package org.example;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

    public class Rocket
    {
    private static final String IMAGE_PATH = "/rocket.png";
    private static final Color FALLBACK_COLOR = Color.RED;
    private int x, y, width, height, speedX, speedY;
    private Image image;
    public Rocket(int x, int y, int width, int height, int speedX, int speedY)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speedX = speedX;
        this.speedY = speedY;

        try
        {
            URL imageUrl = getClass().getResource(IMAGE_PATH);
            if (imageUrl != null)
            {
                image = ImageIO.read(imageUrl);
            }
            else
            {
                System.err.println("Could not find " + IMAGE_PATH);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void move() {
        x += speedX;
        y += speedY;
    }
    public void reverseX() { speedX = -speedX; }
    public void reverseY() { speedY = -speedY; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds()
    {
        return new Rectangle(x, y, width, height);
    }
    public void draw(Graphics g)
    {
        if (image != null)
        {
            g.drawImage(image, x, y, width, height, null);
        }
        else
        {
            g.setColor(FALLBACK_COLOR);
            g.fillOval(x, y, width, height);
        }
    }
}
package org.example;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class Spaceship
{
    private static final String IMAGE_PATH = "/spaceship 2.png";
    private static final Color FALLBACK_COLOR = Color.BLUE;
    private static final int DEFAULT_START_X = 0;
    private static final int DEFAULT_START_Y = 30;
    private int x, y, width, height;
    private Image image;

    public Spaceship(int width, int height)
    {
        this.x = DEFAULT_START_X;
        this.y = DEFAULT_START_Y;
        this.width = width;
        this.height = height;
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
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void draw(Graphics g)
    {
        if (image != null)
        {
            g.drawImage(image, x, y, width, height, null);
        }
        else
        {
            g.setColor(FALLBACK_COLOR);
            g.fillRect(x, y, width, height);
        }
    }
}
package org.example;
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;
public class Spaceship
{
    private static final String IMAGE_PATH = "/spaceship 2.png";
    private int x = 5, y = 35, width, height;
    private Image image;
    public Spaceship(int width, int height)
    {
        this.width = width;
        this.height = height;
        try {
            URL url = getClass().getResource(IMAGE_PATH);
            if (url != null) image = ImageIO.read(url);
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void draw(Graphics g)
    {
        if (image != null) g.drawImage(image, x, y, width, height, null);
        else { g.setColor(Color.BLUE); g.fillRect(x, y, width, height); }
    }
    public Rectangle getBounds()
    {
        int shrinkW = width / 4, shrinkH = height / 4;
        return new Rectangle(x + shrinkW/2, y + shrinkH/2,
                width - shrinkW, height - shrinkH);
    }
}
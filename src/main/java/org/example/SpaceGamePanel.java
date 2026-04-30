package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SpaceGamePanel extends JPanel implements Runnable {
    private Player player;
    private Wall[] mazeWalls;
    private Asteroid[] asteroids;
    private Rectangle goal;

    private Thread gameThread;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean up, down, left, right;
    private boolean hasStartedMoving = false;
    private int timeLeft;
    private int frameCounter = 0;
    private int currentLevel = 1;
    private final int MAX_LEVELS = 50;
    private int lives = 3;
    // משתנה חדש לניהול מצב המשחק (1-רגיל, 2-אקראי, 3-בחירת שלב)
    private int gameMode = 1;

    public SpaceGamePanel() {
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        setupSkipButton();
        setupKeyListener();
    }

    public void setGameMode(int mode) {
        this.gameMode = mode;
    }

    private void setupSkipButton() {
        JButton skipBtn = new JButton("Level 50 (Cheat)");
        skipBtn.setBounds(10, 510, 140, 30);
        skipBtn.setFocusable(false);
        skipBtn.addActionListener(e -> {
            currentLevel = 50;
            initLevel(currentLevel);
        });
        this.add(skipBtn);
    }

    private void initLevel(int level) {
        resetPosition();
        player = new Player(50, 50);
        goal = new Rectangle(720, 500, 40, 40);
        Random rand = new Random();
        int wallCount = 2 + (level / 15);
        mazeWalls = new Wall[wallCount * 2];

        if (level % 2 != 0) {
            int startX = 160;
            int sliceWidth = (650 - startX) / Math.max(1, wallCount);
            for (int i = 0; i < wallCount; i++) {
                int x = startX + (i * sliceWidth) + rand.nextInt(Math.max(1, sliceWidth - 40));
                // תיקון: הבטחת מרווח מינימלי של 50 פיקסלים לעבירות
                int gapSize = 50 + rand.nextInt(Math.max(1, 150 - (level * 2)));
                int gapStart = 50 + rand.nextInt(Math.max(1, 500 - gapSize));
                mazeWalls[i * 2] = new Wall(x, 0, 30, gapStart);
                mazeWalls[i * 2 + 1] = new Wall(x, gapStart + gapSize, 30, 600 - (gapStart + gapSize));
            }
        } else {
            int startY = 150;
            int sliceHeight = (450 - startY) / Math.max(1, wallCount);
            for (int i = 0; i < wallCount; i++) {
                int y = startY + (i * sliceHeight) + rand.nextInt(Math.max(1, sliceHeight - 40));
                // תיקון: הבטחת מרווח מינימלי של 50 פיקסלים לעבירות
                int gapSize = 50 + rand.nextInt(Math.max(1, 150 - (level * 2)));
                int gapStart = 100 + rand.nextInt(Math.max(1, 600 - gapSize));
                mazeWalls[i * 2] = new Wall(0, y, gapStart, 30);
                mazeWalls[i * 2 + 1] = new Wall(gapStart + gapSize, y, 800 - (gapStart + gapSize), 30);
            }
        }

        int asteroidNum = 1 + (level / 7);
        asteroids = new Asteroid[asteroidNum];
        for (int i = 0; i < asteroidNum; i++) {
            int ax, ay;
            do {
                ax = 200 + rand.nextInt(500);
                ay = rand.nextInt(450);
            } while (ax < 150 && ay < 150);
            // תיקון קל: האטה קלה של המהירות המקסימלית (חלוקה ב-15 במקום ב-12)
            int speed = 2 + (level / 15);
            asteroids[i] = new Asteroid(ax, ay, 30, 30, rand.nextBoolean() ? speed : -speed, rand.nextBoolean() ? speed : -speed);
        }
    }

    public void startGame() {
        if (gameMode == 1) {
            currentLevel = 1;
        } else if (gameMode == 2) {
            currentLevel = new Random().nextInt(MAX_LEVELS) + 1;
        } else if (gameMode == 3) {
            String input = JOptionPane.showInputDialog(this, "בחר שלב (1-" + MAX_LEVELS + "):");
            try {
                if (input != null) {
                    int selectedLevel = Integer.parseInt(input);
                    if (selectedLevel < 1) {
                        currentLevel = 1;
                        JOptionPane.showMessageDialog(this, "שלב לא תקין, מתחיל משלב 1");
                    } else if (selectedLevel > MAX_LEVELS) {
                        currentLevel = MAX_LEVELS;
                        JOptionPane.showMessageDialog(this, "השלב המקסימלי הוא " + MAX_LEVELS + ". עובר לשלב האחרון.");
                    } else {
                        currentLevel = selectedLevel;
                    }
                } else {
                    return; // לחיצה על Cancel
                }
            } catch (NumberFormatException e) {
                currentLevel = 1;
                JOptionPane.showMessageDialog(this, "נא להזין מספר בלבד. מתחיל משלב 1.");
            }
        }
        initLevel(currentLevel);
        lives = 3;
        if (gameThread == null || !isRunning) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!isPaused) updateGame();
            SwingUtilities.invokeLater(this::repaint);
            try { Thread.sleep(16); } catch (InterruptedException e) { break; }
        }
    }

    private void updateGame() {
        if (getWidth() <= 0) return;
        if (!hasStartedMoving && (up || down || left || right)) hasStartedMoving = true;
        int nx = player.getX(), ny = player.getY();
        if (up) ny -= 5; if (down) ny += 5; if (left) nx -= 5; if (right) nx += 5;
        if (nx < 0) nx = 0; if (ny < 0) ny = 0;
        if (nx > getWidth() - player.getWidth()) nx = getWidth() - player.getWidth();
        if (ny > getHeight() - player.getHeight()) ny = getHeight() - player.getHeight();
        Rectangle nextPos = new Rectangle(nx, ny, player.getWidth(), player.getHeight());

        if (nextPos.intersects(goal)) {
            if (currentLevel < MAX_LEVELS) {
                currentLevel++;
                JOptionPane.showMessageDialog(this, "שלב " + (currentLevel - 1) + " הושלם!");
                initLevel(currentLevel);
                return;
            } else {
                JOptionPane.showMessageDialog(this, "ניצחת במשחק!");
                isRunning = false;
                System.exit(0);
            }
        }
        boolean canMove = true;
        for (int i = 0; i < mazeWalls.length; i++) {
            if (nextPos.intersects(mazeWalls[i].getBounds())) {
                canMove = false;
                break;
            }
        }
        if (hasStartedMoving) {
            frameCounter++;
            if (frameCounter >= 60) {
                timeLeft--; frameCounter = 0;
                if (timeLeft <= 0) { handlePlayerHit("נגמר הזמן!"); return; }
            }
            for (int i = 0; i < asteroids.length; i++) {
                Asteroid a = asteroids[i];
                a.move();
                if (a.getX() < 0 || a.getX() > getWidth() - a.getWidth()) a.reverseX();
                if (a.getY() < 0 || a.getY() > getHeight() - a.getHeight()) a.reverseY();
                if (nextPos.intersects(a.getBounds())) {
                    handlePlayerHit("נפגעת!");
                    canMove = false;
                }
            }
        }

        if (canMove) { player.setX(nx); player.setY(ny); }
    }

    private void handlePlayerHit(String reason) {
        lives--;
        if (lives <= 0) {
            isRunning = false;
            JOptionPane.showMessageDialog(this, "המשחק נגמר!");
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this, reason + "\nחיים נותרים: " + lives);
            resetPosition();
        }
    }

    private void resetPosition() {
        if (player != null) { player.setX(50); player.setY(50); }
        up = false; down = false; left = false; right = false;
        hasStartedMoving = false;
        timeLeft = 15 + ((currentLevel - 1) * 45 / 49);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (goal == null || player == null) return;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Level: " + currentLevel + " | Lives: " + lives + " | Time: " + timeLeft, 10, 25);
        g.setColor(Color.GREEN);
        g.fillRect(goal.x, goal.y, goal.width, goal.height);
        if (mazeWalls != null) {
            for (int i = 0; i < mazeWalls.length; i++) {
                if (mazeWalls[i] != null) mazeWalls[i].draw(g);
            }
        }
        if (asteroids != null) {
            for (int i = 0; i < asteroids.length; i++) {
                if (asteroids[i] != null) asteroids[i].draw(g);
            }
        }
        player.draw(g);
        if (isPaused) {
            g.setColor(Color.YELLOW);
            g.drawString("PAUSED", getWidth()/2 - 20, getHeight()/2);
        }
    }

    private void setupKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_P) isPaused = !isPaused;
                if (isPaused) return;
                if (c == KeyEvent.VK_UP) up = true;
                if (c == KeyEvent.VK_DOWN) down = true;
                if (c == KeyEvent.VK_LEFT) left = true;
                if (c == KeyEvent.VK_RIGHT) right = true;
            }
            public void keyReleased(KeyEvent e) {
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_UP) up = false;
                if (c == KeyEvent.VK_DOWN) down = false;
                if (c == KeyEvent.VK_LEFT) left = false;
                if (c == KeyEvent.VK_RIGHT) right = false;
            }
        });
    }
}
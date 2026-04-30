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
    private volatile boolean isRunning = false;
    private boolean isPaused = false;
    private boolean up, down, left, right;
    private boolean hasStartedMoving = false; // המשתנה שדואג שהמשחק ימתין ללחיצה

    private int timeLeft;
    private int frameCounter = 0;
    private int currentLevel = 1;
    private int lives = 3;
    private int gameMode = 1;

    public SpaceGamePanel() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        setupKeyListener();
    }

    public void setGameMode(int mode) { this.gameMode = mode; }

    private void initLevel(int level) {
        player = new Player(30, 40);
        goal = new Rectangle(740, 510, 40, 40); // היעד ממוקם בפינה למטה
        Random rand = new Random();

        int wallCount = 2 + (level / 12);
        mazeWalls = new Wall[wallCount * 2];

        // תיקון המבוך: הגבלנו את הטווח המקסימלי כדי שלא יסתיר את היעד הירוק!
        if (level % 2 != 0) {
            int segment = 580 / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int x = 120 + (i * segment); // ה-X המקסימלי יעצור הרבה לפני היעד
                int gap = 90 + rand.nextInt(100);
                int gapY = 50 + rand.nextInt(350);
                mazeWalls[i * 2] = new Wall(x, 0, 25, gapY);
                mazeWalls[i * 2 + 1] = new Wall(x, gapY + gap, 25, 600);
            }
        } else {
            int segment = 380 / wallCount; // הגבלנו את ה-Y כדי שהקירות לא ירדו למטה מדי
            for (int i = 0; i < wallCount; i++) {
                int y = 100 + (i * segment);
                int gap = 90 + rand.nextInt(100);
                int gapX = 150 + rand.nextInt(400);
                mazeWalls[i * 2] = new Wall(0, y, gapX, 25);
                mazeWalls[i * 2 + 1] = new Wall(gapX + gap, y, 800, 25);
            }
        }

        int astCount = 1 + (level / 10);
        asteroids = new Asteroid[astCount];
        for (int i = 0; i < astCount; i++) {
            int speed = 2 + (level / 15);
            asteroids[i] = new Asteroid(200 + rand.nextInt(400), 100 + rand.nextInt(300), 25, 25, speed, speed);
        }

        timeLeft = 15 + (level * 45 / 50);
        up = down = left = right = false;
        hasStartedMoving = false; // מאפס את הדגל כך שהמשחק ימתין למשתמש
    }

    public void startGame() {
        if (gameMode == 1) {
            currentLevel = 1;
        } else if (gameMode == 2) {
            currentLevel = new Random().nextInt(50) + 1;
        } else if (gameMode == 3) {
            String val = JOptionPane.showInputDialog(null, "אנא הקלד מספר שלב (1-50):", "בחירת שלב ידנית", JOptionPane.QUESTION_MESSAGE);
            try {
                if (val != null && !val.trim().isEmpty()) {
                    currentLevel = Math.max(1, Math.min(50, Integer.parseInt(val)));
                } else {
                    currentLevel = 1;
                }
            } catch (NumberFormatException e) {
                currentLevel = 1;
            }
        }

        lives = 3;
        initLevel(currentLevel);

        if (gameThread == null || !isRunning) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!isPaused) update();
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) {}
        }
    }

    private void update() {
        // בודק אם השחקן לחץ על חץ כדי להתחיל להזיז את המשחק
        if (!hasStartedMoving && (up || down || left || right)) {
            hasStartedMoving = true;
        }

        int nx = player.getX(), ny = player.getY();
        if (up) ny -= 5; if (down) ny += 5; if (left) nx -= 5; if (right) nx += 5;

        Rectangle next = new Rectangle(nx, ny, player.getWidth(), player.getHeight());
        boolean hit = false;
        for (Wall w : mazeWalls) if (w != null && next.intersects(w.getBounds())) hit = true;

        if (!hit) {
            player.setX(Math.max(0, Math.min(760, nx)));
            player.setY(Math.max(30, Math.min(560, ny)));
        }

        // האסטרואידים והזמן פועלים *רק* אם השחקן התחיל לזוז
        if (hasStartedMoving) {
            for (Asteroid a : asteroids) {
                if (a != null) {
                    a.move();
                    if (a.getX() < 0 || a.getX() > 770) a.reverseX();
                    if (a.getY() < 30 || a.getY() > 570) a.reverseY();
                    if (next.intersects(a.getBounds())) { handleDeath("נפגעת מאסטרואיד!"); return; }
                }
            }

            frameCounter++;
            if (frameCounter >= 60) {
                timeLeft--; frameCounter = 0;
                if (timeLeft <= 0) handleDeath("נגמר הזמן!");
            }
        }

        if (next.intersects(goal)) {
            if (currentLevel < 50) {
                currentLevel++;
                initLevel(currentLevel);
            } else {
                JOptionPane.showMessageDialog(null, "כל הכבוד! ניצחת במשחק!");
                System.exit(0);
            }
        }
    }

    // פונקציית הפסילה החדשה שמקבלת את סיבת המוות ומציגה אותה עם החיים שנותרו
    private void handleDeath(String reason) {
        lives--;
        up = down = left = right = false; // איפוס מקשים כדי למנוע תזוזה מקרית

        if (lives <= 0) {
            JOptionPane.showMessageDialog(null, reason + "\nהמשחק נגמר! לא נותרו לך חיים.");
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(null, reason + "\nנותרו לך " + lives + " חיים.\nלחץ OK ואז על אחד החצים כדי להמשיך.");
            initLevel(currentLevel);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.GREEN);
        g.fillRect(goal.x, goal.y, goal.width, goal.height);

        for (Wall w : mazeWalls) if (w != null) w.draw(g);
        for (Asteroid a : asteroids) if (a != null) a.draw(g);

        player.draw(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 30);

        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, 30, 800, 30);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Level: " + currentLevel + " | Lives: " + lives + " | Time: " + timeLeft, 20, 21);

        // כיתוב שמנחה את השחקן להתחיל לנוע
        if (!hasStartedMoving && !isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("לחץ על אחד החצים כדי להתחיל!", 220, 280);
        }

        if (isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", 320, 300);
        }
    }

    private void setupKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_P) isPaused = !isPaused;
                if (!isPaused) {
                    if (c == KeyEvent.VK_UP) up = true;
                    if (c == KeyEvent.VK_DOWN) down = true;
                    if (c == KeyEvent.VK_LEFT) left = true;
                    if (c == KeyEvent.VK_RIGHT) right = true;
                }
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
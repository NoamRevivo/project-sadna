package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SpaceGamePanel extends JPanel implements Runnable {

    // =========================================
    // --- קבועים (Constants) מעודכנים ---
    // =========================================
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int TOP_BAR_HEIGHT = 30;
    private static final int MIN_RANDOM_BOUND = 1;

    // הגדרות שחקן - גודל הוקטן למניעת תקיעות
    private static final int PLAYER_START_WIDTH = 45;
    private static final int PLAYER_START_HEIGHT = 55;
    private static final int PLAYER_SPEED = 5;
    private static final int STARTING_LIVES = 3;

    // הגדרות יעד
    private static final int GOAL_WIDTH = 40;
    private static final int GOAL_HEIGHT = 40;
    private static final int GOAL_OFFSET_X = 70;
    private static final int GOAL_OFFSET_Y = 90;

    // הגדרות שלבים ומצבים
    private static final int MAX_LEVEL = 50;
    private static final int STARTING_LEVEL = 1;
    private static final int MODE_NORMAL = 1;
    private static final int MODE_RANDOM = 2;
    private static final int MODE_MANUAL = 3;

    // הגדרות חומות - רווח בסיס הוגדל
    private static final int WALL_THICKNESS = 25;
    private static final int WALL_BASE_COUNT = 2;
    private static final int WALL_LEVEL_DIVISOR = 12;
    private static final int WALL_MAX_LENGTH = 2000;
    private static final int WALL_GAP_BASE = 110;
    private static final int WALL_GAP_RANDOM_ADD = 100;
    private static final int WALLS_PER_SEGMENT = 2;

    // הגדרות טילים
    private static final int ROCKET_BASE_COUNT = 1;
    private static final int ROCKET_LEVEL_DIVISOR = 20;
    private static final int ROCKET_SIZE = 85;
    private static final int ROCKET_BASE_SPEED = 1;
    private static final int ROCKET_SPEED_DIVISOR = 20;

    // הגדרות טקסט ותפריט
    private static final int FONT_SIZE_MEDIUM = 16;
    private static final int FONT_SIZE_LARGE = 24;
    private static final int STATS_TEXT_X = 20;
    private static final int STATS_TEXT_Y = 21;

    // =========================================

    private Spaceship spaceship;
    private Wall[] mazeWalls;
    private Rocket[] rockets;
    private Rectangle goal;
    private Thread gameThread;
    private volatile boolean isRunning = false;
    private boolean isPaused = false;
    private boolean up, down, left, right;
    private boolean hasStartedMoving = false;
    private int timeLeft, currentLevel, lives, gameMode;
    private int frameCounter = 0;
    private boolean isHebrew = true;
    private Runnable onMenuReturn;
    private JButton menuButton;

    public SpaceGamePanel() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setLayout(null);
        setupMenuButton();
        setupKeyListener();
    }

    private void setupMenuButton() {
        menuButton = new JButton();
        menuButton.setFont(new Font("Arial", Font.BOLD, 14));
        menuButton.setBackground(Color.DARK_GRAY);
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusable(false);
        menuButton.addActionListener(e -> { isRunning = false; if (onMenuReturn != null) onMenuReturn.run(); });
        this.add(menuButton);
    }

    public void setGameMode(int mode) { this.gameMode = mode; }
    public void setHebrew(boolean isHebrew) {
        this.isHebrew = isHebrew;
        menuButton.setText(isHebrew ? "תפריט ראשי" : "Main Menu");
    }
    public void setOnMenuReturn(Runnable onMenuReturn) { this.onMenuReturn = onMenuReturn; }

    private void initLevel(int level) {
        int w = getWidth() > 0 ? getWidth() : DEFAULT_WIDTH;
        int h = getHeight() > 0 ? getHeight() : DEFAULT_HEIGHT;
        spaceship = new Spaceship(PLAYER_START_WIDTH, PLAYER_START_HEIGHT);
        goal = new Rectangle(w - GOAL_OFFSET_X, h - GOAL_OFFSET_Y, GOAL_WIDTH, GOAL_HEIGHT);
        Random rand = new Random();

        int wallCount = WALL_BASE_COUNT + (level / WALL_LEVEL_DIVISOR);
        mazeWalls = new Wall[wallCount * WALLS_PER_SEGMENT];

        if (level % 2 != 0) {
            int segment = (w - 200) / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int x = 120 + (i * segment);
                int gap = WALL_GAP_BASE + rand.nextInt(WALL_GAP_RANDOM_ADD);
                int gapY = 50 + rand.nextInt(Math.max(1, h - 300));
                mazeWalls[i * 2] = new Wall(x, 0, WALL_THICKNESS, gapY);
                mazeWalls[i * 2 + 1] = new Wall(x, gapY + gap, WALL_THICKNESS, Math.max(h, 2000));
            }
        } else {
            int segment = (h - 150) / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int y = 140 + (i * segment);
                int gap = WALL_GAP_BASE + rand.nextInt(WALL_GAP_RANDOM_ADD);
                int gapX = 150 + rand.nextInt(Math.max(1, w - 300));
                mazeWalls[i * 2] = new Wall(0, y, gapX, WALL_THICKNESS);
                mazeWalls[i * 2 + 1] = new Wall(gapX + gap, y, Math.max(w, 2000), WALL_THICKNESS);
            }
        }

        int rocketCount = ROCKET_BASE_COUNT + (level / ROCKET_LEVEL_DIVISOR);
        rockets = new Rocket[rocketCount];
        for (int i = 0; i < rocketCount; i++) {
            int speed = ROCKET_BASE_SPEED + (level / ROCKET_SPEED_DIVISOR);
            rockets[i] = new Rocket(200 + rand.nextInt(Math.max(1, w - 400)), 100 + rand.nextInt(Math.max(1, h - 300)), ROCKET_SIZE, ROCKET_SIZE, speed, speed);
        }

        timeLeft = 15 + (level * 45 / 50);
        up = down = left = right = false;
        hasStartedMoving = false;
    }

    public void startGame() {
        if (gameMode == MODE_NORMAL) currentLevel = STARTING_LEVEL;
        else if (gameMode == MODE_RANDOM) currentLevel = new Random().nextInt(MAX_LEVEL) + 1;
        else if (gameMode == MODE_MANUAL) {
            String msg = isHebrew ? "אנא הקלד מספר שלב (1-50):" : "Please enter level number (1-50):";
            String title = isHebrew ? "בחירת שלב ידנית" : "Manual Level Selection";
            // התיקון הקריטי כאן: הוספת null כפרמטר אחרון
            String val = (String) JOptionPane.showInputDialog(null, msg, title, JOptionPane.QUESTION_MESSAGE, null, null, null);
            try { currentLevel = (val != null && !val.trim().isEmpty()) ? Math.max(1, Math.min(50, Integer.parseInt(val))) : 1; }
            catch (NumberFormatException e) { currentLevel = 1; }
        }
        lives = STARTING_LIVES;
        initLevel(currentLevel);
        if (gameThread == null || !isRunning) { isRunning = true; gameThread = new Thread(this); gameThread.start(); }
    }

    @Override
    public void run() {
        while (isRunning) { if (!isPaused) update(); repaint(); try { Thread.sleep(16); } catch (InterruptedException e) {} }
    }

    private void update() {
        if (!hasStartedMoving && (up || down || left || right)) hasStartedMoving = true;

        int nx = spaceship.getX(), ny = spaceship.getY();
        if (up) ny -= PLAYER_SPEED; if (down) ny += PLAYER_SPEED;
        if (left) nx -= PLAYER_SPEED; if (right) nx += PLAYER_SPEED;

        // בדיקת התנגשות נפרדת ל-X ול-Y כדי למנוע "נעילה" בפינות
        Rectangle nextX = new Rectangle(nx, spaceship.getY(), spaceship.getWidth(), spaceship.getHeight());
        boolean hitWallX = false;
        for (Wall w : mazeWalls) if (w != null && nextX.intersects(w.getBounds())) { hitWallX = true; break; }
        if (!hitWallX) spaceship.setX(Math.max(0, Math.min(getWidth() - spaceship.getWidth(), nx)));

        Rectangle nextY = new Rectangle(spaceship.getX(), ny, spaceship.getWidth(), spaceship.getHeight());
        boolean hitWallY = false;
        for (Wall w : mazeWalls) if (w != null && nextY.intersects(w.getBounds())) { hitWallY = true; break; }
        if (!hitWallY) spaceship.setY(Math.max(TOP_BAR_HEIGHT, Math.min(getHeight() - spaceship.getHeight(), ny)));

        if (hasStartedMoving) {
            for (Rocket r : rockets) {
                if (r != null) {
                    r.trackPlayer(spaceship.getX(), spaceship.getY());
                    if (spaceship.getBounds().intersects(r.getBounds())) { handleDeath(isHebrew ? "נפגעת מטיל!" : "Hit by a rocket!"); return; }
                }
            }
            frameCounter++;
            if (frameCounter >= 60) { timeLeft--; frameCounter = 0; if (timeLeft <= 0) handleDeath(isHebrew ? "נגמר הזמן!" : "Time's up!"); }
        }

        if (spaceship.getBounds().intersects(goal)) {
            if (currentLevel < MAX_LEVEL) { currentLevel++; initLevel(currentLevel); }
            else showEndGameMenu(isHebrew ? "ניצחת!" : "Victory!", "Victory");
        }
    }

    private void handleDeath(String reason) {
        lives--; up = down = left = right = false;
        if (lives <= 0) showEndGameMenu(reason + (isHebrew ? "\nהמשחק נגמר!" : "\nGame Over!"), "Game Over");
        else { JOptionPane.showMessageDialog(null, reason + (isHebrew ? "\nנותרו " + lives + " חיים." : "\n" + lives + " lives left.")); initLevel(currentLevel); }
    }

    private void showEndGameMenu(String message, String title) {
        Object[] options = isHebrew ? new Object[]{"תפריט", "יציאה"} : new Object[]{"Menu", "Exit"};
        int choice = JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) { isRunning = false; if (onMenuReturn != null) onMenuReturn.run(); } else System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        menuButton.setBounds(getWidth() - 140, 2, 120, 26);
        g.setColor(Color.GREEN); g.fillRect(goal.x, goal.y, goal.width, goal.height);
        for (Wall w : mazeWalls) if (w != null) w.draw(g);
        for (Rocket r : rockets) if (r != null) r.draw(g);
        spaceship.draw(g);
        g.setColor(Color.BLACK); g.fillRect(0, 0, getWidth(), TOP_BAR_HEIGHT);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_MEDIUM));
        g.drawString("Level: " + currentLevel + " | Lives: " + lives + " | Time: " + timeLeft, STATS_TEXT_X, STATS_TEXT_Y);
        if (!hasStartedMoving && !isPaused) {
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_LARGE));
            String msg = isHebrew ? "לחץ על החצים כדי להתחיל!" : "Press arrows to start!";
            g.drawString(msg, getWidth() / 2 - 150, getHeight() / 2);
        }
    }

    private void setupKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_P) isPaused = !isPaused;
                if (!isPaused) { if (c == KeyEvent.VK_UP) up = true; if (c == KeyEvent.VK_DOWN) down = true; if (c == KeyEvent.VK_LEFT) left = true; if (c == KeyEvent.VK_RIGHT) right = true; }
            }
            public void keyReleased(KeyEvent e) {
                int c = e.getKeyCode();
                if (c == KeyEvent.VK_UP) up = false; if (c == KeyEvent.VK_DOWN) down = false; if (c == KeyEvent.VK_LEFT) left = false; if (c == KeyEvent.VK_RIGHT) right = false;
            }
        });
    }
}
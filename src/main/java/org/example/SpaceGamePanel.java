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
    private boolean hasStartedMoving = false;

    private int timeLeft;
    private int frameCounter = 0;
    private int currentLevel = 1;
    private int lives = 3;
    private int gameMode = 1;
    private boolean isHebrew = true;

    private Runnable onMenuReturn;

    // הכפתור החדש שלנו
    private JButton menuButton;

    public SpaceGamePanel() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setLayout(null); // חובה כדי שנוכל למקם את הכפתור איפה שנרצה

        setupMenuButton(); // יצירת הכפתור
        setupKeyListener();
    }

    // פונקציה לבניית הכפתור
    private void setupMenuButton() {
        menuButton = new JButton();
        menuButton.setFont(new Font("Arial", Font.BOLD, 14));
        menuButton.setBackground(Color.DARK_GRAY);
        menuButton.setForeground(Color.WHITE);
        // קריטי! מונע מהכפתור "לגנוב" את הפוקוס של המקלדת
        menuButton.setFocusable(false);

        // כשהמשתמש לוחץ על הכפתור - עוצרים את המשחק וחוזרים לתפריט
        menuButton.addActionListener(e -> {
            isRunning = false;
            if (onMenuReturn != null) onMenuReturn.run();
        });

        this.add(menuButton);
    }

    public void setGameMode(int mode) { this.gameMode = mode; }

    public void setHebrew(boolean isHebrew) {
        this.isHebrew = isHebrew;
        // עדכון הטקסט של הכפתור בזמן אמת לפי השפה
        if (menuButton != null) {
            menuButton.setText(isHebrew ? "תפריט ראשי" : "Main Menu");
        }
    }

    public void setOnMenuReturn(Runnable onMenuReturn) { this.onMenuReturn = onMenuReturn; }

    private void initLevel(int level) {
        int w = getWidth() > 0 ? getWidth() : 800;
        int h = getHeight() > 0 ? getHeight() : 600;

        player = new Player(30, 40);
        goal = new Rectangle(w - 70, h - 90, 40, 40);
        Random rand = new Random();

        int wallCount = 2 + (level / 12);
        mazeWalls = new Wall[wallCount * 2];

        if (level % 2 != 0) {
            int segment = (w - 200) / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int x = 120 + (i * segment);
                int gap = 90 + rand.nextInt(100);
                int gapY = 50 + rand.nextInt(Math.max(1, h - 300));
                mazeWalls[i * 2] = new Wall(x, 0, 25, gapY);
                mazeWalls[i * 2 + 1] = new Wall(x, gapY + gap, 25, Math.max(h, 2000));
            }
        } else {
            int segment = (h - 150) / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int y = 100 + (i * segment);
                int gap = 90 + rand.nextInt(100);
                int gapX = 150 + rand.nextInt(Math.max(1, w - 300));
                mazeWalls[i * 2] = new Wall(0, y, gapX, 25);
                mazeWalls[i * 2 + 1] = new Wall(gapX + gap, y, Math.max(w, 2000), 25);
            }
        }

        int astCount = 1 + (level / 10);
        asteroids = new Asteroid[astCount];
        for (int i = 0; i < astCount; i++) {
            int speed = 2 + (level / 15);
            asteroids[i] = new Asteroid(200 + rand.nextInt(Math.max(1, w - 300)), 100 + rand.nextInt(Math.max(1, h - 200)), 25, 25, speed, speed);
        }

        timeLeft = 15 + (level * 45 / 50);
        up = down = left = right = false;
        hasStartedMoving = false;
    }

    public void startGame() {
        if (gameMode == 1) {
            currentLevel = 1;
        } else if (gameMode == 2) {
            currentLevel = new Random().nextInt(50) + 1;
        } else if (gameMode == 3) {
            String msg = isHebrew ? "אנא הקלד מספר שלב (1-50):" : "Please enter level number (1-50):";
            String title = isHebrew ? "בחירת שלב ידנית" : "Manual Level Selection";

            String val = JOptionPane.showInputDialog(null, msg, title, JOptionPane.QUESTION_MESSAGE);
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
        if (!hasStartedMoving && (up || down || left || right)) {
            hasStartedMoving = true;
        }

        int nx = player.getX(), ny = player.getY();
        if (up) ny -= 5; if (down) ny += 5; if (left) nx -= 5; if (right) nx += 5;

        Rectangle next = new Rectangle(nx, ny, player.getWidth(), player.getHeight());
        boolean hit = false;
        for (Wall w : mazeWalls) if (w != null && next.intersects(w.getBounds())) hit = true;

        if (!hit) {
            player.setX(Math.max(0, Math.min(getWidth() - player.getWidth(), nx)));
            player.setY(Math.max(30, Math.min(getHeight() - player.getHeight(), ny)));
        }

        if (hasStartedMoving) {
            for (Asteroid a : asteroids) {
                if (a != null) {
                    a.move();
                    if (a.getX() < 0 || a.getX() > getWidth() - a.getWidth()) a.reverseX();
                    if (a.getY() < 30 || a.getY() > getHeight() - a.getHeight()) a.reverseY();
                    if (next.intersects(a.getBounds())) {
                        handleDeath(isHebrew ? "נפגעת מאסטרואיד!" : "Hit by an asteroid!");
                        return;
                    }
                }
            }

            frameCounter++;
            if (frameCounter >= 60) {
                timeLeft--; frameCounter = 0;
                if (timeLeft <= 0) handleDeath(isHebrew ? "נגמר הזמן!" : "Time's up!");
            }
        }

        if (next.intersects(goal)) {
            if (currentLevel < 50) {
                currentLevel++;
                initLevel(currentLevel);
            } else {
                showEndGameMenu(isHebrew ? "כל הכבוד! ניצחת במשחק!" : "Congratulations! You won the game!", isHebrew ? "ניצחון" : "Victory");
            }
        }
    }

    private void handleDeath(String reason) {
        lives--;
        up = down = left = right = false;

        if (lives <= 0) {
            String msg = reason + (isHebrew ? "\nהמשחק נגמר! לא נותרו לך חיים." : "\nGame Over! No lives remaining.");
            showEndGameMenu(msg, "Game Over");
        } else {
            String msg = reason + (isHebrew ? "\nנותרו לך " + lives + " חיים.\nלחץ OK ואז על אחד החצים כדי להמשיך." : "\nYou have " + lives + " lives remaining.\nPress OK and then an arrow key to continue.");
            JOptionPane.showMessageDialog(null, msg);
            initLevel(currentLevel);
        }
    }

    private void showEndGameMenu(String message, String title) {
        Object[] options = isHebrew ? new Object[]{"חזור לתפריט", "יציאה מהמשחק"} : new Object[]{"Return to Menu", "Exit Game"};
        int choice = JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            isRunning = false;
            if (onMenuReturn != null) onMenuReturn.run();
        } else {
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // עדכון המיקום של כפתור החזרה לתפריט בכל ציור מחדש (למקרה שהמסך שונה)
        if (menuButton != null) {
            menuButton.setBounds(getWidth() - 140, 2, 120, 26);
        }

        g.setColor(Color.GREEN);
        g.fillRect(goal.x, goal.y, goal.width, goal.height);

        for (Wall w : mazeWalls) if (w != null) w.draw(g);
        for (Asteroid a : asteroids) if (a != null) a.draw(g);

        player.draw(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), 30);

        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, 30, getWidth(), 30);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Level: " + currentLevel + " | Lives: " + lives + " | Time: " + timeLeft, 20, 21);

        if (!hasStartedMoving && !isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String startMsg = isHebrew ? "לחץ על אחד החצים כדי להתחיל!" : "Press any arrow key to start!";
            g.drawString(startMsg, getWidth() / 2 - (isHebrew ? 160 : 180), getHeight() / 2);
        }

        if (isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", getWidth() / 2 - 80, getHeight() / 2);
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
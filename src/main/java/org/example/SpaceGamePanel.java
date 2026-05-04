package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SpaceGamePanel extends JPanel implements Runnable {

    // =========================================
    // --- קבועים (Constants) לתחזוקה קלה ---
    // =========================================

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int TOP_BAR_HEIGHT = 30;
    private static final int MIN_RANDOM_BOUND = 1;

    // הגדרות שחקן (חללית) - גדלים מוגדלים
    private static final int PLAYER_START_WIDTH = 65;
    private static final int PLAYER_START_HEIGHT = 75;
    private static final int PLAYER_SPEED = 5;
    private static final int STARTING_LIVES = 3;

    // הגדרות יעד (Goal)
    private static final int GOAL_WIDTH = 40;
    private static final int GOAL_HEIGHT = 40;
    private static final int GOAL_OFFSET_X = 70;
    private static final int GOAL_OFFSET_Y = 90;

    // הגדרות זמנים ופריימים
    private static final int FPS_SLEEP_MS = 16;
    private static final int FRAMES_PER_SECOND = 60;
    private static final int BASE_TIME = 15;
    private static final int TIME_MULT_NUM = 45;
    private static final int TIME_MULT_DEN = 50;

    // הגדרות שלבים ומצבי משחק
    private static final int MAX_LEVEL = 50;
    private static final int STARTING_LEVEL = 1;
    private static final int MODE_NORMAL = 1;
    private static final int MODE_RANDOM = 2;
    private static final int MODE_MANUAL = 3;

    // הגדרות חומות (Walls)
    private static final int WALL_THICKNESS = 25;
    private static final int WALL_BASE_COUNT = 2;
    private static final int WALL_LEVEL_DIVISOR = 12;
    private static final int WALL_MAX_LENGTH = 2000;
    private static final int WALL_GAP_BASE = 90;
    private static final int WALL_GAP_RANDOM_ADD = 100;
    private static final int WALLS_PER_SEGMENT = 2;

    private static final int WALL_VERT_START_X = 120;
    private static final int WALL_VERT_SEGMENT_SUB = 200;
    private static final int WALL_VERT_GAP_MIN_Y = 50;
    private static final int WALL_VERT_GAP_SUB = 300;

    private static final int WALL_HORIZ_START_Y = 100;
    private static final int WALL_HORIZ_SEGMENT_SUB = 150;
    private static final int WALL_HORIZ_GAP_MIN_X = 150;
    private static final int WALL_HORIZ_GAP_SUB = 300;

    // הגדרות טילים (Rockets)
    private static final int ROCKET_BASE_COUNT = 1;
    private static final int ROCKET_LEVEL_DIVISOR = 10;
    private static final int ROCKET_SIZE = 85;
    private static final int ROCKET_BASE_SPEED = 2;
    private static final int ROCKET_SPEED_DIVISOR = 15;
    private static final int ROCKET_SPAWN_X_BASE = 200;
    private static final int ROCKET_SPAWN_Y_BASE = 100;
    private static final int ROCKET_SPAWN_X_SUB = 400;
    private static final int ROCKET_SPAWN_Y_SUB = 300;

    // הגדרות גופנים וטקסטים
    private static final int FONT_SIZE_SMALL = 14;
    private static final int FONT_SIZE_MEDIUM = 16;
    private static final int FONT_SIZE_LARGE = 24;
    private static final int FONT_SIZE_PAUSE = 40;
    private static final int TEXT_OFFSET_HEB = 160;
    private static final int TEXT_OFFSET_ENG = 180;
    private static final int TEXT_OFFSET_PAUSE = 80;
    private static final int STATS_TEXT_X = 20;
    private static final int STATS_TEXT_Y = 21;

    // הגדרות כפתור תפריט
    private static final int MENU_BTN_WIDTH = 120;
    private static final int MENU_BTN_HEIGHT = 26;
    private static final int MENU_BTN_OFFSET_X = 140;
    private static final int MENU_BTN_OFFSET_Y = 2;

    // =========================================

    private Spaceship spaceship; // שונה לאות קטנה למען תקינות קוד
    private Wall[] mazeWalls;
    private Rocket[] rockets;
    private Rectangle goal;

    private Thread gameThread;
    private volatile boolean isRunning = false;
    private boolean isPaused = false;
    private boolean up, down, left, right;
    private boolean hasStartedMoving = false;

    private int timeLeft;
    private int frameCounter = 0;
    private int currentLevel = STARTING_LEVEL;
    private int lives = STARTING_LIVES;
    private int gameMode = MODE_NORMAL;
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
        menuButton.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_SMALL));
        menuButton.setBackground(Color.DARK_GRAY);
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusable(false);

        menuButton.addActionListener(e -> {
            isRunning = false;
            if (onMenuReturn != null) onMenuReturn.run();
        });

        this.add(menuButton);
    }

    public void setGameMode(int mode) { this.gameMode = mode; }

    public void setHebrew(boolean isHebrew) {
        this.isHebrew = isHebrew;
        if (menuButton != null) {
            menuButton.setText(isHebrew ? "תפריט ראשי" : "Main Menu");
        }
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
            int segment = (w - WALL_VERT_SEGMENT_SUB) / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int x = WALL_VERT_START_X + (i * segment);
                int gap = WALL_GAP_BASE + rand.nextInt(WALL_GAP_RANDOM_ADD);
                int gapY = WALL_VERT_GAP_MIN_Y + rand.nextInt(Math.max(MIN_RANDOM_BOUND, h - WALL_VERT_GAP_SUB));
                mazeWalls[i * WALLS_PER_SEGMENT] = new Wall(x, 0, WALL_THICKNESS, gapY);
                mazeWalls[i * WALLS_PER_SEGMENT + 1] = new Wall(x, gapY + gap, WALL_THICKNESS, Math.max(h, WALL_MAX_LENGTH));
            }
        } else {
            int segment = (h - WALL_HORIZ_SEGMENT_SUB) / wallCount;
            for (int i = 0; i < wallCount; i++) {
                int y = WALL_HORIZ_START_Y + (i * segment);
                int gap = WALL_GAP_BASE + rand.nextInt(WALL_GAP_RANDOM_ADD);
                int gapX = WALL_HORIZ_GAP_MIN_X + rand.nextInt(Math.max(MIN_RANDOM_BOUND, w - WALL_HORIZ_GAP_SUB));
                mazeWalls[i * WALLS_PER_SEGMENT] = new Wall(0, y, gapX, WALL_THICKNESS);
                mazeWalls[i * WALLS_PER_SEGMENT + 1] = new Wall(gapX + gap, y, Math.max(w, WALL_MAX_LENGTH), WALL_THICKNESS);
            }
        }

        int rocketCount = ROCKET_BASE_COUNT + (level / ROCKET_LEVEL_DIVISOR);
        rockets = new Rocket[rocketCount];
        for (int i = 0; i < rocketCount; i++) {
            int speed = ROCKET_BASE_SPEED + (level / ROCKET_SPEED_DIVISOR);
            rockets[i] = new Rocket(
                    ROCKET_SPAWN_X_BASE + rand.nextInt(Math.max(MIN_RANDOM_BOUND, w - ROCKET_SPAWN_X_SUB)),
                    ROCKET_SPAWN_Y_BASE + rand.nextInt(Math.max(MIN_RANDOM_BOUND, h - ROCKET_SPAWN_Y_SUB)),
                    ROCKET_SIZE, ROCKET_SIZE, speed, speed);
        }

        timeLeft = BASE_TIME + (level * TIME_MULT_NUM / TIME_MULT_DEN);
        up = down = left = right = false;
        hasStartedMoving = false;
    }

    public void startGame() {
        if (gameMode == MODE_NORMAL) {
            currentLevel = STARTING_LEVEL;
        } else if (gameMode == MODE_RANDOM) {
            currentLevel = new Random().nextInt(MAX_LEVEL) + STARTING_LEVEL;
        } else if (gameMode == MODE_MANUAL) {
            String msg = isHebrew ? "אנא הקלד מספר שלב (1-50):" : "Please enter level number (1-50):";
            String title = isHebrew ? "בחירת שלב ידנית" : "Manual Level Selection";

            String val = JOptionPane.showInputDialog(null, msg, title, JOptionPane.QUESTION_MESSAGE);
            try {
                if (val != null && !val.trim().isEmpty()) {
                    currentLevel = Math.max(STARTING_LEVEL, Math.min(MAX_LEVEL, Integer.parseInt(val)));
                } else {
                    currentLevel = STARTING_LEVEL;
                }
            } catch (NumberFormatException e) {
                currentLevel = STARTING_LEVEL;
            }
        }

        lives = STARTING_LIVES;
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
            try { Thread.sleep(FPS_SLEEP_MS); } catch (InterruptedException e) {}
        }
    }

    private void update() {
        if (!hasStartedMoving && (up || down || left || right)) {
            hasStartedMoving = true;
        }

        // חישוב המיקום העתידי של השחקן (הלוגיקה המקורית שלך)
        int nx = spaceship.getX();
        int ny = spaceship.getY();

        if (up) ny -= PLAYER_SPEED;
        if (down) ny += PLAYER_SPEED;
        if (left) nx -= PLAYER_SPEED;
        if (right) nx += PLAYER_SPEED;

        Rectangle next = new Rectangle(nx, ny, spaceship.getWidth(), spaceship.getHeight());
        boolean hitWall = false;

        // בדיקת פגיעה בקירות - בלולאת for רגילה
        for (int i = 0; i < mazeWalls.length; i++) {
            Wall w = mazeWalls[i];
            if (w != null && next.intersects(w.getBounds())) {
                hitWall = true;
            }
        }

        // אם לא פגע בקיר, נזיז את השחקן (עם הגבלות גבולות המסך)
        if (!hitWall) {
            spaceship.setX(Math.max(0, Math.min(getWidth() - spaceship.getWidth(), nx)));
            spaceship.setY(Math.max(TOP_BAR_HEIGHT, Math.min(getHeight() - spaceship.getHeight(), ny)));
        }

        if (hasStartedMoving) {
            Rectangle currentShipBounds = spaceship.getBounds();

            // עדכון טילים - בלולאת for רגילה
            for (int i = 0; i < rockets.length; i++) {
                Rocket r = rockets[i];
                if (r != null) {
                    // הפעלת לוגיקת המעקב
                    r.trackPlayer(spaceship.getX(), spaceship.getY());

                    // בדיקת התנגשות בין השחקן לטיל
                    if (currentShipBounds.intersects(r.getBounds())) {
                        handleDeath(isHebrew ? "נפגעת מטיל!" : "Hit by a rocket!");
                        return;
                    }
                }
            }

            frameCounter++;
            if (frameCounter >= FRAMES_PER_SECOND) {
                timeLeft--;
                frameCounter = 0;
                if (timeLeft <= 0) handleDeath(isHebrew ? "נגמר הזמן!" : "Time's up!");
            }
        }

        // הגעה ליעד
        if (spaceship.getBounds().intersects(goal)) {
            if (currentLevel < MAX_LEVEL) {
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
        if (menuButton != null) {
            menuButton.setBounds(getWidth() - MENU_BTN_OFFSET_X, MENU_BTN_OFFSET_Y, MENU_BTN_WIDTH, MENU_BTN_HEIGHT);
        }
        g.setColor(Color.GREEN);
        g.fillRect(goal.x, goal.y, goal.width, goal.height);

        // ציור חומות - בלולאת for רגילה
        for (int i = 0; i < mazeWalls.length; i++) {
            Wall w = mazeWalls[i];
            if (w != null) w.draw(g);
        }

        // ציור טילים - בלולאת for רגילה
        for (int i = 0; i < rockets.length; i++) {
            Rocket r = rockets[i];
            if (r != null) r.draw(g);
        }

        spaceship.draw(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), TOP_BAR_HEIGHT);
        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, TOP_BAR_HEIGHT, getWidth(), TOP_BAR_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_MEDIUM));
        g.drawString("Level: " + currentLevel + " | Lives: " + lives + " | Time: " + timeLeft, STATS_TEXT_X, STATS_TEXT_Y);

        if (!hasStartedMoving && !isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_LARGE));
            String startMsg = isHebrew ? "לחץ על אחד החצים כדי להתחיל!" : "Press any arrow key to start!";
            g.drawString(startMsg, getWidth() / 2 - (isHebrew ? TEXT_OFFSET_HEB : TEXT_OFFSET_ENG), getHeight() / 2);
        }

        if (isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_PAUSE));
            g.drawString("PAUSED", getWidth() / 2 - TEXT_OFFSET_PAUSE, getHeight() / 2);
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
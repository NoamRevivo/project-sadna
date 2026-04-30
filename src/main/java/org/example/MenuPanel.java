package org.example;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private JLabel titleLabel, authorLabel;
    private JTextArea instructionsArea;
    private JButton modeButton, langButton;
    private boolean isHebrew = true;

    // ה-Interface המעודכן שמקבל גם שפה
    public interface GameModeListener {
        void onModeSelected(int mode, boolean isHebrew);
    }
    private GameModeListener listener;

    public MenuPanel(GameModeListener listener) {
        this.listener = listener;
        this.setBackground(new Color(20, 20, 60));
        this.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(new Color(20, 20, 60));
        top.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        authorLabel = new JLabel("", SwingConstants.CENTER);
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        authorLabel.setForeground(Color.WHITE);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(titleLabel);
        top.add(authorLabel);

        instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(new Color(20, 20, 60));
        instructionsArea.setForeground(Color.WHITE);
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 22));
        instructionsArea.setMargin(new Insets(30, 30, 30, 30));

        modeButton = new JButton();
        modeButton.addActionListener(e -> showSelection());

        langButton = new JButton("English / עברית");
        langButton.addActionListener(e -> {
            isHebrew = !isHebrew;
            updateTexts();
        });

        JPanel south = new JPanel();
        south.setBackground(new Color(20, 20, 60));
        south.add(modeButton);
        south.add(langButton);

        updateTexts();
        this.add(top, BorderLayout.NORTH);
        this.add(instructionsArea, BorderLayout.CENTER);
        this.add(south, BorderLayout.SOUTH);
    }

    private void showSelection() {
        String title = isHebrew ? "בחר מצב משחק" : "Select Game Mode";
        String message = isHebrew ? "בחר את סוג המשימה:" : "Choose mission type:";
        Object[] options = isHebrew ?
                new Object[]{"משחק רגיל", "שלב אקראי", "בחירת שלב"} :
                new Object[]{"Normal Game", "Random Level", "Select Level"};

        int res = JOptionPane.showOptionDialog(this, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (res != -1) {
            listener.onModeSelected(res + 1, isHebrew);
        }
    }

    private void updateTexts() {
        if (isHebrew) {
            titleLabel.setText("משחק מבוך בחלל");
            authorLabel.setText("פותח על ידי נועם רביבו");
            modeButton.setText("בחר מצב משחק להתחלה");
            instructionsArea.setText("הוראות:\n1. נווט עם החיצים.\n2. הימנע מקירות ואסטרואידים.\n3. המטרה: להגיע ליעד הירוק.\n4. בכל שלב ניתן ללחוץ על מקש P להפסקה.");
            instructionsArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            titleLabel.setText("Space Maze");
            authorLabel.setText("Developed by Noam Revivo");
            modeButton.setText("Select Game Mode to Start");
            instructionsArea.setText("Instructions:\n1. Move with Arrow Keys.\n2. Avoid walls and asteroids.\n3. Reach the Green Goal.\n4. Press P to Pause.");
            instructionsArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
    }
}
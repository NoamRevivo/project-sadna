package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MenuPanel extends JPanel
{
    private static final String BACKGROUND_IMAGE_PATH = "/wallpaper 1.png";
    private static final Color FALLBACK_BG_COLOR = new Color(20, 20, 60);
    private static final Color TITLE_COLOR = Color.CYAN;
    private static final Color TEXT_COLOR = Color.WHITE;
    // הגדרות עיצוב - גופנים
    private static final String FONT_FAMILY = "Arial";
    private static final int TITLE_FONT_SIZE = 50;
    private static final int AUTHOR_FONT_SIZE = 20;
    private static final int INSTRUCT_FONT_SIZE = 22;
    // הגדרות ריווח ושוליים (פאדינג)
    private static final int TITLE_PADDING_TOP = 30;
    private static final int TITLE_PADDING_BOTTOM = 10;
    private static final int INSTRUCT_MARGIN = 30;
    // עברית
    private static final String HEB_TITLE = "משחק מבוך בחלל";
    private static final String HEB_AUTHOR = "פותח על ידי נועם רביבו";
    private static final String HEB_BTN_START = "בחר מצב משחק להתחלה";
    private static final String HEB_INSTRUCTIONS = "הוראות:\n1. נווט עם החיצים.\n2. הימנע מקירות ואסטרואידים.\n3. המטרה: להגיע ליעד הירוק.\n4. בכל שלב ניתן ללחוץ על מקש P להפסקה.";
    private static final String HEB_DIALOG_TITLE = "בחר מצב משחק";
    private static final String HEB_DIALOG_MSG = "בחר את סוג המשימה:";
    private static final Object[] HEB_OPTIONS = {"משחק רגיל", "שלב אקראי", "בחירת שלב"};
    // אנגלית
    private static final String ENG_TITLE = "Space Maze";
    private static final String ENG_AUTHOR = "Developed by Noam Revivo";
    private static final String ENG_BTN_START = "Select Game Mode to Start";
    private static final String ENG_INSTRUCTIONS = "Instructions:\n1. Move with Arrow Keys.\n2. Avoid walls and asteroids.\n3. Reach the Green Goal.\n4. Press P to Pause.";
    private static final String ENG_DIALOG_TITLE = "Select Game Mode";
    private static final String ENG_DIALOG_MSG = "Choose mission type:";
    private static final Object[] ENG_OPTIONS = {"Normal Game", "Random Level", "Select Level"};
    private static final String LANG_BUTTON_TEXT = "English / עברית";
    private JLabel titleLabel, authorLabel;
    private JTextArea instructionsArea;
    private JButton modeButton, langButton;
    private boolean isHebrew = true;
    private Image backgroundImage;
    public interface GameModeListener
    {
        void onModeSelected(int mode, boolean isHebrew);
    }
    private GameModeListener listener;
    public MenuPanel(GameModeListener listener)
    {
        this.listener = listener;
        this.setLayout(new BorderLayout());
        try
        {
            URL imageUrl = getClass().getResource(BACKGROUND_IMAGE_PATH);
            if (imageUrl != null)
            {
                backgroundImage = ImageIO.read(imageUrl);
            } else
            {
                System.err.println("Could not find background image: " + BACKGROUND_IMAGE_PATH);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(TITLE_PADDING_TOP, 0, TITLE_PADDING_BOTTOM, 0));
        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, TITLE_FONT_SIZE));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        authorLabel = new JLabel("", SwingConstants.CENTER);
        authorLabel.setFont(new Font(FONT_FAMILY, Font.ITALIC, AUTHOR_FONT_SIZE));
        authorLabel.setForeground(TEXT_COLOR);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(titleLabel);
        top.add(authorLabel);
        instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setOpaque(false);
        instructionsArea.setForeground(TEXT_COLOR);
        instructionsArea.setFont(new Font(FONT_FAMILY, Font.PLAIN, INSTRUCT_FONT_SIZE));
        instructionsArea.setMargin(new Insets(INSTRUCT_MARGIN, INSTRUCT_MARGIN, INSTRUCT_MARGIN, INSTRUCT_MARGIN));
        modeButton = new JButton();
        modeButton.addActionListener(e -> showSelection());
        langButton = new JButton(LANG_BUTTON_TEXT);
        langButton.addActionListener(e -> {
            isHebrew = !isHebrew;
            updateTexts();
        });
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(modeButton);
        south.add(langButton);
        updateTexts();
        this.add(top, BorderLayout.NORTH);
        this.add(instructionsArea, BorderLayout.CENTER);
        this.add(south, BorderLayout.SOUTH);
    }
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(FALLBACK_BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    private void showSelection() {
        String title = isHebrew ? HEB_DIALOG_TITLE : ENG_DIALOG_TITLE;
        String message = isHebrew ? HEB_DIALOG_MSG : ENG_DIALOG_MSG;
        Object[] options = isHebrew ? HEB_OPTIONS : ENG_OPTIONS;

        int res = JOptionPane.showOptionDialog(this, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (res != -1) {
            listener.onModeSelected(res + 1, isHebrew);
        }
    }

    private void updateTexts() {
        if (isHebrew) {
            titleLabel.setText(HEB_TITLE);
            authorLabel.setText(HEB_AUTHOR);
            modeButton.setText(HEB_BTN_START);
            instructionsArea.setText(HEB_INSTRUCTIONS);
            instructionsArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            titleLabel.setText(ENG_TITLE);
            authorLabel.setText(ENG_AUTHOR);
            modeButton.setText(ENG_BTN_START);
            instructionsArea.setText(ENG_INSTRUCTIONS);
            instructionsArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
    }
}
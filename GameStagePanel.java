package networktest1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;

public class GameStagePanel extends JPanel {
    private static final int NUM_BOXES = 3;
    protected JTextField[] inputFields = new JTextField[NUM_BOXES];
    private JLabel[] imageLabels = new JLabel[NUM_BOXES];
    protected String[] displayedTexts;
    private Timer stageTimer;
    private int timeLeft;
    private int stageSeconds;
    private JButton checkButton;
    private JButton exitButton;
    private PrintWriter out;
    private String playerName;
    private Image backgroundImage;
    private int currentTotalScore;
    private boolean answered = false;
    private ScoreListener scoreListener;
    private boolean boxesHidden = false;
    private static GameStagePanel currentActiveStage;
    private JTextArea scoresArea;

    public interface ScoreListener {
        void onScoreUpdated(int newScore);
    }

    public GameStagePanel(String[] colors, String[] textLabels, int stageSeconds, PrintWriter out, String playerName, int currentTotalScore, ScoreListener scoreListener) {
        this.stageSeconds = stageSeconds;
        this.timeLeft = stageSeconds;
        this.displayedTexts = textLabels;
        this.out = out;
        this.playerName = playerName;
        this.currentTotalScore = currentTotalScore;
        this.scoreListener = scoreListener;

        // تحميل صورة الخلفية
        backgroundImage = new ImageIcon(getClass().getResource("/networktest1/images/17.png")).getImage();
        setLayout(new BorderLayout());

        // لوحة الألوان
        JPanel colorPanel = new JPanel(new GridLayout(1, NUM_BOXES, 10, 10));
        colorPanel.setOpaque(false);
        for (int i = 0; i < NUM_BOXES; i++) {
            JLabel label = new JLabel(textLabels[i], SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(Color.BLACK);
            label.setBackground(Color.decode(colors[i]));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            label.setPreferredSize(new Dimension(100, 100));
            imageLabels[i] = label;
            colorPanel.add(label);
        }

        // لوحة المدخلات
        JPanel inputPanel = new JPanel(new GridLayout(1, NUM_BOXES, 10, 10));
        inputPanel.setOpaque(false);
        for (int i = 0; i < NUM_BOXES; i++) {
            inputFields[i] = new JTextField();
            inputFields[i].setFont(new Font("Arial", Font.PLAIN, 14));
            inputFields[i].setEnabled(false);
            inputFields[i].setPreferredSize(new Dimension(100, 30));
            inputPanel.add(inputFields[i]);
            inputFields[i].getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCheckButtonState(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCheckButtonState(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCheckButtonState(); }
            });
        }

        // إضافة اللوحات في الواجهة
        JPanel centerVertical = new JPanel();
        centerVertical.setLayout(new BoxLayout(centerVertical, BoxLayout.Y_AXIS));
        centerVertical.setOpaque(false);
        centerVertical.add(colorPanel);
        centerVertical.add(Box.createRigidArea(new Dimension(0, 10)));
        centerVertical.add(inputPanel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 0, 0, 0);
        centerWrapper.add(centerVertical, gbc);
        add(centerWrapper, BorderLayout.CENTER);

        // زر التحقق من الإجابة
        checkButton = new JButton("تحقق من الإجابات");
        checkButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkButton.setEnabled(false);
        checkButton.addActionListener(e -> checkAnswers(true));

        // زر الخروج
        exitButton = new JButton("خروج");
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> {
            if (out != null && playerName != null) {
                out.println("LEAVE " + playerName);
            }
            ((Window) this.getTopLevelAncestor()).dispose();
        });

        // إضافة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(checkButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // عرض النتائج في الجهة اليمنى
        scoresArea = new JTextArea();
        scoresArea.setEditable(false);
        scoresArea.setFont(new Font("Arial", Font.BOLD, 14));
        scoresArea.setForeground(Color.WHITE);
        scoresArea.setOpaque(false);

        JPanel scoresPanel = new JPanel(new BorderLayout());
        scoresPanel.setOpaque(false);
        JLabel scoreTitle = new JLabel("النتائج الحالية", JLabel.CENTER);
        scoreTitle.setForeground(Color.WHITE);
        scoreTitle.setFont(new Font("Arial", Font.BOLD, 15));
        scoresPanel.add(scoreTitle, BorderLayout.NORTH);
        scoresPanel.add(new JScrollPane(scoresArea), BorderLayout.CENTER);

        add(scoresPanel, BorderLayout.EAST);

        currentActiveStage = this;
    }

    // بدء التوقيت
    public void startStage() {
        Timer delayTimer = new Timer(5000, e -> {
            for (JLabel label : imageLabels) label.setVisible(false);
            boxesHidden = true;

            if (out != null && playerName != null) {
                out.println("START_STAGE_TIMER " + playerName);
                out.flush();
            }

            for (JTextField input : inputFields) {
                input.setEnabled(true);
            }

            answered = false;
            updateCheckButtonState();
            startTimer();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    // تحديث حالة زر التحقق
    private void updateCheckButtonState() {
        if (!boxesHidden) {
            checkButton.setEnabled(false);
            return;
        }
        for (JTextField field : inputFields) {
            if (field.getText().trim().isEmpty()) {
                checkButton.setEnabled(false);
                return;
            }
        }
        checkButton.setEnabled(true);
    }

    // بدء العداد الزمني
    private void startTimer() {
        stageTimer = new Timer(1000, e -> {
            timeLeft--;
            repaint();
            if (timeLeft <= 0 && !answered) {
                stageTimer.stop();
                checkAnswers(false);
            }
        });
        stageTimer.start();
    }

    // التحقق من الإجابات
    protected void checkAnswers(boolean manualTriggered) {
        if (answered) return;
        answered = true;

        int correctCount = 0;
        for (int i = 0; i < NUM_BOXES; i++) {
            String userInput = inputFields[i].getText().trim().toLowerCase();
            String correctAnswer = displayedTexts[i].trim().toLowerCase();
            if (userInput.equals(correctAnswer)) {
                correctCount++;
            }
        }

        if (stageTimer != null) stageTimer.stop();

        currentTotalScore += correctCount;
        if (scoreListener != null) {
            scoreListener.onScoreUpdated(currentTotalScore);
        }

        if (out != null && playerName != null) {
            out.println("SCORE " + playerName + ":" + correctCount);
            out.flush();

            if (manualTriggered && correctCount == NUM_BOXES) {
                out.println("NEXT_STAGE_REQUEST " + playerName);
                out.flush();
            }
        }
    }

    // تحديث اللوحة الخاصة بالنتائج
    public void updateScoreBoard(String scoreText) {
        if (scoresArea != null) {
            scoresArea.setText(scoreText);
        }
    }

    // رسم الخلفية
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("الوقت المتبقي: " + timeLeft + "s", getWidth() - 220, 25);
        g.drawString("سكورك: " + currentTotalScore, getWidth() - 400, 25);
    }

    public static void nextStageSignal() {
        if (currentActiveStage != null) {
            SwingUtilities.invokeLater(() -> {
                currentActiveStage.repaint();
            });
        }
    }
}

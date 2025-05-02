package networktest1;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class GameManagerPanel extends JPanel {
    private int currentStage = 1;
    private int currentTotalScore = 0;
    private PrintWriter out;
    private String playerName;
    private JFrame frame;
    private GameStagePanel currentStagePanel;

    public GameManagerPanel(JFrame frame, PrintWriter out, String playerName) {
        this.out = out;
        this.playerName = playerName;
        this.frame = frame;
        setLayout(new BorderLayout());
        startStage1();
    }

    private void startStage1() {
        removeAll();
        String[] colors = {"#FF0000", "#00FF00", "#0000FF"};
        String[] texts = {"اصفر", "اسود", "وردي"};

        currentStagePanel = new GameStagePanel(colors, texts, 15, out, playerName, currentTotalScore, new GameStagePanel.ScoreListener() {
            @Override
            public void onScoreUpdated(int newScore) {
                currentTotalScore = newScore;
            }
        });
        add(currentStagePanel, BorderLayout.CENTER);
        currentStagePanel.startStage();
        revalidate();
        repaint();
    }

    private void startStage2() {
        removeAll();
        String[] colors = {"#FFFF00", "#FFA500", "#FFC0CB"};
        String[] texts = {"ازرق", "اخضر", "رمادي"};

        currentStagePanel = new GameStagePanel(colors, texts, 10, out, playerName, currentTotalScore, new GameStagePanel.ScoreListener() {
            @Override
            public void onScoreUpdated(int newScore) {
                currentTotalScore = newScore;
            }
        });
        add(currentStagePanel, BorderLayout.CENTER);
        currentStagePanel.startStage();
        revalidate();
        repaint();
    }

    private void startStage3() {
        removeAll();
        String[] colors = {"#A52A2A", "#800080", "#008080"};
        String[] texts = {"ابيض", "ازرق", "اصفر"};

        currentStagePanel = new GameStagePanel(colors, texts, 7, out, playerName, currentTotalScore, new GameStagePanel.ScoreListener() {
            @Override
            public void onScoreUpdated(int newScore) {
                currentTotalScore = newScore;
            }
        });
        add(currentStagePanel, BorderLayout.CENTER);
        currentStagePanel.startStage();
        revalidate();
        repaint();
    }

    public void nextStage() {
        currentStage++;

        if (currentStage == 2) {
            startStage2();
        } else if (currentStage == 3) {
            startStage3();
        } else if (currentStage == 4) {
            JOptionPane.showMessageDialog(frame, "انتهت جميع المراحل! ❤\nسكورك النهائي: " + currentTotalScore);
            frame.dispose();
        }
    }

    public void updateScoresDisplay(String scoreText) {
        if (currentStagePanel != null) {
            currentStagePanel.updateScoreBoard(scoreText);
        }
    }
}
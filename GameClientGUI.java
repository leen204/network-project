package networktest1;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClientGUI {
    private JFrame frame;
    private JTextField playerNameField;
    private JTextArea connectedPlayersArea;
    private JPanel scorePanel;
    private PrintWriter out;
    private Socket socket;
    private JButton playButton;
    protected WaitingRoomGUI waitingRoom;
    private String playerName;
    private GameManagerPanel gameManagerPanel;

    public GameClientGUI() {
        frame = new JFrame("لعبة وهم");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setResizable(false);

        // تحميل الخلفية
        ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/networktest1/images/17.png"));
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setOpaque(false);

        // حقل الإدخال
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inputPanel.setOpaque(false);
        JLabel nameLabel = new JLabel("اسم اللاعب");
        nameLabel.setForeground(Color.BLACK);
        playerNameField = new JTextField(15);
        JButton joinButton = new JButton("انضم");
        inputPanel.add(joinButton);
        inputPanel.add(playerNameField);
        inputPanel.add(nameLabel);

        JPanel topWrapper = new JPanel(null);
        topWrapper.setOpaque(false);
        topWrapper.setPreferredSize(new Dimension(500, 70));
        inputPanel.setBounds(190, 30, 300, 40);
        topWrapper.add(inputPanel);

        // منطقة اللاعبين المتصلين
        connectedPlayersArea = new JTextArea();
        connectedPlayersArea.setEditable(false);
        connectedPlayersArea.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(connectedPlayersArea);
        scrollPane.setPreferredSize(new Dimension(240, 80));

        JLabel playersLabel = new JLabel("اللاعبين المتصلين", JLabel.CENTER);
        playersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        playersLabel.setForeground(Color.BLACK);

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setPreferredSize(new Dimension(260, 110));
        scrollWrapper.setOpaque(false);
        scrollWrapper.add(playersLabel, BorderLayout.NORTH);
        scrollWrapper.add(scrollPane, BorderLayout.CENTER);

        // زر اللعب
        playButton = new JButton("العب");
        playButton.setEnabled(false);

        // 🔽 تغليف كل العناصر ونزولها تحت الخلفية باستخدام margin
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0)); // هامش علوي 100 بكسل

        contentPanel.add(topWrapper);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setOpaque(false);
        centerWrapper.add(scrollWrapper);
        contentPanel.add(centerWrapper);

        JPanel bottomWrapper = new JPanel();
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(playButton);
        contentPanel.add(bottomWrapper);

        panel.add(contentPanel, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);

        // الأزرار
        joinButton.addActionListener(e -> {
            playerName = playerNameField.getText().trim();
            if (!playerName.isEmpty()) {
                connectToServer(playerName);
            }
        });

        playButton.addActionListener(e -> {
            if (waitingRoom == null) {
                waitingRoom = new WaitingRoomGUI();
            }
            out.println("PLAY " + playerName);
            waitingRoom.showWaitingRoom();
        });
    }

    private void connectToServer(String playerName) {
        try {
            socket = new Socket("localhost", 1111);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(playerName);

            new Thread(() -> {
                try {
                    Scanner in = new Scanner(socket.getInputStream());
                    while (in.hasNextLine()) {
                        String line = in.nextLine();
                        SwingUtilities.invokeLater(() -> {
                            if (line.equals("NEXT_STAGE")) {
                                if (gameManagerPanel != null) {
                                    gameManagerPanel.nextStage();
                                }
                            } else if (line.startsWith("Connected players:")) {
                                String playersOnly = line.replace("Connected players: ", "").replace(", ", "\n");
                                connectedPlayersArea.setText(playersOnly);
                                playButton.setEnabled(true);
                            } else if (line.startsWith("Waiting list:")) {
                                String listData = line.replace("Waiting list: ", "").replace(", ", "\n");
                                if (waitingRoom != null) {
                                    waitingRoom.updateWaitingList(listData);
                                }
                            } else if (line.startsWith("SCORES:")) {
                                String scoresRaw = line.replace("SCORES:", "");
                                String[] entries = scoresRaw.split(",");
                                StringBuilder builder = new StringBuilder();
                                for (String entry : entries) {
                                    String[] parts = entry.split(":");
                                    if (parts.length == 2) {
                                        builder.append(parts[0]).append(": ").append(parts[1]).append(" نقاط\n");
                                    }
                                }
                                if (gameManagerPanel != null) {
                                    gameManagerPanel.updateScoresDisplay(builder.toString());
                                }
                            } else if (line.equals("Game started!")) {
                                JOptionPane.showMessageDialog(frame, "اللعبة بدأت!");
                                if (waitingRoom != null) waitingRoom.waitingFrame.dispose();

                                JFrame gameFrame = new JFrame("واجهة اللعبة");
                                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                gameFrame.setSize(600, 400);
                                gameFrame.setResizable(false);

                                gameManagerPanel = new GameManagerPanel(gameFrame, out, playerName);
                                gameFrame.setContentPane(gameManagerPanel);
                                gameFrame.setVisible(true);
                            } else if (line.startsWith("WINNER:")) {
                                String[] parts = line.split(":");
                                if (parts.length == 3) {
                                    String winnerName = parts[1];
                                    String score = parts[2];
                                    JOptionPane.showMessageDialog(frame, "🏆 الفائز هو: " + winnerName + "\nبسكور: " + score);
                                    System.exit(0);
                                }
                            } else if (line.equals("NO_WINNER")) {
                                JOptionPane.showMessageDialog(frame, "لم يحقق أحد الفوز.");
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClientGUI::new);
    }
}

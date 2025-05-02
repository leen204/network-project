/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package networktest1;

import javax.swing.*;
import java.awt.*;

public class WaitingRoomGUI {
    JFrame waitingFrame;
    private JTextArea waitingPlayersArea;

    public WaitingRoomGUI() {
        waitingFrame = new JFrame("غرفة الانتظار");
        waitingFrame.setSize(400, 300);
        waitingFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // تحميل خلفية
        ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/networktest1/images/17.png"));
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setOpaque(false);

        // ===== محتوى النص والعنوان =====
        JLabel titleLabel = new JLabel("اللاعبون في غرفة الانتظار", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        waitingPlayersArea = new JTextArea();
        waitingPlayersArea.setEditable(false);
        waitingPlayersArea.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(waitingPlayersArea);
        scrollPane.setPreferredSize(new Dimension(250, 100));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // ===== تجميع الليبل والنص في بانل مركزي =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // مسافة بين الليبل والتكست
        contentPanel.add(scrollPane);

        // ===== وسط الشاشة =====
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(-30, 0, 0, 0); // ← نزّل المحتوى 30 بكسل من فوق
        centerPanel.add(contentPanel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);
        waitingFrame.add(panel);
    }

    public void showWaitingRoom() {
        waitingFrame.setVisible(true);
    }

    public void updateWaitingList(String players) {
        waitingPlayersArea.setText(players);
    }
}

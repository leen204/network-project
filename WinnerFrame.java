package networktest1;

import javax.swing.*;
import java.awt.*;

public class WinnerFrame extends JFrame {

    public WinnerFrame(String winnerName, int winnerScore) {
        setTitle("🏆 الفائز باللعبة!");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel winnerLabel = new JLabel(
            "<html><center>مبروووك! 🎉<br/>الفائز هو: <b>" + winnerName +
            "</b><br/>بسكور: <b>" + winnerScore + "</b></center></html>",
            SwingConstants.CENTER
        );
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(winnerLabel);
    }
}

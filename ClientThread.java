// ✅ ClientThread.java (يعرض تنبيه إذا أُلغيت اللعبة بسبب قلة اللاعبين)
package networktest1;

import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;

    public ClientThread(Socket serverSocket) throws IOException {
        this.socket = serverSocket;
        this.in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String serverMsg;

            while ((serverMsg = in.readLine()) != null) {
                System.out.println("[DEBUG] Received from server: " + serverMsg);

                if (serverMsg.contains("Game started")) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "اللعبة بدأت!");
                    });
                } else if (serverMsg.contains("Game cancelled")) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "⚠️ اللعبة أُلغيت: عدد اللاعبين غير كافٍ.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                        System.exit(0); // إغلاق البرنامج أو يمكنك استبداله بإغلاق إطار اللعبة فقط
                    });
                } else if (serverMsg.contains("Game cancelled")) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "اللعبة أُلغيت: عدد اللاعبين غير كافٍ.");
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] الاتصال بالسيرفر انقطع");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

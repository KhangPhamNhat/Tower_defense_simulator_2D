import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        DBManager.connect();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tower Defense");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LoginScreen( frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
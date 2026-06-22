import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JPanel {
    private static final Color BG_DARK = new Color(15, 25, 50);
    private static final Color BG_CARD = new Color(25, 55, 95);
    private static final Color BORDER_CLR = new Color(50, 100, 160);
    private static final Color GOLD = new Color(255, 200, 50);

    public LoginScreen(JFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //title
        JLabel title = new JLabel("TOWER DEFENSE",SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(GOLD);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title,gbc);
        //card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR,2),
                BorderFactory.createEmptyBorder(20,30,20,30)
        ));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        //username
        JLabel userLbl = new JLabel("Username");
        userLbl.setForeground(Color.WHITE);
        userLbl.setFont(new Font("Arial", Font.BOLD, 14));
        c.gridy=0;c.gridx=0;
        card.add(userLbl,c);

        JTextField userTxt = new JTextField(20);
        userTxt.setFont(new Font("Arial", Font.PLAIN, 14));
        userTxt.setForeground(Color.WHITE);
        userTxt.setBackground(new Color(10,20,40));
        userTxt.setCaretColor(Color.WHITE); //vạch thẳng đứng nhấp nháy chỉ vị trí đang gõ chữ trên màn hình
        userTxt.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        c.gridy=0;c.gridx=1;
        card.add(userTxt,c);
        //password
        JLabel passLbl = new JLabel("Password");
        passLbl.setForeground(Color.WHITE);
        passLbl.setFont(new Font("Arial", Font.BOLD, 14));
        c.gridy=1;c.gridx=0;
        card.add(passLbl,c);
        JPasswordField passTxt = new JPasswordField(20);
        passTxt.setFont(new Font("Arial", Font.PLAIN, 14));
        passTxt.setForeground(Color.WHITE);
        passTxt.setBackground(new Color(10,20,40));
        passTxt.setCaretColor(Color.WHITE);
        passTxt.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        c.gridy=1;c.gridx=1;
        card.add(passTxt,c);
        //nut login
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(new Color(50,150,50));
        loginBtn.addActionListener(e->{
            String username = userTxt.getText().trim();
            String password = new String(passTxt.getPassword());
            if(username.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(frame,"Please enter username and password");
                return;
            }
            String hashed = SecurityManager.hashPassword(password);
            int playerID = DBManager.loginPlayer(username,hashed);
            if(playerID==-1){
                JOptionPane.showMessageDialog(frame,"Invalid username or password");
            }
            else {
                frame.getContentPane().removeAll();
                frame.setContentPane(new MenuPanel(frame,playerID));
                frame.revalidate();
                frame.repaint();
            }
        });
        c.gridy=2;c.gridx=0;c.gridwidth=2;
        card.add(loginBtn,c);
        //register
        JButton resgisterBtn = new JButton("REGISTER");
        resgisterBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resgisterBtn.setForeground(Color.WHITE);
        resgisterBtn.setBackground(new Color(30,50,90));
        resgisterBtn.addActionListener(e->{
            String username = userTxt.getText().trim();
            String password = new String(passTxt.getPassword());
            if(username.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(frame,"Please enter username and password");
                return;
            }
            if(!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")){
                JOptionPane.showMessageDialog(frame,"Passwords must have at least 6 characters, including both letters and numbers.");
            return;
            }
            String hashed = SecurityManager.hashPassword(password);
            if(DBManager.registerPlayer(username,hashed)){
                //Auto unlock Archer va Cannon
                int playerID = DBManager.loginPlayer(username,hashed);
                DBManager.unlockTower(playerID,"ARCHER");
                DBManager.unlockTower(playerID,"CANNON");
                JOptionPane.showMessageDialog(frame,"Successfully registered");
                frame.getContentPane().removeAll();
                frame.setContentPane(new MenuPanel(frame,playerID));
                frame.revalidate();
                frame.repaint();
            }
            else{
                JOptionPane.showMessageDialog(frame,"Username already exists");
            }
        });
        c.gridy = 3;
        card.add(resgisterBtn,c);
        gbc.gridy = 1;gbc.gridwidth=2;
        add(card,gbc);
    }
}

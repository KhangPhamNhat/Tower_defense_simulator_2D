import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

public class ShopScreen extends JPanel {
    private static final String[] TOWERS = {"MAGE","SNIPER","FREEZE"};
    private static final int[] GEM_COSTS = {50,80,60};
    private static final Color[] COLORS = {Color.MAGENTA,Color.GREEN,Color.CYAN};
    private static final String[] DESC = {
            "Armor penetration, ignores armor",
            "Extremely long range, high damage",
            "Slows down enemies"
    };
    //mau`
    private static final Color BG_DARK = new Color(15, 25, 50);
    private static final Color BG_CARD = new Color(25, 55, 95);
    private static final Color BORDER_CLR = new Color(50, 100, 160);
    private static final Color GOLD = new Color(255, 200, 50);
    private static final Color GEMS_CLR = new Color(150, 100, 255);

    private int playerID;
    private JLabel gemsLabel;
    public ShopScreen(JFrame frame,int playerID) {
        this.playerID = playerID;
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        //title
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_DARK);
        titleBar.setBorder(BorderFactory.createEmptyBorder(15,20,10,20));

        JLabel title = new JLabel("SHOP");
        title.setFont(new Font("Arial", Font.BOLD, 35));
        title.setForeground(GOLD);

        gemsLabel = new JLabel("GEMS: "+DBManager.getGems(playerID));
        gemsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gemsLabel.setForeground(GEMS_CLR);

        titleBar.add(title,BorderLayout.WEST);
        titleBar.add(gemsLabel,BorderLayout.EAST);
        add(titleBar,BorderLayout.NORTH);
        //tower panel
        JPanel towerPanel = new JPanel(new GridLayout(TOWERS.length,1,10,10));
        towerPanel.setBackground(BG_DARK);
        towerPanel.setBorder(BorderFactory.createEmptyBorder(10,30,10,30));

        List<String> owned = DBManager.getOwnedTowers(playerID);

        for(int i = 0; i < TOWERS.length; i++) {
            final String type = TOWERS[i];
            final int cost = GEM_COSTS[i];
            boolean isOwned = owned.contains(type);
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_CLR,2),
                    BorderFactory.createEmptyBorder(10,15,10,15)
            ));
            //info
            JPanel info = new JPanel(new GridLayout(3,1));
            info.setBackground(BG_CARD);
            JLabel nameLabel = new JLabel(type);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            nameLabel.setForeground(Color.WHITE);

            JLabel descLabel = new JLabel(DESC[i]);
            descLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            descLabel.setForeground(Color.LIGHT_GRAY);

            JLabel costLabel = new JLabel(isOwned ? "Owned: " : cost + "Gems");
            costLabel.setFont(new Font("Arial", Font.BOLD, 14));
            costLabel.setForeground(isOwned ? new Color(100,255,100) : GOLD);

            info.add(nameLabel);
            info.add(descLabel);
            info.add(costLabel);

            JButton buyBtn = new JButton(isOwned ? "OWNED" : "BUY");
            buyBtn.setPreferredSize(new Dimension(100,60));
            buyBtn.setBackground(isOwned ?new Color(50,80,50):new Color(50,150,50));
            buyBtn.setForeground(Color.WHITE);
            buyBtn.setFont(new Font("Arial", Font.BOLD, 14));
            buyBtn.setEnabled(!isOwned);
            buyBtn.addActionListener(e -> {
                if(DBManager.buyTower(playerID,type,cost)){
                    JOptionPane.showMessageDialog(frame,"You have successfully bought "+type+" to "+cost+"!");
                    frame.getContentPane().removeAll();
                    frame.setContentPane(new ShopScreen(frame,playerID));
                    frame.revalidate();
                    frame.repaint();
                }
                else{
                    JOptionPane.showMessageDialog(frame,"Not enough GEMS");
                }
            });
            card.add(info,BorderLayout.CENTER);
            card.add(buyBtn,BorderLayout.EAST);
            towerPanel.add(card);
        }
        add(towerPanel,BorderLayout.CENTER);
        JButton backBtn = new JButton("BACK");
        backBtn.setFont(new Font("Arial", Font.BOLD, 16));
        backBtn.setBackground(new Color(30,50,90));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        backBtn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.setContentPane(new MenuPanel(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(BG_DARK);
        bottomPanel.add(backBtn);
        add(bottomPanel,BorderLayout.SOUTH);

    }
}

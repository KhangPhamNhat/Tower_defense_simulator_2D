import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;

public class LeaderboardScreen extends JPanel {
    private static final Color BG_DARK = new Color(15, 25, 50);
    private static final Color BG_CARD = new Color(25, 55, 95);
    private static final Color BORDER_CLR = new Color(50, 100, 160);
    private static final Color GOLD = new Color(255, 200, 50);
    public LeaderboardScreen(JFrame frame,int playerID) {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        //Title
        JLabel title = new JLabel("LEADERBOARD",SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 35));
        title.setForeground(GOLD);
        title.setBorder(BorderFactory.createEmptyBorder(20,0,10,0));
        add(title,BorderLayout.NORTH);
        //tab panel cho tung mode
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_DARK);
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        //story mode tung map
        String[] maps = {"Forest Path","Swamp Crossing","Dark Castle"};
        for(int i =0; i<maps.length;i++){
            tabs.addTab("Story - "+maps[i], createMapPanel("STORY",i+1));
        }
        //infinity mode
        for(int i =0; i<maps.length;i++){
            tabs.addTab("Inf - " + maps[i], createMapPanel("INFINITY", i+1));
        }
        //online mode
        tabs.addTab("Online PVP",createTable("ONLINE",0,"NORMAL"));
        add(tabs,BorderLayout.CENTER);
        //back
        JButton back = new JButton("BACK");
        back.setFont(new Font("Arial", Font.BOLD, 16));
        back.setBackground(new Color(30, 50, 90));
        back.setForeground(Color.WHITE);
        back.addActionListener(e->{
            frame.getContentPane().removeAll();
            frame.setContentPane(new MenuPanel(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });
        JButton refresh = new JButton("REFRESH");
        refresh.setFont(new Font("Arial", Font.BOLD, 16));
        refresh.setBackground(new Color(50, 150, 50));
        refresh.setForeground(Color.WHITE);
        refresh.addActionListener(e->{
            frame.getContentPane().removeAll();
            frame.setContentPane(new LeaderboardScreen(frame,playerID));
            frame.revalidate();
            frame.repaint();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(BG_DARK);
        bottom.add(back);
        bottom.add(refresh);
        add(bottom,BorderLayout.SOUTH);
    }
    private JPanel createTable(String mode,int mapID,String diff){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        String scoreHeader = mode.equals("STORY")?"Time (s)":"Score";
        String[] columns = {"Rank","Player",scoreHeader};
        Object[][]data = getLeaderboardData(mode,mapID,diff);

        JTable table = new JTable(data,columns) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    table.setDefaultRenderer(Object.class,(t,val,sel,foc,row,col)->{
        JLabel label = new JLabel(val!=null?val.toString():"",SwingConstants.CENTER);
        label.setOpaque(true);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setBackground(BG_CARD);
        label.setForeground(Color.WHITE);
        return label;
    });
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(BG_DARK);
    scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 30, 10, 30),
            BorderFactory.createLineBorder(BORDER_CLR, 2)
    ));
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    panel.add(scroll,BorderLayout.CENTER);
    return panel;
    }
    private JPanel createMapPanel(String mode, int mapId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        JTabbedPane diffTabs = new JTabbedPane();
        diffTabs.setBackground(BG_DARK);
        diffTabs.setForeground(Color.WHITE);
        diffTabs.setFont(new Font("Arial", Font.BOLD, 12));

        diffTabs.addTab("EASY", createTable(mode, mapId, "EASY"));
        diffTabs.addTab("NORMAL", createTable(mode, mapId, "NORMAL"));
        diffTabs.addTab("HARD", createTable(mode, mapId, "HARD"));

        // Mặc định chọn NORMAL
        diffTabs.setSelectedIndex(1);

        panel.add(diffTabs, BorderLayout.CENTER);
        return panel;
    }
    private Object[][] getLeaderboardData(String mode,int mapID,String diff){
        try{
            ResultSet rs = DBManager.getLeaderboard(mode,mapID,diff);
            Object[][] data = new Object[50][3];
            int row = 0;
            while(rs!= null && rs.next() && row<50){
                data[row][0]="#"+(row+1);
                data[row][1]=rs.getString("username");
                data[row][2]=rs.getInt("best_score");
                row++;
            }
            // fill doan trong
            for(int i = row;i<50;i++){
                data[i][0]="#"+(i+1);
                data[i][1]="---";
                data[i][2]="---";
            }
            return data;
        }
        catch(Exception e){
            e.printStackTrace();
            return new Object[50][3];
        }
    }
}

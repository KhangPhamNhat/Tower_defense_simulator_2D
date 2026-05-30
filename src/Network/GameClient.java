import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
public class GameClient implements Runnable{
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private final String host;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int playerID;
    private WaveManager waveManager;
    private GameEngine gameEngine; // để update game khi nhận message
    public GameClient(GameEngine gameEngine,String host,int playerID) {
        this.gameEngine = gameEngine;
        this.playerID = playerID;
        this.host = host;
    }
    public void connect(){
        try{
            socket = new Socket(host,PORT);
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("connected to "+HOST+":"+PORT);
            new Thread(this).start(); // message tu server gui den client
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        try{
            String message;
            while((message = in.readLine())!=null){
                handleMessage(message);
            }
        }
        catch (IOException e){
            System.out.println("Disconnected from server!");
        }
    }
    private void handleMessage(String message){
        String [] parts = message.split(":",2);
        String action = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        switch(action){
            case "START":
                SwingUtilities.invokeLater(()->{
                    gameEngine.start();
                    if(waveManager!=null){waveManager.startNextWave();}
                });
                break;
            case "INCOMING_ENEMY":
                //spawn mob tu doi thu
                String[] enemyTypes = data.split(",");
                for(String enemy:enemyTypes){
                    gameEngine.spawnEnemies(enemy.trim(),enemy.startsWith("BOSS"));
                }
                break;
            case "OPPONENT_PLACE":
                // Hiển thị tower opponent đặt (chỉ visual)
                System.out.println("Opponent placed tower at: " + data);
                break;
            case "WINNER":
                SwingUtilities.invokeLater(() -> {
                    gameEngine.stop();
                    JOptionPane.showMessageDialog(null,
                            data.equals("0") ? "Player 1 WINS!" : "Player 2 WINS!");
                });
                for(Window window:Window.getWindows()){
                    if(window instanceof JFrame){
                        JFrame frame = (JFrame) window;
                        frame.getContentPane().removeAll();
                        frame.setContentPane(new MenuPanel(frame,playerID));
                        frame.revalidate();
                        frame.repaint();
                        break;
                    }
                }
                break;
            case "OPPONENT_DISCONNECT":
                System.out.println("Opponent disconnected");
                gameEngine.stop();
                break;
        }
    }
    //gui action len server
    public void sendPlaceTower(int row,int col, String towerType){
        out.println("PLACE_TOWER:" + towerType+", " +row+", "+col);
    }
    public void sendSpawnEnemy(String enemyType){
        out.println("SPAWN_ENEMY:" +enemyType);
    }
    public void sendGameOver(){
        out.println("GAME_OVER");
    }
    public void disconnect(){
        try{
            if(socket!=null){socket.close();}
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public void setWaveManager(WaveManager waveManager){
        this.waveManager = waveManager;
    }
}

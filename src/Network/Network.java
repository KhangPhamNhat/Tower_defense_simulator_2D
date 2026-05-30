import java.net.*;
import java.io.*;
import java.util.*;

public class Network {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private ClientHandler[] players = new ClientHandler[2];
    private int playerCount = 0;
    private boolean gameStarted = false;

    public void start(){
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            while(playerCount < 2){
                Socket socket = serverSocket.accept();
                players[playerCount]= new ClientHandler(socket,playerCount,this);
                new Thread(players[playerCount]).start();
                playerCount++;
                System.out.println("Player " + playerCount + " connected");
            }
            startGame();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    private void startGame(){
        gameStarted = true;
        broadcast("START");
        System.out.println("Game started");
    }
    public synchronized void broadcast(String message){
        for(ClientHandler p : players){
            if(p!=null){
                p.send(message);
            }
        }
    }
    public synchronized void sendToOpponent(int senderID, String message){
        int opponentID = senderID == 0?1:0;// if else if oppo = 1 thi sender = 0 va else...
        if(players[opponentID]!=null){
            players[opponentID].send(message);
        }
    }
    public synchronized void playerDisconnected(int playerID){
        if(players[playerID]!=null){
            broadcast("Player " + playerID + " disconnected");
        }
    }
    public static void main(String[] args){
        new Network().start();
    }
}
class ClientHandler implements Runnable{
    private Socket socket;
    private int playerID;
    private Network server;
    private PrintWriter out;
    private BufferedReader in;
    public ClientHandler (Socket socket, int playerID, Network server){
        this.socket = socket;
        this.playerID = playerID;
        this.server = server;
        try{
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        try {
            String message;
            while((message= in.readLine())!=null){
                handMessage(message);
            }
        }catch (IOException e){
            System.out.println("Player " + playerID + " disconnected");
        }
        finally {
            server.playerDisconnected(playerID);
        }
    }
    private void handMessage(String message){
        // format action:data
        String[] parts = message.split(":",2);
        String action = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        switch (action){
            case "PLACE_TOWER":
                // gui opponent tower minh dat
                server.sendToOpponent(playerID,"OPPONENT_PLACE:"+data);
                break;
            case "SPAWN_ENEMY":
                // gui enemy sang map cua doi thu
                server.sendToOpponent(playerID,"INCOMING_ENEMY:"+data);
                break;
            case "GAME_OVER":
                server.broadcast("WINNER:" +(playerID==0?"1":"0"));
                break;
            default:
                server.broadcast(message);
        }
    }
    public void send(String message){
        out.println(message);
    }
}

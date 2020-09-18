import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class socketExample {
    private static final int port = 50000;

    public static void main(String[] args) {
        ServerSocket serversocket;
        try{
            serversocket = new ServerSocket(port);
            System.out.println("<server> listening port: 50000");

            while (true) {
                Socket sock = serversocket.accept();
                System.out.println("connect! ("+sock.getInetAddress()+")");

                new user(sock).start();
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }
}

class user extends Thread{
    public final Socket sock;
    private int num;
    user(Socket soc){
        this.sock = soc;
    }

    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        JSONObject obj;

        try {
            while (true) {
                InputStream input = this.sock.getInputStream();
                if (input.available() != 0) {
                    byte[] byteArray = new byte[input.available()];
                    int size = input.read(byteArray);

                    if (size == -1) {
                        break;
                    }
                    String recvmsg = new String(byteArray, 0, size, StandardCharsets.UTF_8); // JSON -> String
                    System.out.println("receive: "+ recvmsg);

                    obj = (JSONObject)parser.parse(recvmsg);
                    num = ((Long)obj.get("num")).intValue() + 1;

                    String message = "{\"name\":\"server\", \"contents\":\"hello client\", \"num\": \""+num+"\"}"; // String
                    obj = (JSONObject)parser.parse(message); // String -> Json
                    System.out.println("send: "+ message);

                    PrintWriter out = new PrintWriter(new OutputStreamWriter(this.sock.getOutputStream())); // send to client
                    out.println(obj.toString());
                    out.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("[server]클라이언트가 퇴장하였습니다.: " + e);
        }
    }
}
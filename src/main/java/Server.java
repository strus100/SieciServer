import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server {

    public static int getPlayersNumber() {
        return playersNumber;
    }

    public static int playersNumber = 0;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(3333);
        System.out.println("Oczekuję na połączenie");

        Socket socket1 = serverSocket.accept();
        Socket socket2 = serverSocket.accept();
        /*Socket socket3 = serverSocket.accept();
        Socket socket4 = serverSocket.accept();
        Socket socket5 = serverSocket.accept();
        */
        new EchoThread(socket1).start();
        new EchoThread(socket2).start();
        /*new EchoThread(socket3).start();
        new EchoThread(socket4).start();
        new EchoThread(socket5).start();
        */
        System.out.println("Gramy");
    }
}


class EchoThread extends Thread {
    protected Socket socket;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        try {
            //Komunikacja od klienta
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            //Komunikacja do klienta
            DataOutputStream outToClient = new DataOutputStream(
                    socket.getOutputStream());

            ArrayList<String> list = new ArrayList<String>();
            StringTokenizer stringTokenizer;
            String recvfrom;
            String sendto;

            while (true) {

                recvfrom = inFromClient.readLine();
                stringTokenizer = new StringTokenizer(recvfrom);

               while (stringTokenizer.hasMoreTokens()) {
                   list.add(stringTokenizer.nextToken());
                   }

               switch (list.get(0)) {
                    case "LOGIN":
                        System.out.println("Key: " + recvfrom);
                        sendto = "START" + '\n';
                        outToClient.writeBytes(sendto);
                        list.clear();
                        continue;
                    case "PLANSZA":
                        System.out.println("Key: " + recvfrom);
                        sendto = "PLANSZA OK" + '\n';
                        outToClient.writeBytes(sendto);
                        list.clear();
                        continue;
                    case "PASS":
                        System.out.println("Key: "+ recvfrom);
                        sendto = "PASS OK" + '\n';
                        outToClient.writeBytes(sendto);
                        list.clear();
                        continue;
                    case "ATAK":
                        System.out.println("Key: "+ recvfrom);
                        sendto = "ATAK OK" + '\n';
                        outToClient.writeBytes(sendto);
                        list.clear();
                        continue;
                    default:
                        System.out.println("Key: " + recvfrom);
                        sendto = "ERROR" + '\n';
                        outToClient.writeBytes(sendto);
                        list.clear();
                        continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
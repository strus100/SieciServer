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
       // Socket socket2 = serverSocket.accept();
    //    Socket socket3 = serverSocket.accept();
    //    Socket socket4 = serverSocket.accept();
    //    Socket socket5 = serverSocket.accept();


        EchoThread thread1 = new EchoThread(socket1);
//        EchoThread thread2 = new EchoThread(socket2);
  //      EchoThread thread3 = new EchoThread(socket3);
  //      EchoThread thread4 = new EchoThread(socket4);
  //      EchoThread thread5 = new EchoThread(socket5);


        while (true){
            thread1.run();
  //          thread2.run();
    //        thread3.run();
      //      thread4.run();
         //   thread5.run();
        }


        //TODO for zatrzymuj wątki i wznawiaj tylko gdy ID == i
        }
}


class EchoThread extends Thread {
    protected Socket socket;
    public int guard = 0;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;

    }

   synchronized public void run() {

        try {
            ArrayList<String> list = new ArrayList<String>();
            StringTokenizer stringTokenizer;
            String recvfrom;
            String sendto;


            //Komunikacja od klienta
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            //Komunikacja do klienta
            DataOutputStream outToClient = new DataOutputStream(
                    socket.getOutputStream());

            if(guard == 0) {
                sendto = "POLACZONO" + '\n';
                outToClient.writeBytes(sendto);
                guard++;
            }
            else{
                sendto = "TWOJ RUCH" + '\n';
                outToClient.writeBytes(sendto);

            }
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
                                break;
                            case "PLANSZA":
                                System.out.println("Key: " + recvfrom);
                                sendto = "PLANSZA OK" + '\n';
                                outToClient.writeBytes(sendto);
                                list.clear();
                                break;
                            case "PASS":
                                System.out.println("Key: " + recvfrom);
                                sendto = "PASS OK" + '\n';
                                outToClient.writeBytes(sendto);
                                list.clear();
                                break;
                            case "ATAK":
                                System.out.println("Key: " + recvfrom);
                                sendto = "ATAK OK" + '\n';
                                outToClient.writeBytes(sendto);
                                list.clear();
                                break;
                            default:
                                System.out.println("Key: " + recvfrom);
                                sendto = "ERROR" + '\n';
                                outToClient.writeBytes(sendto);
                                list.clear();
                                break;
                }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
   }
}
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

public class Server {

    public static CountDownLatch latch = new CountDownLatch(1);
    public static String value = " ";

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(3333);
        System.out.println("Oczekuję na połączenie");

        Socket socket1 = serverSocket.accept();
        Socket socket2 = serverSocket.accept();
        Socket socket3 = serverSocket.accept();
        //    Socket socket4 = serverSocket.accept();
        //    Socket socket5 = serverSocket.accept();

        EchoThread thread1 = new EchoThread(socket1);
        EchoThread thread2 = new EchoThread(socket2);
        EchoThread thread3 = new EchoThread(socket3);
        //      EchoThread thread4 = new EchoThread(socket4);
        //      EchoThread thread5 = new EchoThread(socket5);


        while (true){

            thread1.run();
            EchoThread.clientValue += value;
            thread2.run();
            EchoThread.clientValue += value;
            thread3.run();
            EchoThread.clientValue += value;
            //      thread4.run();
            //   thread5.run();
        }
    }
}


class EchoThread extends Thread {
    protected Socket socket;
    public int guard = 0;
    static String clientValue = "";

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;

    }

    synchronized public void run() {
        int userGuard = 0;
        boolean userGuardSwitch = true;

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
                outToClient.writeBytes("POLACZONO" + '\n');
            }
            else{

                outToClient.writeBytes(clientValue + "TWOJ RUCH" + '\n');
            }

            do {

            recvfrom = inFromClient.readLine();
            stringTokenizer = new StringTokenizer(recvfrom);

            while (stringTokenizer.hasMoreTokens()) {
                list.add(stringTokenizer.nextToken());
            }


                switch (list.get(0)) {
                    case "LOGIN":
                        System.out.println("Key: " + recvfrom);
                        if(guard == 0){
                            outToClient.writeBytes("START" + '\n');
                            guard++;
                        }
                        else {
                            outToClient.writeBytes("ERROR" + '\n');
                        }
                       list.clear();

                        userGuardSwitch = false;
                        break;
                    case "PASS":
                        System.out.println("Key: " + recvfrom);
                        outToClient.writeBytes("PASS OK" + '\n');
                        userGuardSwitch = false;
                        list.clear();
                        Server.value = "";
                        Server.latch.countDown();

                        break;
                    case "ATAK":
                        System.out.println("Key: " + recvfrom);
                        outToClient.writeBytes("ATAK OK" + '\n');
                        list.clear();

                        sendto = "WYNIK DO WSZYSTKICH ";
                        Server.value = sendto;
                        Server.latch.countDown();

                        outToClient.writeBytes("BY ZAKONCZYC KOLEJKE WPISZ PASS" + '\n');
                        userGuardSwitch = true;
                        break;
                    default:
                        System.out.println("Key: " + recvfrom);
                        outToClient.writeBytes("ERROR" + '\n');
                        list.clear();

                        userGuard++;
                        userGuardSwitch = false;
                        break;
                }
                if (userGuard == 50)
                    socket.close();
           }while(userGuardSwitch);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
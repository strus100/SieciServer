import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

public class Server {
    public static Game game = new Game();

    public static CountDownLatch latch = new CountDownLatch(1);
    public static String value = " ";

    public static void main(String[] args) throws Exception {


        ServerSocket serverSocket = new ServerSocket(3333);
        System.out.println("Oczekuję na połączenie");

        Socket socket1 = serverSocket.accept();
/*
        Socket socket2 = serverSocket.accept();
        Socket socket3 = serverSocket.accept();
*/
        //Socket socket4 = serverSocket.accept();
        //    Socket socket5 = serverSocket.accept();

        EchoThread thread1 = new EchoThread(socket1,1);
/*
        EchoThread thread2 = new EchoThread(socket2,2);
        EchoThread thread3 = new EchoThread(socket3,3);
*/
        //      EchoThread thread4 = new EchoThread(socket4);
        //      EchoThread thread5 = new EchoThread(socket5);


        while (true){

            thread1.run();
            EchoThread.clientValue += value;
            /*thread2.run();
            EchoThread.clientValue += value;
            thread3.run();
            EchoThread.clientValue += value;
            //      thread4.run();
            *///   thread5.run();
        }
    }
}


class EchoThread extends Thread {
    protected Socket socket;
    public int guard = 0;
    static String clientValue = "";


    public int threadID;
    public EchoThread(Socket clientSocket, int ID) {
        this.socket = clientSocket;
        this.threadID = ID;

    }

    synchronized public void run() {
        currentThread().setName(threadID+"");

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

                /*String[] tab = {"0","0","0","0","0"};
                for (int i = 0; i < list.size() ; i++) {
                    tab[i] = list.get(i);
                }
*/
                switch (list.get(0)) {
                    case "LOGIN":
                        System.out.println("Key: " + recvfrom);
                        if(guard == 0){
                            outToClient.writeBytes("START " + threadID + " 1" + '\n');
                            guard++;

                            for (int i = 0; i < 25 ; i++) {
                                outToClient.writeBytes(
                                        Server.game.gameView().get(i).toString()
                                        + '\n');
                            }
                        }
                        else {
                            outToClient.writeBytes("ERROR" + '\n');
                            userGuard++;
                        }
                       list.clear();

                        userGuardSwitch = false;
                        break;
                    case "PASS":
                        System.out.println("Key: " + recvfrom);
                        outToClient.writeBytes("PASS OK" + '\n');
                        userGuardSwitch = false;
                        list.clear();

                        break;
                    case "ATAK":
                        System.out.println("Key: " + recvfrom);

                        if(list.size() > 4)
                        {
                            outToClient.writeBytes("ATAK OK" + '\n');
                            //TODO atakowanie pól


                            sendto = "WYNIK DO WSZYSTKICH ";

                            Server.value = sendto;
                            Server.latch.countDown();
                        }
                        else
                        {
                            outToClient.writeBytes("ERROR" + '\n');
                            userGuard++;
                        }


                        list.clear();

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
                {
                    threadID = 0;
                    socket.close();
                }
           }while(userGuardSwitch);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}

class Game {
    String[][] gameField = new String[6][6];
    int userID = 0;
    int dices = 0;
    Game(){
        System.out.println(
        EchoThread.currentThread().getName());
        for (int i = 1; i < 6 ; i++) {
            for (int j = 1; j < 6; j++) {
                gameField[i][j] = userID + " " + dices;
            }
        }

    }

    public List gameView(){
        List view = new ArrayList<>();
        for (int i = 1; i < 6 ; i++) {
            for (int j = 1; j < 6; j++) {
                view.add("PLANSZA " + i + " " + j +
                        " " + gameField[i][j]);
            }
        }

        return view;
    }

    public void attack(int xFrom, int yFrom , int xTo, int yTo, int threadID){
        List listAttack = new ArrayList();

        int oppositeID;
        int oppositeDices;

        StringTokenizer stringTokenizer = new StringTokenizer(gameField[xFrom][yFrom]);

        while (stringTokenizer.hasMoreTokens()) {

            listAttack.add(stringTokenizer.nextToken());
        }


        userID = Integer.parseInt(listAttack.get(0).toString());
        dices = Integer.parseInt(listAttack.get(1).toString());
        listAttack.clear();

        stringTokenizer = new StringTokenizer(gameField[xTo][yTo]);

        while (stringTokenizer.hasMoreTokens()) {

            listAttack.add(stringTokenizer.nextToken());
        }

        oppositeID = Integer.parseInt(listAttack.get(0).toString());
        oppositeDices = Integer.parseInt(listAttack.get(1).toString());

        int dicePower;

        if(dices > 1 && userID == 1){
            if(
                    xTo == xFrom - 1 && yTo == yFrom||
                    xTo == xFrom + 1 && yTo == yFrom||
                    yTo == yFrom - 1 && xTo == xFrom||
                    yTo == yFrom + 1 && xTo == xFrom
            ){
                System.out.println("ATAK");

                //TODO RAND wynik ataku

                /*
                *   Dwie listy z wynikami na pętlach for dla każdego pola losuj
                *   (int)(Math.random() * 6) + 1;
                *
                * 
                * */


                int random =  (int)(Math.random() * 6) + 1;

                int result ;



                gameField[xFrom][yFrom]= threadID + " " + 1;
                gameField[xTo][yTo] = threadID + " " + (dices - 1);
            }
            else {
                System.out.println("ERROR");
            }


        }
        else{
            System.out.println("ERROR");
        }

    }

}
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

// TODO dwie sekundy bezczynności, lista wyników, atakowanie wielkorotne?,zapis logów do pliku, dodawnie kostek.
public class Server {
    public static Game game = new Game();

    public static CountDownLatch latch = new CountDownLatch(1);
    public static String value = " ";

    public static void main(String[] args) throws Exception {

        for (int i = 1; i <6 ; i++) {
            game.newField(i);
        }


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
            if (thread1.threadID != 0) {
                thread1.run();
                EchoThread.clientValue += value;
            }
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
    public final int maxThread = 1;
    static String clientValue = "";
    public int numberGamers = 0;
    public int threadID;


    int tura = 1;
    int round = 0;
    int userGuard = 0;

    public EchoThread(Socket clientSocket, int ID) {
        this.socket = clientSocket;
        this.threadID = ID;

    }

    synchronized public void run() {

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

            if(round == 0) {
                outToClient.writeBytes("POLACZONO" + '\n');
            }
            else{
                outToClient.writeBytes(clientValue + '\n');
                plansza(outToClient);
                 }

            do {

                outToClient.writeBytes("TWOJ RUCH" + '\n' );

                recvfrom = inFromClient.readLine();
            stringTokenizer = new StringTokenizer(recvfrom);

            System.out.println("Key: " + recvfrom);

            while (stringTokenizer.hasMoreTokens()) {
                list.add(stringTokenizer.nextToken());
            }



                switch (list.get(0)) {
                    case "LOGIN":

                        if(round == 0){
                           String login = list.get(1);
                            outToClient.writeBytes("START " + threadID + " 1" + '\n');

                            numberGamers++;

                        }
                        else {
                            outToClient.writeBytes("ERROR" + '\n');
                            userGuard++;
                            round--;
                        }
                       list.clear();

                        userGuardSwitch = false;
                        break;
                    case "PASS":
                        outToClient.writeBytes("PASS OK" + '\n');
                        userGuardSwitch = false;
                        list.clear();

                        break;
                    case "ATAK":

                        if(list.size() > 4)
                        {
                            sendto = Server.game.attack(
                                    Integer.parseInt(list.get(1)),
                                    Integer.parseInt(list.get(2)),
                                    Integer.parseInt(list.get(3)),
                                    Integer.parseInt( list.get(4)),
                                    threadID);

                            System.out.println(sendto);

                            if(sendto == "ERROR"){
                                outToClient.writeBytes("ERROR" + '\n');
                                userGuard++;
                            }
                            else {

                                outToClient.writeBytes("ATAK OK" + '\n');
                                Server.value = sendto + " ";
                                Server.latch.countDown();
                            }
                        }
                        else
                        {
                            outToClient.writeBytes("ERROR" + '\n');
                            userGuard++;
                        }
                        list.clear();
                        userGuardSwitch = true;
                        break;
                    default:
                        outToClient.writeBytes("ERROR" + '\n');
                        list.clear();

                        userGuard++;
                        userGuardSwitch = false;
                        break;
                }
                if (userGuard == 50)
                {
                    threadID = 0;
                    outToClient.writeBytes("TURA " + tura + numberGamers + '\n');
                    numberGamers--;
                    socket.close();
                }
           }while(userGuardSwitch);

            if(threadID == maxThread && round != 0 ){
                Server.value = "KONIEC RUNDY ";
                Server.latch.countDown();


            }

            round++;
            if(numberGamers == 0 /*FIXME 1 */ || round == 100){
                outToClient.writeBytes("TURA " + tura + " " + numberGamers + '\n');
                    tura++;

            }



        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void plansza(DataOutputStream outToClient) throws IOException {
        List view = Server.game.gameView();
        for (int i = 0; i < 25 ; i++) {
            outToClient.writeBytes(
                    view.get(i).toString()
                            + '\n');
        }


    }


}

class Game {
    String[][] gameField = new String[6][6];
    int userID = 0;
    int dices = 0;
    Game(){

        for (int i = 1; i < 6 ; i++) {
            for (int j = 1; j < 6; j++) {
                gameField[i][j] = userID + " " + dices;
            }
        }

    }

    public void addField(int threadID){

        StringTokenizer stringTokenizer =
                new StringTokenizer(gameField[1][1]);

        List listAttack = new ArrayList();

        while (stringTokenizer.hasMoreTokens()) {
            listAttack.add(stringTokenizer.nextToken());
        }

        userID = Integer.parseInt(listAttack.get(0).toString());
        dices += Integer.parseInt(listAttack.get(1).toString());

        if (userID == threadID){


        }

        listAttack.clear();

    }

    public void newField(int threadID) {
        int randX;
        int randY;
        int guard = 0;
        while(guard < 3) {
            randX = (int)(Math.random() * 5) + 1;
            randY = (int)(Math.random() * 5) + 1;
            if (String.valueOf(gameField[randX][randY]).equals("0 0")){
                gameField[randX][randY] = threadID + " " + 6; /// FIXME
                guard++;
            }
        }
        guard = 0;
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

    public int score(int threadID) {
        dices = 0;
        for (int i = 1; i < 6; i++) {
            for (int j = 1; j < 6; j++) {
                StringTokenizer stringTokenizer =
                        new StringTokenizer(gameField[i][j]);
                List listAttack = new ArrayList();

                while (stringTokenizer.hasMoreTokens()) {
                    listAttack.add(stringTokenizer.nextToken());
                }
                userID = Integer.parseInt(listAttack.get(0).toString());
                if (userID == threadID) {
                    dices += Integer.parseInt(listAttack.get(1).toString());
                }
                listAttack.clear();

            }


        }
    return dices;
    }

    public String attack(int xFrom, int yFrom , int xTo, int yTo, int threadID){
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
        listAttack.clear();

        if(dices > 1 && userID == threadID){
            if(
                    xTo == xFrom - 1 && yTo == yFrom||
                    xTo == xFrom + 1 && yTo == yFrom||
                    yTo == yFrom - 1 && xTo == xFrom||
                    yTo == yFrom + 1 && xTo == xFrom
            ){
                System.out.println("ATAK");
                String wynik = "WYNIK ";
                int result = 0 ;
                int roll = 0;

                for (int i = 0; i < dices ; i++) {
                    roll =(int)(Math.random() * 6) + 1;
                    result += roll;
                    listAttack.add(roll);

                }

                wynik += "GRACZ " + threadID + " " + dices + " " + listAttack + " ";
                listAttack.clear();

                for (int i = 0; i <oppositeDices ; i++) {
                    roll = (int)(Math.random() * 6) + 1;
                    result -= roll;
                    listAttack.add(roll);
                }

                wynik += "Obrońca " + oppositeID + " " + oppositeDices + " " + listAttack + " ";
                listAttack.clear();

                if(result > 0){
                    gameField[xFrom][yFrom]= threadID + " " + 1;
                    gameField[xTo][yTo] = threadID + " " + (dices - 1);
                    System.out.println("wygrałeś");
                    wynik += threadID ;
                    return wynik;
                }
                else{
                    gameField[xFrom][yFrom]= threadID + " " + 1;
                    System.out.println("przegrałeś");
                    wynik += oppositeID;
                    return wynik;
                }
            }
            else {
                return "ERROR";
            }
        }
        else{
            return "ERROR";
        }
    }
}
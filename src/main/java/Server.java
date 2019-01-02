import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;

// TODO dwie sekundy bezczynności, ?lista wyników.
public class Server {
    public static Game game = new Game();

    public static CountDownLatch latch = new CountDownLatch(1);
    public static String value = "";
    public static Map<String, Integer> score = new HashMap<String, Integer>();
    public static String scoreFinal;
    public static String[] logins = new String[5];

    public static void main(String[] args) throws Exception {
        boolean start = true;

        for (int i = 1; i < 6; i++) {
            game.newField(i);
        }



        ServerSocket serverSocket = new ServerSocket(3333);
        System.out.println("Oczekuję na połączenie");

        Socket socket1 = serverSocket.accept();
        Socket socket2 = serverSocket.accept();
  /*      Socket socket3 = serverSocket.accept();
        Socket socket4 = serverSocket.accept();
            Socket socket5 = serverSocket.accept();
*/
        EchoThread thread1 = new EchoThread(socket1, 1);
        EchoThread thread2 = new EchoThread(socket2, 2);
/*        EchoThread thread3 = new EchoThread(socket3,3);
          EchoThread thread4 = new EchoThread(socket4,4);
              EchoThread thread5 = new EchoThread(socket5,5);
  */

        while (
                !socket1.isClosed() ||
                !socket2.isClosed()/* ||
                !socket3.isClosed() ||
                !socket4.isClosed() ||
                !socket5.isClosed()
        */
           ) {

            scoreFinal = score.toString();
            System.out.println("WYNIKI " + scoreFinal);
            if (thread1.threadID != 0) {
                thread1.run();
                EchoThread.clientValue = value;
            }

            if (thread2.threadID != 0) {
                thread2.run();
                EchoThread.clientValue = value;

            }
/*
            if (thread3.threadID != 0) {
                thread3.run();
                EchoThread.clientValue += value;
            }

            if (thread4.threadID != 0) {
                thread4.run();
                EchoThread.clientValue += value;
            }

            if (thread5.threadID != 0) {
                thread5.run();
                EchoThread.clientValue += value;
            }
    */

            if(start == true){
            for (int i = 0; i < 5; i++) {
                score.put(logins[i],0);
             }
                start = false;
            }
        }
    }
}


class EchoThread extends Thread {

    protected Socket socket;
    public final int maxThread = 2;
    public final int LICZBA_RUND = 20;
    public final int LICZBA_TUR = 3;

    static String clientValue = "";
    public static int numberGamers = 0;
    public int threadID;


    int tura = 1;
    int round = 0;
    int userGuard = 0;
    PrintWriter zapis;

    public EchoThread(Socket clientSocket, int ID) {
        this.socket = clientSocket;
        this.threadID = ID;

        try {
            String name = "log" + threadID + ".txt";
            this.zapis = new PrintWriter(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


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

            if (round == 0) {
                zapis.println("Server to thread " + threadID + ": POLACZONO");
                outToClient.writeBytes("POLACZONO" + '\n');
            } else {
                zapis.println("Server to thread " + threadID + ": " + clientValue);
                zapis.println("Server to thread " + threadID + ": PLANSZA");
                outToClient.writeBytes(clientValue + '\n');
                plansza(outToClient);
            }

            do {
                zapis.println("Server to thread " + threadID + " TWOJ RUCH");
                outToClient.writeBytes("TWOJ RUCH" + '\n');

                recvfrom = inFromClient.readLine();
                recvfrom.toUpperCase();
                stringTokenizer = new StringTokenizer(recvfrom);


                zapis.println("Thread " + threadID + ": " + recvfrom);

                while (stringTokenizer.hasMoreTokens()) {
                    list.add(stringTokenizer.nextToken());
                }


                switch (list.get(0)) {
                    case "LOGIN":

                        if (round == 0) {
                            Server.logins[threadID - 1] = list.get(1);
                            zapis.println("Server to thread " + threadID + ": START" + threadID + "1");
                            outToClient.writeBytes("START " + threadID + " 1" + '\n');

                            numberGamers++;

                        } else {
                            zapis.println("Server to thread " + threadID + ": ERROR");

                            outToClient.writeBytes("ERROR" + '\n');
                            userGuard++;
                            round--;
                        }
                        list.clear();

                        userGuardSwitch = false;
                        break;
                    case "PASS":

                        zapis.println("Server to thread " + threadID + ": OK");

                        outToClient.writeBytes("OK" + '\n');
                        userGuardSwitch = false;
                        list.clear();

                        break;
                    case "ATAK":

                        if (list.size() > 4) {
                            sendto = Server.game.attack(
                                    Integer.parseInt(list.get(1)),
                                    Integer.parseInt(list.get(2)),
                                    Integer.parseInt(list.get(3)),
                                    Integer.parseInt(list.get(4)),
                                    threadID);

                            System.out.println(sendto);

                            if (sendto == "ERROR") {

                                zapis.println("Server to thread " + threadID + ": ERROR");

                                outToClient.writeBytes("ERROR" + '\n');
                                userGuard++;
                            } else {

                                zapis.println("Server to thread " + threadID + ": OK");

                                outToClient.writeBytes(sendto + "OK" + '\n');

                                Server.value = sendto + " ";
                                Server.latch.countDown();
                            }
                        } else {

                            zapis.println("Server to thread " + threadID + ": ERROR");

                            outToClient.writeBytes("ERROR" + '\n');
                            userGuard++;
                        }
                        list.clear();
                        userGuardSwitch = true;
                        break;
                    default:

                        zapis.println("Server to thread " + threadID + ": ERROR");

                        outToClient.writeBytes("ERROR" + '\n');
                        list.clear();

                        userGuard++;
                        userGuardSwitch = false;
                        break;
                }
                if (userGuard == 50) {
                    threadID = 0;
                    zapis.println("Server to thread " + threadID + ": TURA" + tura + numberGamers);

                    outToClient.writeBytes("TURA " + tura + numberGamers + '\n');
                    numberGamers--;
                    socket.close();
                }
            } while (userGuardSwitch);


            zapis.println("Server to thread " + threadID + ": KONIEC RUNDY");
            outToClient.writeBytes("KONIEC RUNDY" + '\n');

            if (threadID == maxThread && round != 0) {
                for (int i = 1; i < 6; i++) {
                    Server.game.addField(i);
                }
            }


            if ((numberGamers == 1 && round != 0) || round == LICZBA_RUND) {
                //NOWA TURA
                zapis.println("Server to thread " + threadID + ": TURA " + tura + " " + numberGamers);
                outToClient.writeBytes("TURA " + tura + " " + numberGamers + '\n');

                if (threadID == maxThread) {
                    for (int i = 1; i <= maxThread ; i++) {
                                Server.game.addScore(Server.score,i);
                    }

                    Server.game.ResetGame();
                    Server.value = "";

                    for (int i = 1; i < 6; i++) {
                        Server.game.newField(i);
                    }
                }

                if (tura == LICZBA_TUR) {
                    zapis.println("Server to thread " + threadID + ": KONIEC");
                    outToClient.writeBytes("KONIEC " + '\n');
                    zapis.close();
                    socket.close();
                }
                round = 0;
                tura++;
            }

            round++;


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void plansza(DataOutputStream outToClient) throws IOException {
        List view = Server.game.gameView();
        for (int i = 0; i < 25; i++) {
            outToClient.writeBytes(
                    view.get(i).toString()
                            + '\n');
        }


    }


}

class Game {
    String[][] gameField = new String[6][6];
    int userID;
    int dices;
    Random r = new Random();

    Game() {
        ResetGame();

    }

    public void ResetGame() {
        userID = 0;
        dices = 0;
        for (int i = 1; i < 6; i++) {
            for (int j = 1; j < 6; j++) {
                gameField[i][j] = userID + " " + dices;
            }
        }
    }

    public List StringTokenizer(int i, int j) {
        StringTokenizer stringTokenizer =
                new StringTokenizer(gameField[i][j]);

        List listAttack = new ArrayList();

        while (stringTokenizer.hasMoreTokens()) {
            listAttack.add(stringTokenizer.nextToken());
        }

        return listAttack;
    }

    public void addField(int threadID) {

        dices = 0;
        List fieldsX = new ArrayList();
        List fieldsY = new ArrayList();

        List listAttack;


        for (int i = 1; i < 6; i++) {
            for (int j = 1; j < 6; j++) {
                listAttack = StringTokenizer(i, j);
                userID = Integer.parseInt(listAttack.get(0).toString());

                if (userID == threadID) {
                    dices++;
                    fieldsX.add(i);
                    fieldsY.add(j);
                }
                listAttack.clear();
            }
        }
        int guard = 0;

        while (dices > 0 && guard < 5) {

            int rand = r.nextInt(fieldsX.size());
            listAttack = StringTokenizer(
                    (int) fieldsX.get(rand),
                    (int) fieldsY.get(rand)
            );


            guard++;


            int temp = Integer.parseInt(listAttack.get(1).toString());
            while (temp < 8 && dices > 0) {
                temp++;
                dices--;
                guard = 0;
            }
            gameField[(int) fieldsX.get(rand)]
                    [(int) fieldsY.get(rand)] = threadID + " " + temp;


        }

    }

    public void newField(int threadID) {
        int randX;
        int randY;
        int guard = 0;
        while (guard < 2) {
            randX = (int) (Math.random() * 5) + 1;
            randY = (int) (Math.random() * 5) + 1;
            if (String.valueOf(gameField[randX][randY]).equals("0 0")) {
                gameField[randX][randY] = threadID + " " + 2;
                guard++;
            }
        }
        guard = 0;
    }

    public void addScore(Map score, int threadID) {
        String login = Server.logins[threadID - 1];
        int temp;
        if(score.containsKey(login)){
            temp = (int) score.get(login);
            temp += myScore(threadID);
            score.replace(login,temp);
        }
    }

    public List gameView() {
        List view = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            for (int j = 1; j < 6; j++) {
                view.add("PLANSZA " + i + " " + j +
                        " " + gameField[i][j]);
            }
        }

        return view;
    }

    public int myScore(int threadID) {
        dices = 0;
        for (int i = 1; i < 6; i++) {
            for (int j = 1; j < 6; j++) {
                List listAttack = StringTokenizer(i, j);

                userID = Integer.parseInt(listAttack.get(0).toString());
                if (userID == threadID) {
                    dices += Integer.parseInt(listAttack.get(1).toString());
                }
                listAttack.clear();
            }
        }


        return dices;
    }

    public String attack(int xFrom, int yFrom, int xTo, int yTo, int threadID) {
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

        if (dices > 1 && userID == threadID) {
            if (
                    xTo == xFrom - 1 && yTo == yFrom ||
                            xTo == xFrom + 1 && yTo == yFrom ||
                            yTo == yFrom - 1 && xTo == xFrom ||
                            yTo == yFrom + 1 && xTo == xFrom
            ) {
                System.out.println("ATAK");
                String wynik = "WYNIK ";
                int result = 0;
                int roll = 0;

                for (int i = 0; i < dices; i++) {
                    roll = (int) (Math.random() * 6) + 1;
                    result += roll;
                    listAttack.add(roll);

                }

                wynik += "GRACZ " + threadID + " " + dices + " " + listAttack + " ";
                listAttack.clear();

                for (int i = 0; i < oppositeDices; i++) {
                    roll = r.nextInt(7) + 1;

                    result -= roll;
                    listAttack.add(roll);
                }

                wynik += "Obrońca " + oppositeID + " " + oppositeDices + " " + listAttack + " ";
                listAttack.clear();

                if (result > 0) {
                    gameField[xFrom][yFrom] = threadID + " " + 1;
                    gameField[xTo][yTo] = threadID + " " + (dices - 1);
                    System.out.println("wygrałeś");
                    wynik += threadID;
                    return wynik;
                } else {
                    gameField[xFrom][yFrom] = threadID + " " + 1;
                    System.out.println("przegrałeś");
                    wynik += oppositeID;
                    return wynik;
                }
            } else {
                return "ERROR";
            }
        } else {
            return "ERROR";
        }
    }
}
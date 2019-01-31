import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EchoThread{

    public String login;
    protected Socket socket;

    public final int LICZBA_RUND = 10;
    public final int LICZBA_TUR = 10;

    public static LinkedList clientvalue = new LinkedList();

    public int numberGamers = 0;
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

    public void end(String score){

        try {
            //Komunikacja do klienta
            DataOutputStream outToClient = new DataOutputStream(
                    socket.getOutputStream());

            zapis.println("Server to thread " + threadID + score);
            outToClient.writeBytes(score + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open() throws IOException {

        //Komunikacja od klienta
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));

        //Komunikacja do klienta
        DataOutputStream outToClient = new DataOutputStream(
                socket.getOutputStream());


        zapis.println("Server to thread " + threadID + ": POLACZONO");
        outToClient.writeBytes("POLACZONO" + '\n');

    }
    public void run() {


        clientvalue = Server.values;
        if(threadID == Server.minThread){
            Server.values.clear();
        }
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

            if (round < LICZBA_RUND && Server.maxThread > 1) {
                if (round == 0) {
                    zapis.println("Server to thread " + threadID + ": POLACZONO");
                    outToClient.writeBytes("POLACZONO" + '\n');
                } else {
                    String clientValueStr = String.valueOf(clientvalue);
                    zapis.println("Server to thread " + threadID + ": " + clientValueStr);
                    zapis.println("Server to thread " + threadID + ": PLANSZA");

                    outToClient.writeBytes(clientValueStr + '\n');
                    plansza(outToClient);
                }

                do {
                    if(round > 0 ){
                        zapis.println("Server to thread " + threadID + " TWOJ RUCH");
                        outToClient.writeBytes("TWOJ RUCH" + '\n');

                    }
                    if (round > 0) {
                        socket.setSoTimeout(2000);
                    }
                    recvfrom = inFromClient.readLine();

                    if(round > 1 ){
                        Server.game.addField(threadID);
                        Server.game.addScore(Server.score,threadID);
                    }

                    recvfrom.toUpperCase();
                    stringTokenizer = new StringTokenizer(recvfrom);
                    zapis.println("Thread " + threadID + ": " + recvfrom);

                    list.clear();
                    while (stringTokenizer.hasMoreTokens()) {
                        list.add(stringTokenizer.nextToken());
                    }

                    if (list.size() == 0)
                        list.add("null");

                    switch (list.get(0)) {
                        case "LOGIN":

                            if (round == 0) {
                                login = list.get(1);
                                zapis.println("Server to thread " + threadID + ": START" + threadID + "1");
                                outToClient.writeBytes("START " + threadID + " 1" + '\n');

                                numberGamers++;
                                for (int i = 0; i < 5; i++) {
                                    Pattern compiledPattern = Pattern.compile(Server.logins[i]);
                                    Pattern regex = Pattern.compile("[A-z]+[0-9]*");
                                    Matcher matcher = compiledPattern.matcher(list.get(1));
                                    Matcher matcher1 = regex.matcher(list.get(1));
                                    if (matcher.matches() || !matcher1.matches()) {
                                        error(outToClient);
                                        round--;
                                    }
                                }

                            } else {
                                error(outToClient);
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
                                    error(outToClient);
                                } else {
                                    zapis.println("Server to thread " + threadID + ": OK " + sendto);
                                    outToClient.writeBytes("OK " + sendto + '\n');
                                if(!Server.values.contains(sendto)) {
                                    Server.values.add(sendto);
                                }
                               //     Server.latch.countDown();
                                }
                            } else {

                                error(outToClient);
                            }
                            list.clear();
                            userGuardSwitch = true;
                            break;
                        default:

                            error(outToClient);
                            list.clear();

                            userGuard++;
                            userGuardSwitch = false;
                            break;
                    }
                    if (userGuard == 50) {
                        outToClient.writeBytes("TURA " + tura + " " + Server.errorThread + '\n');
                        errorClose();
                    }
                } while (userGuardSwitch);

if(round  != 0 && round != LICZBA_RUND) {
    zapis.println("Server to thread " + threadID + ": KONIEC RUNDY");
    outToClient.writeBytes("KONIEC RUNDY" + '\n');
}

            }
            if (Server.maxThread == 1 || round == LICZBA_RUND) {//wpisz o jeden wiecej 10 = 9 rund

                int endTura = Server.game.myRanking(Server.score, Server.logins, threadID);
                zapis.println("Server to thread " + threadID + ": TURA" + tura + " " + endTura
                );
                outToClient.writeBytes("TURA " + tura + " " + endTura + '\n');

                if (threadID == Server.maxThread) {
                    Server.game.ResetGame();
                    for (int i = 1; i < 6; i++) {
                        Server.game.newField(i);
                    }
                    Server.game.randomField();
                }

                if (tura == LICZBA_TUR) {/*

                    Server.game.finalScore(Server.score,Server.logins,Server.finalScore);*/
                    String end = Server.game.sortedFinalScore(Server.finalScore, Server.logins) + "";

                    zapis.println("Server to thread " + threadID + ": KONIEC " + end);
                    outToClient.writeBytes("KONIEC " + end + '\n');
                    System.out.println(threadID + " disconnected.");

                    zapis.close();
                    socket.close();
                }
                tura++;
                round = 0;
            }
            round++;
        } catch (SocketTimeoutException e) {
            try {
                errorClose();
            } catch (IOException e1) {
                System.out.println("thread: " + threadID + " disconnected");
                zapis.println("thread: " + threadID + " disconnected");
                return;
            }
            return;
        } catch (IOException e) {
            //return error
            System.out.println("thread: " + threadID + " disconnected");
            zapis.println("thread: " + threadID + " disconnected");
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

    public void error( DataOutputStream outToClient) throws IOException {
        zapis.println("Server to thread " + threadID + ": ERROR");
        outToClient.writeBytes("ERROR" + '\n');
        userGuard++;
    }

    public void errorClose() throws IOException {


            zapis.println("Server to thread " + threadID + ": TURA" + tura + " " + Server.errorThread);
            Server.errorThread--;

            Server.game.resetField(threadID);
            Server.game.resetScore(Server.score, threadID);

            if (threadID == Server.maxThread)
                Server.maxThread--;
            if(threadID == Server.minThread)
                Server.minThread++;
            threadID = 0;
            zapis.close();
            socket.close();

    }
}



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

//TODO problem z wynikami gdy brakuje gracza?
public class Server {
    public static Game game = new Game();

    //   public static CountDownLatch latch = new CountDownLatch(1);
    public static LinkedList values = new LinkedList();

    public static String[] logins = {"LOGIN1", "LOGIN2", "LOGIN3", "LOGIN4", "LOGIN5"};
    public static Map<String, Integer> score = new LinkedHashMap<>();
    public static Map finalScore = new LinkedHashMap();
    public static int maxThread = 5;
    public static int minThread = 1;
    public static int errorThread = 5;

    public static void main(String[] args) throws Exception {

        values.add("");

        for (int i = 1; i < 6; i++) {
            game.newField(i);
        }
        game.randomField();
        int serverRound = 0;

        boolean start = true;
    /*    if(start == true){
            for (int i = 0; i < logins.length ; i++) {
                score.put(logins[i],0);
                finalScore.put(logins[i],0);
            }
            start = false;
        }

        System.out.println(score.toString());
        for (int i = 1; i < 6 ; i++) {
            game.addScore(score , i);
        }

        game.addScore(score,2);
        game.addScore(score,3);
        game.addScore(score,3);
        game.addScore(score,5);
        game.addScore(score,5);
        game.addScore(score,4);


        System.out.println(score.toString());
        game.finalScore(score,logins,finalScore);
        System.out.println(game.sortedFinalScore(finalScore, logins));

    */
        ServerSocket serverSocket = new ServerSocket(3333);
        System.out.println("Oczekuję na połączenie");

        Socket socket1 = serverSocket.accept();
        Socket socket2 = serverSocket.accept();
        Socket socket3 = serverSocket.accept();
        Socket socket4 = serverSocket.accept();
        Socket socket5 = serverSocket.accept();

        EchoThread thread1 = new EchoThread(socket1, 1);
        EchoThread thread2 = new EchoThread(socket2, 2);
        EchoThread thread3 = new EchoThread(socket3, 3);
        EchoThread thread4 = new EchoThread(socket4, 4);
        EchoThread thread5 = new EchoThread(socket5, 5);
        //   boolean start = true;

        thread1.run();
        if (start == true)
            logins[0] = thread1.login;

        thread2.run();
        if (start == true)
            logins[1] = thread2.login;

        thread3.run();
        if (start == true)
            logins[2] = thread3.login;

        thread4.run();
        if (start == true)
            logins[3] = thread4.login;

        thread5.run();
        if (start == true)
            logins[4] = thread5.login;

        while (true) {

            thread1.run();
            EchoThread.clientvalue.addAll(values);
            thread2.run();
            EchoThread.clientvalue.addAll(values);


            thread3.run();
            EchoThread.clientvalue.addAll(values);

            thread4.run();
            EchoThread.clientvalue.addAll(values);

            thread5.run();
            EchoThread.clientvalue.addAll(values);


            while (EchoThread.clientvalue.size() > 15) {
                EchoThread.clientvalue.removeFirst();
            }

            if (start == true) {
                for (int i = 0; i < logins.length; i++) {
                    score.put(logins[i], 0);
                    finalScore.put(logins[i], 0);
                }
                start = false;
            }

            switch (maxThread) {
                case 5:
                    serverRound = thread5.round;
                    break;
                case 4:
                    serverRound = thread4.round;
                    break;
                case 3:
                    serverRound = thread3.round;
                    break;
                case 2:
                    serverRound = thread2.round;
                    break;
                case 1:
                    serverRound = thread1.round;
                    break;

            }

            /*if (serverRound != 0) {


                if (thread1.isAlive()) {
                    game.addField(1);
                    game.addScore(score, 1);
                }
                if (thread2.isAlive()) {
                    game.addField(2);
                    game.addScore(score, 2);
                }
                if (thread3.isAlive()) {
                    game.addField(3);
                    game.addScore(score, 3);
                }
                if (thread4.isAlive()) {
                    game.addField(4);
                    game.addScore(score, 4);
                }
                if (thread5.isAlive()) {
                    game.addField(5);
                    game.addScore(score, 5);
                }
            */    if (serverRound == 10 || maxThread == 1) {//przepisz ECHOTHREAD LICZBA_RUND
                    String finalScoreStr = game.finalScore(score, logins, finalScore).toString();

                    thread1.end(finalScoreStr);
                    thread2.end(finalScoreStr);
                    thread3.end(finalScoreStr);
                    thread4.end(finalScoreStr);
                    thread5.end(finalScoreStr);


                }
            }


        }
    }

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class Game {
    String[][] gameField = new String[6][6];
    int userID;
    int dices;
    Random r = new Random();

    Game() {
        ResetGame();

    }


    public void randomField() {
        int randX;
        int randY;
        int guard = 0;
        int randField;
        while (guard < 5) {
            randX = (int) (Math.random() * 5) + 1;
            randY = (int) (Math.random() * 5) + 1;
            randField = (int) (Math.random() * 4) + 1;
            if (String.valueOf(gameField[randX][randY]).equals("0 0")) {
                gameField[randX][randY] = 0 + " " + randField;
                guard++;
            }
        }
        guard = 0;
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


    public void resetField(int threadID) {
        dices = 0;
        List listAttack;

        for (int i = 1; i < 6; i++) {
            for (int j = 1; j < 6; j++) {
                listAttack = StringTokenizer(i, j);
                userID = Integer.parseInt(listAttack.get(0).toString());

                if (userID == threadID) {
                    dices = Integer.parseInt(listAttack.get(1).toString());
                    gameField[i][j] = 0 + " " + dices;

                }
                listAttack.clear();
            }
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

    public void addScore(Map score, int threadID) {
        String login = Server.logins[threadID - 1];
        int temp;
        if (score.containsKey(login)) {
            temp = Integer.parseInt(score.get(login).toString());
            temp += myScore(threadID);
            score.replace(login, temp);
        }
    }


    public Map finalScore(Map score, String[] logins, Map finalScore) {

        Stream<Map.Entry<String, Integer>> sorted =
                score.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Object[] temp = sorted.toArray();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Pattern compiledPattern = Pattern.compile(logins[j]);
                Matcher matcher = compiledPattern.matcher(temp[i] + "");

                if (matcher.find()) {
                    String key = logins[j];
                    int miejsce = Integer.parseInt(finalScore.get(key) + "");
                    miejsce += i + 1;//liczba punktów
                    finalScore.replace(key, miejsce);
                }
            }
        }

        return finalScore;
    }

    public String sortedFinalScore(Map finalScore, String[] logins) {
        String result = "";
        String string = "";
        Stream<Map.Entry<String, Integer>> sorted =
                finalScore.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue());

        Object[] temp = sorted.toArray();

        for (Object o : temp) {
            string += String.valueOf(o) + " ";
        }

        result = string.replace('=', ' ');
        return result;
    }


    public int myRanking(Map score, String[] logins, int threadID) {
        int miejsce = 0;
        Stream<Map.Entry<String, Integer>> sorted =
                score.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Object[] temp = sorted.toArray();
        for (int i = 0; i < 5; i++) {
            Pattern compiledPattern = Pattern.compile(logins[threadID - 1]);
            Matcher matcher = compiledPattern.matcher(temp[i] + "");

            if (matcher.find()) {
                miejsce = i + 1;//liczba punktów
            }
        }
        return miejsce;
    }

    public void resetScore(Map score, int threadID) {
        String login = Server.logins[threadID - 1];
        if (score.containsKey(login)) {
            score.replace(login, 0);
        }

    }
}


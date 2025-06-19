import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Collections;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Player
{
    String nick;

    Player(String nick)
    {
        this.nick = nick;
    }
}

class Team 
{
    String name;
    Player player1, player2;
    List<Integer> scores;
    int totalScore;

    Team(Player p1, Player p2, String name)
    {
        this.player1 = p1;
        this.player2 = p2;
        this.name = name;
        this.scores = new ArrayList<>();
        this.totalScore = 0;
    }

    void addScore(int score) 
    {
        scores.add(score);
        totalScore += score;
    }

    int average_score()
    {
        if (scores.isEmpty()) return 0;
        return totalScore / scores.size();
    }

    void displayStats() 
    {
        System.out.println(name + " (" + player1.nick + " i " + player2.nick + "):");
        System.out.println("  Suma punktow: " + totalScore);
        System.out.println("  srednia punktow: " + average_score() + "\n");
    }
}

class Round
{
    List<Bidding> biddings;
    Contract contract;
    int nsScore;
    int ewScore;
    int roundNumber;

    Round(int roundNumber) 
    {
        this.biddings = new ArrayList<>();
        this.roundNumber = roundNumber;
        this.nsScore = 0;
        this.ewScore = 0;
    }
    
    void addBidding(Bidding bid) 
    {
        biddings.add(bid);
    }

}

class Game
{
    Team teamNS, teamEW;
    int scoreNS, scoreEW;
    List<List<Bidding>> biddingSequence;
    List<Round> rounds;
    Table table;
    int currentRound;
    int maxRounds;
    boolean gameFinished;
    boolean nsVulnerable;
    boolean ewVulnerable;
    LocalDateTime startTime;
    LocalDateTime endTime;

    Game(Team ns, Team ew, Table table, int maxRounds)
    {
        this.teamNS = ns;
        this.teamEW = ew;
        this.table = table;
        this.biddingSequence = new ArrayList<>();
        this.rounds = new ArrayList<>();
        this.scoreNS = 0;
        this.scoreEW = 0;
        this.currentRound = 1;
        this.maxRounds = maxRounds;
        this.gameFinished = false;
        this.nsVulnerable = false;
        this.ewVulnerable = false;
        this.startTime = LocalDateTime.now();
        startNewRound();
    }

    void add_bidding(int round, Bidding bid)
    {
        while (biddingSequence.size() < round) 
        {
            biddingSequence.add(new ArrayList<>());
        }
        biddingSequence.get(round - 1).add(bid);
    }

    void displayBidding() 
    {
        System.out.println("\nHistoria licytacji:");
        for (int i = 0; i < biddingSequence.size(); i++) 
        {
            System.out.println("Runda " + (i + 1) + ":");
            System.out.println("Pozycje: N-" + table.north.nick + ", E-" + table.east.nick + ", S-" + table.south.nick + ", W-" + table.west.nick);
            for (Bidding bid : biddingSequence.get(i))
            {
                System.out.println("  " + bid);
            }
            System.out.println();
        }
    }

    boolean nextRound() 
    {
        currentRound++;
        if (currentRound > maxRounds) 
        {
            gameFinished = true;
            endTime = LocalDateTime.now();
            return false;
        }
        startNewRound();
        updateVulnerability();
        return true;
    }

    void updateVulnerability() 
    {
        int roundNumber = rounds.size();
        switch (roundNumber % 4) 
        {
            case 1: nsVulnerable = false; ewVulnerable = false; break;
            case 2: nsVulnerable = false; ewVulnerable = true; break;
            case 3: nsVulnerable = true; ewVulnerable = false; break;
            case 0: nsVulnerable = true; ewVulnerable = true; break;
        }
    }

    void displayScoreHistory() 
    {
        System.out.println("\nHistoria wynikow:");
        System.out.println("Runda\tNS\tEW");

        int previousNS = 0;
        int previousEW = 0;

        for (int i = 0; i < rounds.size(); i++) 
        {
            Round round = rounds.get(i);
            int roundNS = round.nsScore - previousNS;
            int roundEW = round.ewScore - previousEW;
            System.out.println((i+1) + "\t" + roundNS + "\t" + roundEW);

            previousNS = round.nsScore;
            previousEW = round.ewScore;
        }
        System.out.println("Suma\t" + scoreNS + "\t" + scoreEW);
    }

    void displayRoundSummary() 
    {
        System.out.println("\n=== Podsumowanie rundy " + currentRound + " ===");
        System.out.println("\nStatystyki druzyn:");
        teamNS.displayStats();
        teamEW.displayStats();
        displayScoreHistory();
    }

    Round getCurrentRound() 
    {
        if (rounds.isEmpty()) 
        {
            startNewRound();
        }
        return rounds.get(rounds.size()-1);
    }

    void startNewRound() 
    {
        rounds.add(new Round(currentRound));
    }

    String getGameInfo()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Gra " + startTime.format(formatter) + 
               " | Rundy: " + maxRounds +
               " | NS: " + teamNS.name + " (" + teamNS.player1.nick + " + " + teamNS.player2.nick + ")" +
               " | EW: " + teamEW.name + " (" + teamEW.player1.nick + " + " + teamEW.player2.nick + ")" +
               " | Wynik: " + teamNS.name + " " + scoreNS + " - " + scoreEW + " " + teamEW.name + "\n";
    }

    String getFullGameData() 
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("GAME_DATA|")
          .append(startTime.format(formatter)).append("|")
          .append(endTime != null ? endTime.format(formatter) : "").append("|")
          .append(maxRounds).append("|")
          .append(teamNS.name).append("|")
          .append(teamNS.player1.nick).append("|")
          .append(teamNS.player2.nick).append("|")
          .append(teamEW.name).append("|")
          .append(teamEW.player1.nick).append("|")
          .append(teamEW.player2.nick).append("|")
          .append(scoreNS).append("|")
          .append(scoreEW).append("\n");
        
        for (Round round : rounds) 
        {
            sb.append("ROUND|").append(round.roundNumber).append("|")
              .append(round.nsScore).append("|")
              .append(round.ewScore).append("\n");
        }

        return sb.toString();
    }
}

class Scoreboard
{
    int round;

    Scoreboard()
    {
        round = 0;
    }

    void displayScores(Team ns, Team ew, int scoreNS, int scoreEW)
    {
        System.out.println("\n=== Wyniki rundy nr." + round + " ===");
        System.out.println("Druzyna " + ns.name + " (" + ns.player1.nick + " i " + ns.player2.nick + "):");

        if (!ns.scores.isEmpty())
        {
            int lastRoundScore = ns.scores.get(ns.scores.size() - 1);
            System.out.println("  Wynik w tej rundzie: " + lastRoundScore);
        }
        System.out.println("  Suma punktow: " + scoreNS);
        
        System.out.println("Druzyna " + ew.name + " (" + ew.player1.nick + " i " + ew.player2.nick + "):");
        if (!ew.scores.isEmpty()) 
        {
            int lastRoundScore = ew.scores.get(ew.scores.size() - 1);
            System.out.println("  Wynik w tej rundzie: " + lastRoundScore);
        }
        System.out.println("  Suma punktow: " + scoreEW);
    }
}

class Table
{
    Player north, east, south, west;

    void assignPositions(Player n, Player e, Player s, Player w)
    {
        north = n;
        east = e;
        south = s;
        west = w;
    }

    Team getNSTeam()
    {
         return new Team(north, south, "NS");
    }

    Team getEWTeam()
    {
        return new Team(east, west, "EW");
    }
}

class Bidding
{
    Player player;
    String bid;

    Bidding(Player player, String bid)
    {
        this.player = player;
        this.bid = bid;
    }

    
    @Override
    public String toString() //TODO: ogarnąc te public i private w calym pliku
    {
        return player.nick + ": " + bid;
    }
}

class Contract
{
    Team declaringTeam, undeclaringTeam;
    int level;
    String suit;
    int result;
    boolean doubled;
    boolean redoubled;
    
    Contract(Team d, Team u, int level, String suit, int result, boolean doubled, boolean redoubled)
    {
        this.declaringTeam = d;
        this.undeclaringTeam = u;
        this.level = level;
        this.suit = suit;
        this.result = result; 
        this.doubled = doubled;
        this.redoubled = redoubled;
    }

    int calculateScore(boolean isDeclarerVulnerable)
    {
        int score = 0;
        int baseContractPoints = calculateContractPoints(level, suit);
        int contractPoints = baseContractPoints;
        int overtricks = Math.max(result, 0);
        int undertricks = Math.max(-result, 0);

        if (redoubled) 
        {
            contractPoints *= 4;
            score += 100;
        } else if (doubled) 
        {
            contractPoints *= 2;
            score += 50;
        }
        
        if ( result >= 0)
        {
            score += contractPoints;

            if (redoubled) 
            {
                score += overtricks * (isDeclarerVulnerable ? 400 : 200);
            } else if (doubled) 
            {
                score += overtricks * (isDeclarerVulnerable ? 200 : 100);
            } else 
            {
                score += overtricks * colorValue(suit);
            }

            if (baseContractPoints >= 100) 
            {
                score += isDeclarerVulnerable ? 500 : 300;

                if (level == 6) {
                    score += isDeclarerVulnerable ? 750 : 500;
                } else if (level == 7) {
                    score += isDeclarerVulnerable ? 1500 : 1000;
                } else {
                    score += 300;
                }
            } else 
            {
                score += 50;
            }
        }
        else
        {
            score -= calculatePenalty(undertricks, doubled, redoubled, isDeclarerVulnerable);
        }

        return score;
    }

    int calculateContractPoints(int level, String suit)
    {
    if (suit.equals("NT"))
        return 40 + (level - 1) * 30;
    else
        return level * colorValue(suit);
    }

    int colorValue(String color)
    {
        switch (color)
        {
            case "C": return 20;
            case "D": return 20;
            case "H": return 30;
            case "S": return 30;
            case "NT": return 30;
            default: return 0;
        }
    }

    int calculatePenalty(int undertricks, boolean doubled, boolean redoubled, boolean vulnerable) 
    {
        int penalty = 0;
        int multiplier = redoubled ? 2 : (doubled ? 1 : 0);

        if (doubled || redoubled) 
        {
            if (vulnerable) 
            {
                penalty = 100 * multiplier;
                penalty += 200 * multiplier;
                penalty += Math.max(undertricks - 2, 0) * 300 * multiplier;
            } else 
            {
                penalty = 100 * multiplier;
                if (undertricks >= 2) 
                {
                    penalty += 100 * multiplier;
                }
                if (undertricks >= 3) 
                {
                    penalty += (undertricks - 2) * 200 * multiplier;
                }
            }
        } else 
        {
            int perTrick = (suit.equals("C") || suit.equals("D")) ? 20 : 30;
            penalty = undertricks * perTrick;
        }

        return penalty;
    }
}

class History
{
    List<Game> gamesHistory;
    Scoreboard scoreboard;
    private static final String HISTORY_FILE = "bridge_history.txt";

    History()
    {
        gamesHistory = new ArrayList<>();
        scoreboard = new Scoreboard();
        loadHistoryFromFile();
    }

    void add_game(Game game)
    {
         gamesHistory.add(game);
         saveHistoryToFile();
    }

    void displayGamesList() 
    {
        System.out.println("\n=== Historia gier ===");
        for (int i = 0; i < gamesHistory.size(); i++) 
        {
            System.out.println((i + 1) + ". " + gamesHistory.get(i).getGameInfo());
        }
    }

    void displayGameDetails(int gameIndex) 
    {
        if (gameIndex < 0 || gameIndex >= gamesHistory.size()) 
        {
            System.out.println("Nieprawidlowy numer gry!");
            return;
        }

        Game selectedGame = gamesHistory.get(gameIndex);
        System.out.println("\n=== Szczegoly gry " + (gameIndex + 1) + " ===");
        System.out.println(selectedGame.getGameInfo());

        selectedGame.teamNS.displayStats();
        selectedGame.teamEW.displayStats();
        selectedGame.displayBidding();
        selectedGame.displayScoreHistory();
    }

    private void saveHistoryToFile() 
    {
        try (FileWriter writer = new FileWriter(HISTORY_FILE)) 
        {
            for (Game game : gamesHistory) 
            {
                writer.write(game.getFullGameData());
            }
        } catch (IOException e) 
        {
            System.out.println("Błąd podczas zapisywania historii do pliku: " + e.getMessage());
        }
    }

    private void loadHistoryFromFile() 
    {
        gamesHistory.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) 
        {
            String line;
            Game currentGame = null;

            while ((line = reader.readLine()) != null) 
            {
                String[] parts = line.split("\\|");

                if (parts[0].equals("GAME_DATA")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime startTime = LocalDateTime.parse(parts[1], formatter);
                    LocalDateTime endTime = parts[2].isEmpty() ? null : LocalDateTime.parse(parts[2], formatter);
                    int maxRounds = Integer.parseInt(parts[3]);

                    Player ns1 = new Player(parts[5]);
                    Player ns2 = new Player(parts[6]);
                    Team ns = new Team(ns1, ns2, parts[4]);

                    Player ew1 = new Player(parts[8]);
                    Player ew2 = new Player(parts[9]);
                    Team ew = new Team(ew1, ew2, parts[7]);

                    Table table = new Table();
                    table.assignPositions(ns1, ew1, ns2, ew2);

                    currentGame = new Game(ns, ew, table, maxRounds);
                    currentGame.startTime = startTime;
                    currentGame.endTime = endTime;
                    currentGame.scoreNS = Integer.parseInt(parts[10]);
                    currentGame.scoreEW = Integer.parseInt(parts[11]);

                    gamesHistory.add(currentGame);
                } else if (parts[0].equals("ROUND") && currentGame != null) {
                    int roundNumber = Integer.parseInt(parts[1]);
                    int nsScore = Integer.parseInt(parts[2]);
                    int ewScore = Integer.parseInt(parts[3]);

                    Round round = new Round(roundNumber);
                    round.nsScore = nsScore;
                    round.ewScore = ewScore;
                    currentGame.rounds.add(round);
                }
            }
        } catch (IOException e) {
            System.out.println("Brak pliku historii lub błąd podczas wczytywania: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Błąd podczas parsowania historii: " + e.getMessage());
        }
    }
    
}

class GameManager
{
    private List<Player> players;
    History history;
    private Scanner scanner;

    GameManager()
    {
        players = new ArrayList<>();
        history = new History();
        scanner = new Scanner(System.in);
    }

    void start ()
    {
        System.out.println("Witaj w grze w brydza!");

        boolean playingSession = true;
        while (playingSession)
        {
            System.out.println("\n=== Menu glowne ===");
            System.out.println("1. Rozpocznij nowa gre");
            System.out.println("2. Przegladaj historie gier");
            System.out.println("3. Jak grac?");
            System.out.println("4. Zakoncz");
            System.out.print("Wybierz opcje (1-4): ");
            
            String choice = scanner.nextLine();

            switch (choice) {
            case "1":
                setupPlayers();
                playNewGame();
                break;
            case "2":
                browseHistory();
                break;
            case "3":
                info();
                break;
            case "4":
                playingSession = false;
                break;
            default:
                System.out.println("Nieprawidlowy wybor!");
            }
        }

        System.out.println("Dziekujemy za gre!");
        scanner.close();
    }

    private void playNewGame()
    {
        Game game = setupGame();
        boolean playingGame = true;
        while (playingGame && !game.gameFinished)
        {
            System.out.println("\nRozpoczyna sie runda " + game.currentRound + " z " + game.maxRounds);
            playBidding(game);

            Round currentRound = game.getCurrentRound();
            currentRound.nsScore = game.scoreNS;
            currentRound.ewScore = game.scoreEW;

            history.scoreboard.round = game.currentRound;
            if (game.nextRound()) 
            {
                System.out.println("\nCzy chcesz kontynuowac do nastepnej rundy? (t/n)");
                String choice = scanner.nextLine();
                playingGame = choice.equalsIgnoreCase("t");

                if (!playingGame) {
                    game.rounds.remove(game.rounds.size() - 1);
                    game.currentRound--;
                }
            } else 
            {
                displayGameSummary(game); 
                System.out.println("\nNacisnij Enter, aby wrocic do menu glownego...");
                scanner.nextLine();
            }
        }
        
        history.add_game(game);
    }
    
    private void setupPlayers()
    {
        System.out.println("\nWprowadz imiona 4 graczy:");
        for (int i = 1; i <= 4; i++) 
        {
            System.out.print("Gracz " + i + ": ");
            String name = scanner.nextLine();
            players.add(new Player(name));
        }
    }

    Game setupGame() 
    {
        String ewName, nsName;

        System.out.println("\nKonfiguracja nowej gry:");

        int rounds = 8;
        System.out.print("Podaj liczbe rund do rozegrania (domyslnie 8): ");
        try 
        {
            rounds = Integer.parseInt(scanner.nextLine());
            if (rounds < 1) rounds = 8;
        } catch (NumberFormatException e) 
        {
            System.out.println("Nieprawidlowa liczba, ustawiam domyslna wartosc 8.");
            rounds = 8;
        }
        
        System.out.println("\nWybierz sposob tworzenia druzyn:");
        System.out.println("1. Losowe przydzielenie graczy do druzyn");
        System.out.println("2. Reczne przydzielenie graczy do druzyn");
        System.out.print("Twoj wybor (1-2): ");

        Team ns, ew;
        Table table = new Table();

        String choice = scanner.nextLine();
        if (choice.equals("1"))
        {
            Collections.shuffle(players);

            System.out.println("\nWylosowano nastepujacy przydzial graczy:");
            System.out.println("Druzyna NS: " + players.get(0).nick + " i " + players.get(1).nick);
            System.out.println("Druzyna EW: " + players.get(2).nick + " i " + players.get(3).nick);

            System.out.print("Podaj nazwe druzyny NS (domyslnie 'NS'): "); 
            nsName = scanner.nextLine();
            if (nsName.isEmpty()) nsName = "NS";
            
            System.out.print("Podaj nazwe druzyny EW (domyslnie 'EW'): ");
            ewName = scanner.nextLine();
            if (ewName.isEmpty()) ewName = "EW";

            ns = new Team(players.get(0), players.get(1), nsName);
            ew = new Team(players.get(2), players.get(3), ewName);
            
            table.assignPositions(
                ns.player1, // North
                ew.player1, // East
                ns.player2, // South
                ew.player2  // West
            );
        } else
        {
            System.out.println("\nWybierz graczy dla druzyny NS:");
            System.out.println("Dostepni gracze:");
            for (int i = 0; i < players.size(); i++) {
                System.out.println((i + 1) + ". " + players.get(i).nick);
            }
            
            int ns1 = -1, ns2 = -1;
        
            while (ns1 < 0 || ns1 >= players.size()) 
            {
                System.out.print("Wybierz pierwszego gracza dla druzyny NS (1-4): ");
                try {
                    ns1 = Integer.parseInt(scanner.nextLine()) - 1;
                    if (ns1 < 0 || ns1 >= players.size())
                        System.out.println("Nieprawidlowy numer gracza. Wybierz liczbe od 1 do 4.");
                } catch (NumberFormatException e) {
                    System.out.println("To nie jest liczba. Wybierz numer gracza od 1 do 4.");
                }
            }

            while (ns2 < 0 || ns2 >= players.size() || ns2 == ns1) 
            {
                System.out.print("Wybierz drugiego gracza dla druzyny NS (1-4, rozny od pierwszego): ");
                try {
                    ns2 = Integer.parseInt(scanner.nextLine()) - 1;
                    if (ns2 < 0 || ns2 >= players.size()) {
                        System.out.println("Nieprawidlowy numer gracza. Wybierz liczbe od 1 do 4.");
                    } else if (ns2 == ns1) {
                        System.out.println("Nie mozesz wybrac tego samego gracza dwa razy!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("To nie jest liczba. Wybierz numer gracza od 1 do 4.");
                }
            }
            
            List<Player> remainingPlayers = new ArrayList<>(players);
            remainingPlayers.remove(players.get(ns1));
            remainingPlayers.remove(players.get(ns2));

            System.out.println("\nPrzydzial graczy:");
            System.out.println("Druzyna NS: " + players.get(ns1).nick + " i " + players.get(ns2).nick);
            System.out.println("Druzyna EW: " + remainingPlayers.get(0).nick + " i " + remainingPlayers.get(1).nick);

            System.out.print("Podaj nazwe druzyny NS (domyslnie 'NS'): ");
            nsName = scanner.nextLine();
            if (nsName.isEmpty()) nsName = "NS";
            
            System.out.print("Podaj nazwe druzyny EW (domyslnie 'EW'): ");
            ewName = scanner.nextLine();
            if (ewName.isEmpty()) ewName = "EW";

            ns = new Team(players.get(ns1), players.get(ns2), nsName);
            ew = new Team(remainingPlayers.get(0), remainingPlayers.get(1), ewName);
            
            table.assignPositions(
                ns.player1, // North
                ew.player1, // East
                ns.player2, // South
                ew.player2  // West
            );
        }
        
        System.out.println("\nUtworzono nastepujace druzyny:");
        System.out.println( nsName + ": " + ns.player1.nick + " i " + ns.player2.nick);
        System.out.println( ewName + ": " + ew.player1.nick + " i " + ew.player2.nick);
        
        return new Game(ns, ew, table, rounds);
    }

    void playBidding(Game game)
    {
        System.out.println("\nRozpoczyna sie licytacja!");
        Table table = game.table;
        Player[] biddingOrder = {table.north, table.east, table.south, table.west};
        int startingPlayerIndex = (game.currentRound - 1) % 4;
        int currentPlayer = startingPlayerIndex;

        int passCount = 0;
        String lastBid = "";
        boolean lastWasDouble = false;
        boolean lastWasRedouble = false;

        while (passCount < 4)
        {
            if (passCount == 3 && lastBid != "") break;

            Player current = biddingOrder[currentPlayer];
            System.out.print(current.nick + ", wpisz swoja oferte: ");
            String bid = scanner.nextLine().toUpperCase();
            if (bid.equals("PASS")) 
            {
                passCount++;
                game.add_bidding(game.currentRound, new Bidding(current, "PASS"));
            } else if (bid.equals("DBL") && !lastBid.isEmpty() && !lastWasDouble && !lastWasRedouble) 
            {
                passCount = 0;
                lastWasDouble = true;
                lastWasRedouble = false;
                game.add_bidding(game.currentRound, new Bidding(current, "DBL"));
            } else if (bid.equals("RDBL") && !lastBid.isEmpty() && lastWasDouble && !lastWasRedouble) 
            {
                passCount = 0;
                lastWasDouble = false;
                lastWasRedouble = true;
                game.add_bidding(game.currentRound, new Bidding(current, "RDBL"));
            } else if (isValidBid(bid) && (lastBid.isEmpty() || isHigherBid(bid, lastBid)))
            {
                passCount = 0;
                lastBid = bid;
                lastWasDouble = false;
                lastWasRedouble = false;
                game.add_bidding(game.currentRound, new Bidding(current, bid));
            } else 
            {
                System.out.println("Nieprawidlowa oferta. Sprobuj ponownie.");
                continue;
            }
            currentPlayer = (currentPlayer + 1) % 4;
        }

        if (!lastBid.isEmpty())
        {
            System.out.println("\nLicytacja zakonczona. Kontrakt: " + lastBid + 
                             (lastWasDouble ? " podwojony" : "") + 
                             (lastWasRedouble ? " ponownie podwojony" : ""));
            createContract(game, lastBid, lastWasDouble, lastWasRedouble);
        } else
        {
            System.out.println("\nLicytacja zakonczona bez kontraktu.");
            Round currentRound = game.getCurrentRound();
            currentRound.nsScore = game.scoreNS;
            currentRound.ewScore = game.scoreEW;
        }

        game.displayRoundSummary();
    }

    private void createContract(Game game, String lastBid, boolean doubled, boolean redoubled)
    {
        Team declaringTeam;
        Team undeclaringTeam;
        Table table = game.table;
        
        Player lastBidder = game.biddingSequence.get(game.currentRound-1).stream()
            .filter(b -> b.bid.equals(lastBid))
            .findFirst()
            .get()
            .player;
        
        if (table.north.equals(lastBidder) || table.south.equals(lastBidder)) 
        {
            declaringTeam = game.teamNS;
            undeclaringTeam = game.teamEW;
        } else 
        {
            declaringTeam = game.teamEW;
            undeclaringTeam = game.teamNS;
        }
        
        int level = Character.getNumericValue(lastBid.charAt(0));
        String suit = lastBid.substring(1);

        int maxPossibleOvertricks = 13 - level - 6;
        int maxPossibleUndertricks = level + 6;
        
        while (true) 
        {
            System.out.print("Podaj wynik kontraktu (+n dla nadrobek, -n dla niedobrok, = dla dokladnego): ");
            String resultStr = scanner.nextLine();

            try 
            {
                int result = parseResult(resultStr);
                if (result > maxPossibleOvertricks)
                {
                    System.out.println("Nie mozna uzyskac wiecej niz " + maxPossibleOvertricks + " nadrobek!");
                    continue;
                }
                if (-result > maxPossibleUndertricks) {
                    System.out.println("Nie mozna stracic wiecej niz " + maxPossibleUndertricks + " lew!");
                    continue;
                }

                boolean isDeclarerVulnerable = (declaringTeam == game.teamNS) ? game.nsVulnerable : game.ewVulnerable;
                Contract contract = new Contract(declaringTeam, undeclaringTeam, level, suit, result, doubled, redoubled);
                
                int score = contract.calculateScore(isDeclarerVulnerable);
                if (declaringTeam == game.teamNS) {
                    game.scoreNS += score;
                    game.teamNS.addScore(score);
                } else {
                    game.scoreEW += score;
                    game.teamEW.addScore(score);
                }

                Round currentRound = game.getCurrentRound();
                currentRound.contract = contract;
                currentRound.nsScore = game.scoreNS;
                currentRound.ewScore = game.scoreEW;
                break;
            } catch (IllegalArgumentException e)
            {
                System.out.println("Nieprawidlowy format wyniku. Wprowadz +n, -n lub =");
            }
        }
        
    }

    boolean isValidBid(String bid)
    {
        if (bid.length() == 2) {
            char level = bid.charAt(0);
            char suit = bid.charAt(1);
            return level >= '1' && level <= '7' && 
                   (suit == 'C' || suit == 'D' || suit == 'H' || suit == 'S' || suit == 'N');
        }
        return false;
    }

    boolean isHigherBid(String newBid, String lastBid)
    {
        int newLevel = Character.getNumericValue(newBid.charAt(0));
        char newSuit = newBid.charAt(1);
        int lastLevel = Character.getNumericValue(lastBid.charAt(0));
        char lastSuit = lastBid.charAt(1);

        int newSuitValue = suitValue(newSuit);
        int lastSuitValue = suitValue(lastSuit);

        return (newLevel > lastLevel) || (newLevel == lastLevel && newSuitValue > lastSuitValue);
    }

     private int suitValue(char suit)
     {
        switch (suit) 
        {
            case 'C': return 1;
            case 'D': return 2;
            case 'H': return 3;
            case 'S': return 4;
            case 'N': return 5;
            default: return 0;
        }
    }

    int parseResult(String resultStr) 
    {
        if (resultStr.startsWith("+")) 
        {
            return Integer.parseInt(resultStr.substring(1));
        } else if (resultStr.startsWith("-")) 
        {
            return -Integer.parseInt(resultStr.substring(1));
        } else if (resultStr.equals("=")) 
        {
            return 0;
        }

        throw new IllegalArgumentException("Nieprawidlowy format wyniku" + resultStr);
    }

    private void browseHistory() {
        if (history.gamesHistory.isEmpty()) 
        {
            System.out.println("Brak zapisanych gier w historii!");
            return;
        }

        history.displayGamesList();

        System.out.print("\nWybierz numer gry do wyswietlenia (0 aby anulowac): ");
        try 
        {
            int choice = Integer.parseInt(scanner.nextLine()) - 1;
            if (choice == -1) return;

            history.displayGameDetails(choice);

            System.out.println("\nNacisnij Enter, aby kontynuowac...");
            scanner.nextLine();
        } catch (NumberFormatException e) 
        {
            System.out.println("Nieprawidlowy numer gry!");
        }
    }

    private void displayGameSummary(Game game) 
    {
        System.out.println("\n=== PODSUMOWANIE GRY ===");
        System.out.println("------------------------");
        System.out.println(game.teamNS.name + ": " + game.scoreNS + " punktow");
        System.out.println(game.teamEW.name + ": " + game.scoreEW + " punktow");

        if (game.scoreNS > game.scoreEW) 
        {
            System.out.println("\nZWYCIEZA DRUZYNA " + game.teamNS.name + "!");
            System.out.println("Gratulacje dla " + game.teamNS.player1.nick + " i " + game.teamNS.player2.nick + "!");
        } 
        else if (game.scoreEW > game.scoreNS) 
        {
            System.out.println("\nZWYCIEZA DRUZYNA " + game.teamEW.name + "!");
            System.out.println("Gratulacje dla " + game.teamEW.player1.nick + " i " + game.teamEW.player2.nick + "!");
        } 
        else 
        {
            System.out.println("\nREMIS! Obie druzyny zdobyly tyle samo punktow.");
        }

        game.displayScoreHistory();
    }

    void info()
    {
        System.out.println("\nKomendy i ich znaczenie:");
        System.out.println("------------------------");
        System.out.println("Licytacja sklada sie z 2-znakowych ofert:");
        System.out.println("- Pierwsza cyfra (1-7) oznacza poziom licytacji");
        System.out.println("- Druga litera oznacza kolor lub bez atu:");
        System.out.println("  C - Trefl (Clubs)");
        System.out.println("  D - Karo (Diamonds)");
        System.out.println("  H - Kier (Hearts)");
        System.out.println("  S - Pik (Spades)");
        System.out.println("  N - Bez atu (No Trump)");
        System.out.println("\nPrzyklady poprawnych ofert: 1C, 2D, 3H, 4S, 5N");
        
        System.out.println("\nSPECJALNE KOMENDY - pojedyncze slowa:");
        System.out.println("PASS  - pas (nie licytuje)");
        System.out.println("DBL   - kontra (podwaja kare/punkty)");
        System.out.println("RDBL  - rekontra (czterokrotnie zwieksza kare/punkty)");
        
        System.out.println("\nUWAGI:");
        System.out.println("- Maksymalny poziom to 7 (13 lew)");
        System.out.println("- DBL/RDBL mozna zglaszac tylko po ofercie przeciwnika");
        System.out.println("- PASS mozna zglosic w kazdej turze");
    }
    
}

public class BridgeBidding
{
    public static void main(String[] args) 
    {
        GameManager gameManager = new GameManager();
        gameManager.start();
    }
}
package networktest1;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Map<String, PrintWriter> players;
    private List<String> waitingRoom;
    private static boolean timerStarted = false;
    private static Timer gameStartTimer;

    private static Map<String, Integer> totalPlayerScores = new HashMap<>();
    private static Set<String> activePlayers = Collections.synchronizedSet(new HashSet<>());
    private static Set<String> scoreTracker = Collections.synchronizedSet(new HashSet<>());
    private static boolean stageMoved = false;

    private static Timer stageTimer = null;
    private static int currentStage = 1;

    public ServerThread(Socket socket, Map<String, PrintWriter> players, List<String> waitingRoom) {
        this.socket = socket;
        this.players = players;
        this.waitingRoom = waitingRoom;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String playerName = in.readLine();

            synchronized (players) {
                players.put(playerName, out);
            }

            synchronized (totalPlayerScores) {
                totalPlayerScores.put(playerName, 0);
            }

            updateClients();
            sendScoresToAll();

            String command;
            while ((command = in.readLine()) != null) {
                if (command.startsWith("PLAY ")) {
                    synchronized (waitingRoom) {
                        if (!waitingRoom.contains(playerName)) {
                            waitingRoom.add(playerName);
                        }
                    }
                    updateClients();

                    if (!timerStarted && waitingRoom.size() >= 1) {
                        timerStarted = true;
                        gameStartTimer = new Timer();
                        gameStartTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startGameIfEnoughPlayers();
                            }
                        }, 30000);
                    }

                    if (waitingRoom.size() == 4) {
                        if (gameStartTimer != null) {
                            gameStartTimer.cancel();
                        }
                        startGameIfEnoughPlayers();
                    }
                } else if (command.startsWith("SCORE ")) {
                    String[] parts = command.substring(6).split(":");
                    if (parts.length == 2) {
                        String name = parts[0].trim();
                        int score = Integer.parseInt(parts[1].trim());

                        synchronized (totalPlayerScores) {
                            totalPlayerScores.put(name, totalPlayerScores.getOrDefault(name, 0) + score);
                        }

                        scoreTracker.add(name);
                        sendScoresToAll();

                        if (!stageMoved && scoreTracker.containsAll(activePlayers)) {
                            moveToNextStage();
                        }
                    }
                } else if (command.startsWith("NEXT_STAGE_REQUEST ")) {
                    if (!stageMoved) {
                        moveToNextStage();
                    }
                } else if (command.startsWith("LEAVE ")) {
                    String playerNameToLeave = command.substring(6).trim();

                    synchronized (waitingRoom) {
                        waitingRoom.remove(playerNameToLeave);
                    }
                    synchronized (totalPlayerScores) {
                        totalPlayerScores.remove(playerNameToLeave);
                    }
                    synchronized (activePlayers) {
                        activePlayers.remove(playerNameToLeave);
                    }

                    updateClients();
                    sendScoresToAll();

                    if (activePlayers.size() == 1) {
                        String winner = activePlayers.iterator().next();
                        int score = totalPlayerScores.getOrDefault(winner, 0);
                        broadcastToActivePlayers("WINNER:" + winner + ":" + score);
                    }
                } else if (command.startsWith("START_STAGE_TIMER ")) {
                    if (stageTimer == null) {
                        startStageTimer();
                    }
                }
            }
        } catch (IOException e) {
            handleDisconnect();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleDisconnect() {
        try {
            String disconnectedPlayer = getPlayerNameForOutput(out);
            if (disconnectedPlayer != null) {
                synchronized (waitingRoom) {
                    waitingRoom.remove(disconnectedPlayer);
                }
                synchronized (totalPlayerScores) {
                    totalPlayerScores.remove(disconnectedPlayer);
                }
                synchronized (activePlayers) {
                    activePlayers.remove(disconnectedPlayer);
                }

                updateClients();
                sendScoresToAll();

                if (activePlayers.size() == 1) {
                    String winner = activePlayers.iterator().next();
                    int score = totalPlayerScores.getOrDefault(winner, 0);
                    broadcastToActivePlayers("WINNER:" + winner + ":" + score);
                }
            }
        } catch (Exception ex) {
            System.out.println("Error handling disconnection: " + ex.getMessage());
        }
    }

    private String getPlayerNameForOutput(PrintWriter playerOut) {
        synchronized (players) {
            for (Map.Entry<String, PrintWriter> entry : players.entrySet()) {
                if (entry.getValue() == playerOut) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void startGameIfEnoughPlayers() {
        synchronized (players) {
            activePlayers.clear();
            scoreTracker.clear();
            stageMoved = false;
            currentStage = 1;

            for (String player : waitingRoom) {
                if (players.containsKey(player)) {
                    PrintWriter out = players.get(player);
                    if (out != null) {
                        out.println("Game started!");
                        activePlayers.add(player);
                    }
                }
            }

            waitingRoom.clear();
            timerStarted = false;
            updateClients();
            sendScoresToAll();
        }
    }

    private void startStageTimer() {
        if (stageTimer != null) {
            stageTimer.cancel();
        }
        stageTimer = new Timer();

        int stageTime = 18000;
        if (currentStage == 2) stageTime = 13000;
        if (currentStage == 3) stageTime = 10000;

        stageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!stageMoved) {
                    moveToNextStage();
                }
            }
        }, stageTime);
    }

    private void moveToNextStage() {
        if (stageTimer != null) {
            stageTimer.cancel();
            stageTimer = null;
        }

        currentStage++;

        if (currentStage <= 3) {
            scoreTracker.clear();
            stageMoved = false;
            broadcastToActivePlayers("NEXT_STAGE");
            sendScoresToAll();
        } else {
            announceWinner();
        }
    }

    private void sendScoresToAll() {
        synchronized (players) {
            StringBuilder scoresBuilder = new StringBuilder();
            synchronized (totalPlayerScores) {
                boolean first = true;
                for (Map.Entry<String, Integer> entry : totalPlayerScores.entrySet()) {
                    if (!first) {
                        scoresBuilder.append(",");
                    }
                    scoresBuilder.append(entry.getKey()).append(":").append(entry.getValue());
                    first = false;
                }
            }
            String scoresString = "SCORES:" + scoresBuilder.toString();
            for (PrintWriter writer : players.values()) {
                writer.println(scoresString);
            }
        }
    }

    private void updateClients() {
        String connectedPlayersMsg = "Connected players: " + String.join(", ", players.keySet());
        String waitingListMsg = "Waiting list: " + String.join(", ", waitingRoom);

        synchronized (players) {
            for (PrintWriter writer : players.values()) {
                writer.println(connectedPlayersMsg);
                writer.println(waitingListMsg);
            }
        }
    }

    private void broadcastToActivePlayers(String message) {
        synchronized (players) {
            for (String player : activePlayers) {
                PrintWriter writer = players.get(player);
                if (writer != null) {
                    writer.println(message);
                }
            }
        }
    }

    private void announceWinner() {
        String winnerName = null;
        int highestScore = -1;

        synchronized (totalPlayerScores) {
            for (Map.Entry<String, Integer> entry : totalPlayerScores.entrySet()) {
                if (entry.getValue() > highestScore) {
                    winnerName = entry.getKey();
                    highestScore = entry.getValue();
                }
            }
        }

        if (winnerName != null) {
            broadcastToActivePlayers("WINNER:" + winnerName + ":" + highestScore);
        } else {
            broadcastToActivePlayers("NO_WINNER");
        }
    }
}
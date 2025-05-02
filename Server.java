/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package networktest1;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT =1111;
    private static Map<String, PrintWriter> players = new HashMap<>();
    private static List<String> waitingRoom = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Game server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");
                new ServerThread(clientSocket, players, waitingRoom).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Huawei
 */
package networktest1;
import java.io.*;
import java.net.*;
public class Client {
  
     private static final String serverIP ="localhost"; // IP
    private static final int PORT =1111;
  public static void main(String[] args) {
        try (Socket socket = new Socket(serverIP, PORT)) {//open Socket 
         ClientThread playerCThread = new ClientThread(socket);//Thread يستقبل من السيرفر
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));//read
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);// send data to server use Socket

   
            new Thread(playerCThread).start();

            
            String userInput;
            while ((userInput = keyboard.readLine()) != null) {
                out.println(userInput);  
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                } else if (userInput.equalsIgnoreCase("play")) {
                    out.println("play");  
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
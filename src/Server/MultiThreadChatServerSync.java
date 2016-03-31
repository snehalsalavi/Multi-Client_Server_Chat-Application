package Server;

/*
 * -----------------------------------------------------------------------
 * Program: Chat Server
 * Author: Snehal Salavi
 * A chat server java program delivers the public and private messages to 
 * multiple clients that are connected in the same chat room. 
 * -----------------------------------------------------------------------
 */

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;


public class MultiThreadChatServerSync {

  // The private server socket.
  private static ServerSocket server_Socket = null;
  // The private client socket.
  private static Socket client_Socket = null;
  //The set of the chat members' name
  private static HashSet<String> chatMembers = new HashSet<String>();
  //The set of the active clients
  private static final LinkedHashSet<clientThread> threads = new LinkedHashSet<clientThread>();

  public static void main(String args[]) {

    // The default port number used in this program is 9399.
    int portNumber = 9399;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
          + "Now using default port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the port Number 9399 (default in this prgram) or any port greater than 1023.
     */
    try {
      server_Socket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each client connection and start a new client thread.
     */
    while (true) 
    {
      try 
      {
        client_Socket = server_Socket.accept();
        clientThread cliThread = new clientThread(client_Socket, threads,chatMembers);
        threads.add(cliThread);
		cliThread.start();	   
      } //End of try
      catch (IOException e) 
      {
        System.out.println(e);
      }
    }//End of While loop
  }
}



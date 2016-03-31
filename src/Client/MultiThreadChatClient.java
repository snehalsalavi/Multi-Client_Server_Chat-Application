package Client;

/*
 * -----------------------------------------------------------------------
 * Program: Chat Client
 * Author: Snehal Salavi
 * A chat client java program connects to the server and communicates to other
 * clients in a chat room thru server. 
 * -----------------------------------------------------------------------
 */

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiThreadChatClient implements Runnable {

  // The client socket
  private static Socket client_Socket = null;
  // The output stream
  private static PrintStream op_stream = null;
  // The input stream
  private static DataInputStream ip_stream = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args)
  {
	// The default port.
    int portNumber = 9399;
    // The default host.
    String hostID = "localhost";

    if (args.length < 2) {
      System.out.println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
              + "Now using host=" + hostID + ", portNumber=" + portNumber);
    } 
    else 
    {
      hostID = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try 
    {
    	System.out.println("Trying "+hostID);
    	client_Socket = new Socket(hostID, portNumber);
    	System.out.println("Connected to "+hostID);
    	inputLine = new BufferedReader(new InputStreamReader(System.in));
    	op_stream = new PrintStream(client_Socket.getOutputStream());
    	ip_stream = new DataInputStream(client_Socket.getInputStream());
    }
    catch(UnknownHostException e) 
    {
      System.err.println("Don't know about the host " + hostID);
    }
    catch (IOException e) 
    {
      System.err.println("Unable to get I/O for the connection to the host "+ hostID);
    }

    /*
     * Start reading and writing the data to the socket connected to the server.
     */
    if (client_Socket != null && op_stream != null && ip_stream != null) {
      try {

        /* Create a thread to read from the server. */
        new Thread(new MultiThreadChatClient()).start();
        while (!closed) {
          op_stream.println(inputLine.readLine().trim());
        }
        /*
         * Close the output stream, close the input stream, close the socket.
         */
        op_stream.close();
        ip_stream.close();
        client_Socket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  /*
   * Create a thread to read from the server. 
   */
  public void run() {
    /*
     * Read from the server socket till we receive "Bye" from the server. 
     * If the server quits then stop the client.
     */
    String responseLine;
    try 
    {
      while((responseLine = ip_stream.readLine()) != null) 
      {
        System.out.println(responseLine);
        if (responseLine.indexOf("*** Bye") != -1)
          break;
      }
      closed = true;
    } 
    catch (IOException e)
    {
      System.err.println("IOException:  " + e);
    }
  }
}

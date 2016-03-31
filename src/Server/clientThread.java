package Server;
 /*
  * ------------------------------------------------------------------------
 * Program: Chat Client Thread
 * Author: Snehal Salavi
 * Functionality: The chat client thread java program opens the input and the output data streams 
 * for a particular client. It ask the client's name and validates if it's already 
 * not in use by other chat member/client. It displays the client available list 
 * of the chat rooms and number of members in each chat room. It notifies all the 
 * other clients connected to a server which are in same chat room about the fact 
 * that a new client has joined the chat room. It broadcasts the data it receives, 
 * to other clients in the same chat room. It can also route the private message 
 * to the particular client in a chat room. Further, when a client leaves the
 * chat room this thread informs all other clients about that and terminates.
 * ------------------------------------------------------------------------
*/

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

class clientThread extends Thread {

	  //The client name
	  private String clientName = null;
	  private DataInputStream ip_stream = null;
	  private PrintStream op_stream = null;
	  //The private client socket
	  private Socket client_Socket = null;
	  //The set of the active clients
	  private final LinkedHashSet<clientThread> threads;
	  //The set of the chat members' name
	  private HashSet<String> chatMembers = new HashSet<String>();
	  //The mapping of the chat room and the number of participants in the chat room
	  private static HashMap<String, HashSet<String>> chatRoomsHm = new HashMap<String, HashSet<String>>();  
	  	 
	  //The constructor
	  public clientThread(Socket client_Socket, LinkedHashSet<clientThread> threads, HashSet<String> chatMembers) {
	    this.client_Socket = client_Socket;
	    this.threads = threads;
	  	this.chatMembers = chatMembers;		
	  }
	  
	  // Run method for thread
	  public void run() {
	     LinkedHashSet<clientThread> threads = this.threads;
	     
	    try {
	      /*
	       * Create input and output streams for this client.
	       */
	      ip_stream = new DataInputStream(client_Socket.getInputStream());
	      op_stream = new PrintStream(client_Socket.getOutputStream());
	      String name;
		  String  chatRoom;
	      while (true) {
	    	  op_stream.println("Welcome to the 'SNEHAL' chat server");
	        op_stream.println("Enter your Login Name.");
	        name = ip_stream.readLine().trim();
			if(chatMembers.contains(name))
			{
					op_stream.println("Sorry, name already taken.");
			}
			else
			{
				if (name.indexOf('@') == -1) 
				{
					chatMembers.add(name);
				  break;
				}
				else 
				{
				  op_stream.println("The name should not contain '@' character.");
				}
			}			
	      }
		  
		  //Display the active chat rooms and Add client to chat room
		  while(true)
		  {
			  op_stream.println("To view active Chat rooms type \"/rooms\" and press Enter");
			String line = ip_stream.readLine().trim();
			
			if (line.startsWith("/rooms"))
			{
				if(chatRoomsHm.isEmpty())
				{ 
					op_stream.println("No active rooms\nJoin new chat room by typing \"/join chatRoom_name\" \n");
					break;
				}
				op_stream.println("Active chat rooms are:\n");
				int numChatRoomMember;
				for(Map.Entry<String, HashSet<String>> chatRoomEntry: chatRoomsHm.entrySet())
				{
					String room = chatRoomEntry.getKey();
					HashSet<String> roomMemberSet = chatRoomEntry.getValue();
					Iterator<String> roomMemberIter = roomMemberSet.iterator();
					numChatRoomMember = 0;
					while(roomMemberIter.hasNext())
					{
						String roomMember = roomMemberIter.next();
						numChatRoomMember++;
					}
					op_stream.println(room+" ("+numChatRoomMember+")");		
				}
				op_stream.println("To Join chat room type \"/join chatRoom_name\" and press Enter\n");
				break;			
			}			
		  }
		  
		  // Join the chat room
		  while (true) {		
	        String line = ip_stream.readLine().trim();
			
			if (line.startsWith("/join"))
			{
				String[] words = line.split("\\s", 2);
				if (words.length > 1 && words[1] != null) 
				{
					words[1] = words[1].trim();
					
					if (!words[1].isEmpty()) 
					{					
						chatRoom = words[1];
						if(!chatRoomsHm.isEmpty())
						{
							HashSet<String> set = chatRoomsHm.get(chatRoom);
							if(set == null)			
								chatRoomsHm.put(words[1],set = new HashSet<String>());
							set.add(name);
							break;
						}
						HashSet<String> set;
						chatRoomsHm.put(words[1],set = new HashSet<String>());
						set.add(name);
						break;
					}
				} 
			}
		  }	  
		 
	      /* Client is entering the chat room */
	      op_stream.println("Entering the room: " +chatRoom+"\n To leave the chat room type /quit");
	      synchronized (this) 
	      {			  
			  Iterator<clientThread> clientIter = threads.iterator();
			  while(clientIter.hasNext())
			  {
				  clientThread cliThread = clientIter.next();
				  if(cliThread != null && cliThread == this)
				  {
					  clientName = "@" + name;					
					  break;
				  }				  
			  }
	       		
			//Notify to other clients that new user has entered in the chat room
			Iterator<clientThread> clientIter2 = threads.iterator();
			  while(clientIter2.hasNext())
			  {
				  clientThread cliThread = clientIter2.next();					
				  HashSet<String> roomMemberSet = chatRoomsHm.get(chatRoom);						
										
				  if(cliThread != null && cliThread != this && roomMemberSet.contains((cliThread.clientName).substring(1)))
				  {
					cliThread.op_stream.println("*** A new user " +name+ " entered the chat room - "+chatRoom+" !!! ***");
				  }				  
			  }	      
	      }
	      
	      /* Start the conversation in a chat room */
	      while (true) {
	        String line = ip_stream.readLine();
	        if (line.startsWith("/quit")) {
	          break;
	        }
	        /* If the message is private, it is sent to the given client only. */
	        if (line.startsWith("@")) 
	        {
	          String[] words = line.split("\\s", 2);
	          if (words.length > 1 && words[1] != null) 
	          {
	            words[1] = words[1].trim();
	            if (!words[1].isEmpty()) 
	            {
	              synchronized (this) 
	              {	               
				    Iterator<clientThread> clientIter3 = threads.iterator();
				    while(clientIter3.hasNext())
				    {
						clientThread cliThread = clientIter3.next();										
						HashSet<String> roomMemberSet = chatRoomsHm.get(chatRoom);
						
					  if(cliThread != null && cliThread != this && cliThread.clientName != null && cliThread.clientName.equals(words[0]) && roomMemberSet.contains((cliThread.clientName).substring(1)) && roomMemberSet.contains((words[0]).substring(1)))
					  {
						cliThread.op_stream.println("<" + name + "> " + words[1]);
	                    /*
	                     * Echo the message to let the client know the private message was sent.
	                     */
	                    this.op_stream.println(">" + name + "> " + words[1]);
	                    break;
					  }// End of If					  
				    }//End of while		
	              }//End of synchronized block
	            }
	          }
	        } 
	        else 
	        {
	          /* The message is public, broadcast it to all other clients. */
	          synchronized (this) 
	          {            
				Iterator<clientThread> clientIter4 = threads.iterator();
				while(clientIter4.hasNext())
				{
					clientThread cliThread = clientIter4.next();							
					HashSet<String> roomMemberSet = chatRoomsHm.get(chatRoom);
						
					if(cliThread != null && cliThread.clientName != null && roomMemberSet.contains((cliThread.clientName).substring(1)))
					{
						cliThread.op_stream.println("<" + name + "> " + line);
					}					  
				  }
	          	}
	        }
	      }//End of while
	      
	      synchronized (this) 
	      {
	    	  Iterator<clientThread> clientIter5 = threads.iterator();
	    	  while(clientIter5.hasNext())
	    	  {
	    		  clientThread cliThread = clientIter5.next();						
	    		  HashSet<String> roomMemberSet = chatRoomsHm.get(chatRoom);
	    		  
	    		  if(cliThread != null && cliThread != this && cliThread.clientName != null && roomMemberSet.contains((cliThread.clientName).substring(1)))
	    		  {
	    			  cliThread.op_stream.println("*** The user " + name + " is leaving the chat room !!! ***");
	    		  }
	    	  }	
	      }
	      op_stream.println("*** Bye " + name + " ***");

	      /*
	       * When client leaves the chat room, clean up by setting the thread to null.
	       *  
	       */
	      synchronized (this) 
	      {
	    	  Iterator<clientThread> clientIter6 = threads.iterator();
	    	  
	    	  while(clientIter6.hasNext())
	    	  {
	    		  clientThread cliThread = clientIter6.next();
	    		  HashSet<String> roomMemberSet = chatRoomsHm.get(chatRoom);
	    		  Iterator<String> strIter = roomMemberSet.iterator();
						
	    		  if(cliThread == this)
	    		  {
	    			  roomMemberSet.remove((cliThread.clientName).substring(1));
	    			  chatRoomsHm.put(chatRoom,roomMemberSet);
	    			  chatMembers.remove(name);
	    			  cliThread = null;					
	    		  }
	    	  }	       
		  }
	      /*
	       * Close the input, output stream and the socket.
	       */
	      ip_stream.close();
	      op_stream.close();
	      client_Socket.close();
	    } 
	    catch (IOException e) 
	    {}
	  }
	}

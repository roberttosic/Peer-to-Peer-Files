import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;

public class server
{
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	
	private static final int maxClientsCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientsCount];

    public static void main(String args[])
    {
        int portNumber = 3335;
        if (args.length < 1) 
        {
            System.out.println("Using default socket settings.\n");
        } 
        else 
        {
            portNumber = Integer.valueOf(args[0]).intValue();
            System.out.println("Using custom socket settings.\n");
        }

        try
        {
            serverSocket = new ServerSocket(portNumber);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        while(true)
        {
            try
            {
                clientSocket = serverSocket.accept();
                int i = 0;
                for(i = 0; i < maxClientsCount; i++)
                {
                    if (threads[i] == null) {
                    (threads[i] = new clientThread(clientSocket, threads)).start();
                    break;
                    }
                }
                if (i == maxClientsCount)
                {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            }
            catch (IOException e) 
            {
                System.out.println(e);
            }
        }
    }
}

class clientThread extends Thread 
{

    public String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    InetAddress address = null;
    private final clientThread[] threads;
    private int maxClientsCount;

    public clientThread(Socket clientSocket, clientThread[] threads)
    {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }
    public void run()
    {
        int maxClientsCount = this.maxClientsCount;
        clientThread[] threads = this.threads;
        try
        {
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            address = clientSocket.getInetAddress();
            String name;

            while(true)
            {
                os.println("Enter the port number your server thread is located on.");
                name = is.readLine().trim();
                if(name.matches("[0-9]+") && name.length() == 4)
                {
                    break;
                }
                else
                {
                    os.println("INVALID port number. Please try again.");
                }
            }
            os.println("\nWelcome to our P2P network.\n\n INSTRUCTIONS:\n /quit: Leaves the server.\n /peers: provides a list of peers to connect to.\n /download [host] [serverport]: downloads the file that this peer has.\n\n");
            synchronized(this)
            {
                for(int i = 0; i < maxClientsCount; i++)
                {
                    if (threads[i] != null && threads[i] == this) {
                    clientName =  name;
                    break;
                }
            }
            
            synchronized (this)
            {
                for(int i = 0; i <maxClientsCount; i++)
                {
                    if (threads[i] != null && threads[i] == this)
                    {
                        threads[i].os.println("A new peer " + clientName + ":" + address + " entered the network.");
                    }
                }
            }
            while (true)
            {
                String line = is.readLine();
                if(line.startsWith("/quit"))
                {
                    break;
                }


                if(line.startsWith("/download"))
                {
                    String[] words = line.split(" ");
                    this.os.println("Starting download from " + words[1] + " " + words[2]);

                }

                if(line.startsWith("/peers"))
                {
                    for(int i = 0; i < maxClientsCount; i++)
                    {
                        if (threads[i] != null && threads[i] != this && threads[i].clientName != null)
                        {
                            this.os.println(threads[i].clientName + ":" + threads[i].address + "\n");
                            break;
                        }
                    }
                    
                }
            }
            synchronized(this)
            {
                for (int i = 0; i < maxClientsCount; i++) 
                {
                    if (threads[i] != null && threads[i] != this && threads[i].clientName != null) 
                    {
                        threads[i].os.println("THE PEER " + clientName + ":" + address + " HAS LEFT THE SERVER.");
                    }
                }
            }
            os.println("GOODBYE " + name + ".");

            synchronized (this) 
            {
                for (int i = 0; i < maxClientsCount; i++) 
                {
                    if (threads[i] == this) 
                    {
                        threads[i] = null;
                    }
                }
            }
            is.close();
            os.close();
            clientSocket.close();
        }
    }
    catch (IOException e) 
        {
        }

  }
}

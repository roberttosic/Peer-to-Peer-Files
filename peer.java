import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;

public class peer implements Runnable
{
    private static Socket clientSocket = null;
    private static PrintStream os = null;
    private static DataInputStream is = null;

    private static BufferedReader inputLine = null;
    private static boolean closed = false;

    private static ServerSocket serverSocket = null;
    private static PrintStream os2 = null;
    private static DataInputStream is2 = null;

    public static void main(String[] args)
    {
        int portNumber = 3335;
        int portNumber2 = 3336;
        String host = "localhost";

        if(args.length < 3)
        {
            System.out.println("Usage: java MultiThreadChatClient <host> <portNumber\n" + "Now using host= " + host + "portNumber= " + portNumber);
        }
        else
        {
            host = args[0];
            portNumber = Integer.valueOf(args[1]).intValue();
            portNumber2 = Integer.valueOf(args[2]).intValue();
        }
        try
        {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
        }
        catch(UnknownHostException e)
        {
        }
        catch(IOException e)
        {
        }

        try
        {
            serverSocket = new ServerSocket(portNumber2);
            Socket connectionSocket = serverSocket.accept();

        }
        catch(UnknownHostException e){
        }
        catch(IOException e){
        }

        if(clientSocket != null && os != null & is !=null)
        {
            try
            {
                new Thread(new peer()).start();
                new Thread(new serverThread(connectionSocket)).start();

                while(!closed)
                {
                    os.println(inputLine.readLine().trim());
                    

                }
                os.close();
                is.close();
                clientSocket.close();
            }
            catch(IOException e)
            {
            }
        }

    }

    public void run()
    {
        String responseLine;
        try
        {
            while((responseLine = is.readLine()) != null)
            {
                System.out.println(responseLine);
                if(responseLine.indexOf("GOODBYE") != -1)
                {
                    break;
                }
                if(responseLine.startsWith("Starting"))
                {
                    String[] words = responseLine.split(" ");
                    System.out.println("Establishing connection with peer");
                    new Thread(new downloadThread(Integer.valueOf(words[4]).intValue(), words[3])).start();
                }
            }
            closed = true;
        }
        catch(IOException e)
        {
            System.err.println("IOException: " + e);
        }
    }
}

class serverThread extends Thread
{
    private String serverName = null;
    private ServerSocket serverSocket = null;
    private int maxCount;
    private File index;
    private Socket connectionSocket;

    public serverThread(Socket connectionSocket)
    {
        this.connectionSocket = connectionSocket;
        maxCount = 1;
    } 
    public void run()
    {
        byte[] b;
        while(true)
        {
            index = new File("index.txt");
            try
            {
                System.out.println("Connected!\n");
                InputStream is = new FileInputStream(index);
                OutputStream os = connectionSocket.getOutputStream();

                b = new byte [1024];
                int length = 0;
                
                while((length = is.read(b)) != -1){
					os.write(b,0,length);
                    System.out.println("serverlolol");
				}

                os.close();
				is.close();

            }
            catch (IOException e) 
            {
            }
        }
    }
}

class downloadThread extends Thread
{
    private Socket clientSocket = null;
    private InputStream in = null;
    private OutputStream out = null;


    public downloadThread(int port, String host)
    {
        try
        {
            this.clientSocket = new Socket(host, port);
        }
        catch(UnknownHostException e)
        {
        }
        catch(IOException e)
        {
        }
    }
    public void run()
    {
        try
        {
            in = clientSocket.getInputStream();
            out = new FileOutputStream("placehere/index.txt");
            System.out.println("Connected with peer!");
            byte[] b = new byte [1024];
            byte[] b2 = new byte [1024];
            int length = 0;

            
            while((length = in.read(b)) != -1){
			    out.write(b,0,length);
                System.out.println("downloadlolol");
		    }
            out.close();
            in.close();

        }
        catch (IOException e) 
        {
        }
        try
        {
            clientSocket.close();
        }
        catch (IOException e) 
        {
        }
        
        
    }
}
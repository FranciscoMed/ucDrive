
import jdk.swing.interop.SwingInterOpUtils;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ucServer extends Thread
{
    static final String rootFolderPath = System.getProperty("user.dir");

    static String usersFolderPath;
    List<String> usersConnected;
    int ServerPort;
    Boolean isPrimary;
    ServerSocket listenSocket;

    public ucServer(int serverPort)
    {
        this.usersConnected = new ArrayList<>();
        this.ServerPort = serverPort;
    }

    public ServerSocket getListenSocket() {
        return listenSocket;
    }

    public static synchronized void main(String args[]) throws Exception
    {
        // o True serve para dizer que é o Primary Server, a false seria o secundario.
        

        String configPathManual = ucServer.rootFolderPath + "\\ServerConfig";
        int  heartbeat, primaryPort = 0, failbeat, secondaryPort = 0;

        try
        {
            File config = new File(configPathManual);
            BufferedReader br = new BufferedReader(new FileReader(config));
            String serverConfig;

            while ((serverConfig = br.readLine()) != null)
            {

                if (serverConfig.contains("heartbeat:"))
                {
                    heartbeat = Integer.parseInt(serverConfig.substring(11));
                }
                else if (serverConfig.contains("failbeat:"))
                {
                    failbeat =  Integer.parseInt(serverConfig.substring(10));
                }
                else if(serverConfig.contains("primaryPort:"))
                {
                    primaryPort =  Integer.parseInt(serverConfig.substring((13)));
                }
                else if(serverConfig.contains("secondaryPort:"))
                {
                    secondaryPort =  Integer.parseInt(serverConfig.substring((15)));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        int firstPort = primaryPort;
        int secondPort = secondaryPort;

        ucServer servidorAtual = new ucServer(7000);

        // Verifica Se o servidor é Primário ou não
        try
        {
            isPrimary(servidorAtual, firstPort);
        }
        catch (Exception e)
        {
            if (e.getMessage().equals("SecondaryServer"))
            {
                System.out.println("Secondary Server, por isso não terei acesso ás ligações TCP!");
            }
        }

        if (servidorAtual.isPrimary)
        {
            System.out.println("[Server Side] - Sou o Servidor Primário!");


            // Thread para UDP
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        udpRunningThread(servidorAtual, firstPort);
                    }
                    finally {
                        System.out.println("Closing TCP server");
                    }
                }
            }).start();

            System.out.println("here 2");
            // Thread para TCP
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        tcpRunningThread(servidorAtual, firstPort);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        System.out.println("Closing TCP server");
                    }
                }
            }).start();

            System.out.println("here 3");


        }
        else
        {
            System.out.println("[Server Side] - Sou o Servidor Secundário!");

            // Thread para UDP
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    udpRunningThread(servidorAtual, secondPort);
                }
            }).start();
        }
    }

    // Retorna o Socket de ligação ao Servidor ou então que é o Segundo Servidor
    private static synchronized boolean isPrimary(ucServer thisServer, int port) throws Exception
    {
        try
        {
            ServerSocket listenSocket = new ServerSocket(port);
            thisServer.usersFolderPath = System.getProperty("user.dir") + "\\Servidor 1\\Users";
            thisServer.isPrimary = true;
            thisServer.listenSocket = listenSocket;
            System.out.println("Server Listen Socket = " + listenSocket);

            return true;
        }
        catch(IOException e)
        {
            if (e.getMessage().contains("Address already in use"))
            {
                thisServer.usersFolderPath = System.getProperty("user.dir") + "\\Servidor 2\\Users";
                thisServer.isPrimary = false;
                throw new Exception("SecondaryServer");
            }
            else
            {
                System.out.println("Listen: " + e.getMessage());
            }
        }
        return false;
    }

    public static synchronized void tcpRunningThread(ucServer thisServer, int port) throws IOException
    {
        //Lê Config
        System.out.println("[TCP CONNECTION] - À Escuta no Porto " + port);

        try
        {
            ServerSocket thisSocket = thisServer.getListenSocket();

            thisServer.isPrimary = true;
            System.out.println("Root Folder Directory = " + rootFolderPath);
            System.out.println("Users Folder Directory = " + usersFolderPath);

            while(true)
            {
                Socket clientSocket = thisSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())= " + clientSocket);
                new Connection(clientSocket, thisServer);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static synchronized void udpRunningThread(ucServer thisServer, int port)
    {
        System.out.println("[UDP CONNECTION] - TENTANDO LIGAR");


        //Lê Config
        try (DatagramSocket udpSocket = new DatagramSocket(port))
        {
            byte[] buf = new byte[udpSocket.getReceiveBufferSize()];
            DatagramPacket pacote = new DatagramPacket(buf, buf.length);
            System.out.println("[UDP CONNECTION] - À Escuta no Porto " + port);

            while (true)
            {
                udpSocket.receive(pacote);
                System.out.println(pacote.getSocketAddress().toString() + ":   " + new String(buf, "UTF-8"));


                // APENAS FAZ ECO - FALTA MUDAR ISTO
                udpSocket.send(pacote);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}





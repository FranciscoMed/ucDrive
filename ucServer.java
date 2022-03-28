import java.net.*;
import java.io.*;
import java.util.*;


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

    public ServerSocket getListenSocket()
    {
        return listenSocket;
    }

    public static synchronized void main(String args[]) throws Exception
    {
        // o True serve para dizer que é o Primary Server, a false seria o secundario.
        String configPathManual = ucServer.rootFolderPath + "\\ServerConfig";
        int  heartbeat = 0, primaryPort = 0, failbeat = 0, secondaryPort = 0;

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
        catch (IOException e)
        {
            e.printStackTrace();
        }
        int firstPort = primaryPort;
        int secondPort = secondaryPort;

        ucServer servidorAtual = new ucServer(7000);

        // Thread para TCP
        Thread tcpThread = new Thread()
        {
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
        };

        // Thread para UDP
        Thread udpThread = new Thread()
        {
            public void run()
            {
                try
                {
                    udpRunningThread(servidorAtual, firstPort);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };


        // Faz a verificação de qual servidor será o primário
        isPrimary(servidorAtual, firstPort);

        if (servidorAtual.isPrimary)
        {
            System.out.println("[Server Side] - Sou o Servidor Primário!");

            System.out.println("[Server Side] - Launch UDP");
            udpThread.start();

            System.out.println("[Server Side] - Launch TCP");
            tcpThread.start();

        }
        else
        {
            System.out.println("[Server Side] - Sou o Servidor Secundário!");
            udpThread.start();

            try
            {
                udpThread.join();

                System.out.println("AFTER JOIN");
                restartServer(servidorAtual.ServerPort, heartbeat, failbeat, configPathManual);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    private static void restartServer(int serverPort, int heartbeat, int failbeat, String configPathManual) throws Exception
    {
        ucServer servidorAtual = new ucServer(7000);

        // Thread para TCP
        Thread tcpThread = new Thread()
        {
            public void run()
            {
                try
                {
                    tcpRunningThread(servidorAtual, serverPort);
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
        };

        // Thread para UDP
        Thread udpThread = new Thread()
        {
            public void run()
            {
                try
                {
                    udpRunningThread(servidorAtual, serverPort);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };


        // Faz a verificação de qual servidor será o primário
        isPrimary(servidorAtual, serverPort);

        if (servidorAtual.isPrimary)
        {
            System.out.println("[Server Side] - Sou o Servidor Primário!");

            System.out.println("[Server Side] - Launch UDP");
            udpThread.start();

            System.out.println("[Server Side] - Launch TCP");
            tcpThread.start();

        }
        else
        {
            System.out.println("[Server Side] - Sou o Servidor Secundário!");
            udpThread.start();

            try
            {
                udpThread.join();

                System.out.println("AFTER JOIN");
                restartServer(servidorAtual.ServerPort, heartbeat, failbeat, configPathManual);
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    // Retorna o Socket de ligação ao Servidor ou então que é o Segundo Servidor
    private static synchronized boolean isPrimary(ucServer thisServer, int port) throws Exception
    {
        try
        {
            ServerSocket listenSocket = new ServerSocket(port);
            System.out.println(listenSocket.getInetAddress());

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
                return false;
            }
            else
            {
                System.out.println("Listen: " + e.getMessage());
            }
        }
        return false;
    }

    // Thread das comunicaçõs UDP entre o servidor principal e os clientes
    public static synchronized void tcpRunningThread(ucServer thisServer, int port) throws IOException
    {

        System.out.println("[TCP CONNECTION] - À Escuta no Porto " + port);

        try
        {
            ServerSocket thisSocket = thisServer.getListenSocket();

            thisServer.isPrimary = true;
            System.out.println("[TCP Server] - Root Folder Directory = " + rootFolderPath);
            System.out.println("[TCP Server] - Users Folder Directory = " + usersFolderPath);

            while(true)
            {
                Socket clientSocket = thisSocket.accept(); // BLOQUEANTE
                System.out.println("[TCP CONNECTION] - CLIENT_SOCKET (created at accept())= " + clientSocket);
                new Connection(clientSocket, thisServer);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Thread das comunicaçõs UDP entre os servidores
    public static void udpRunningThread(ucServer thisServer, int firstPort)
    {
        if (thisServer.isPrimary)
        {
            // SERVIDOR PRIMÁRIO
            try
            {
                // Cria o socket que vai ler os pings.
                DatagramSocket udpSocket = new DatagramSocket(firstPort);
                System.out.println("[UDP CONNECTION] - LISTENING SOCKET CREATED ON PORT: " + firstPort);
                byte[] receive = new byte[65535];
                DatagramPacket DpReceive = null;

                while (true)
                {
                    // Cria o DatagramPacket que receberá os dados
                    DpReceive = new DatagramPacket(receive, receive.length);

                    // Recebe os dados
                    udpSocket.receive(DpReceive);
                    System.out.println("[UDP CONNECTION] - Recebemos: " + data(receive));

                    // Envia o ping de volta!
                    udpSocket.send(DpReceive);
                    // Limpa o buffer
                    receive = new byte[65535];
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            // SERVIDOR SECUNDÁRIO
            try
            {
                // Cria o socket que vão carregar os dados.
                DatagramSocket udpSocket = new DatagramSocket();
                InetAddress ip = InetAddress.getLocalHost();
                byte buffer[] = null;
                int contadorFalhas = 0;

                while (true)
                {
                    // convert the String input into the byte array.
                    buffer = "PING".getBytes();

                    // Step 2 : Create the datagramPacket for sending the data.
                    DatagramPacket DpSend = new DatagramPacket(buffer, buffer.length, ip, firstPort);

                    // Step 3 : invoke the send call to actually send the data.
                    udpSocket.send(DpSend);

                    // Step 4 : Espera receber
                    udpSocket.setSoTimeout(1000); // FALTA MUDAR PARA VARIAVEL DO CONFIG
                    try
                    {
                        byte[] receive = new byte[65535];
                        DatagramPacket DpReceived = new DatagramPacket(receive, receive.length);
                        udpSocket.receive(DpReceived);
                        System.out.println("[UDP CONNECTION] - Recebemos: " + data(receive));
                        receive = new byte[65535];
                        contadorFalhas = 0;
                    }
                    catch (SocketTimeoutException ste)
                    {
                        contadorFalhas = contadorFalhas + 1;
                        System.out.println("[UDP CONNECTION] - Não recebemos resposta. Falha [" + contadorFalhas + "]");
                    }

                    if (contadorFalhas >= 1) // FALTA MUDAR PARA VARIAVEL DO CONFIG
                    {
                        udpSocket.close();
                        System.out.println("[UDP CONNECTION] - Demasiadas falhas seguidas. Irá assumir como servidor principal!");
                        return;
                    }

                    // Faz sleep do tempo suposto entre envios
                    sleep(10000); // FALTA MUDAR PARA VARIAVEL DO CONFIG
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // Passa de Bytes para String - Auxiliar
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}





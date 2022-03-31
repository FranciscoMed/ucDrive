import java.net.*;
import java.io.*;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.*;


public class ucServer extends Thread
{
    static final String rootFolderPath = System.getProperty("user.dir");

    static String usersFolderPath;
    List<String> usersConnected;
    int ServerPort;
    Boolean isPrimary;
    ServerSocket listenSocket;
    int heartbeat;
    int failbeat;
    String neighborAddress;
    String secondaryAddress;
    int numberConnections;

    public ucServer(int serverPort, int heartbeat, int failbeat, String neighborAddress)
    {
        this.usersConnected = new ArrayList<>();
        this.ServerPort = serverPort;
        this.heartbeat = heartbeat;
        this.failbeat = failbeat;
        this.neighborAddress = neighborAddress;
        this.numberConnections = 0;
        this.secondaryAddress = null;
    }

    public ServerSocket getListenSocket()
    {
        return listenSocket;
    }

    public static synchronized void main(String args[])
    {
        String configPathManual = ucServer.rootFolderPath + "\\ServerConfig";
        int  heartbeat = 5, primaryPort = 7000, failbeat = 5;
        String neighborAddress = "";

        // Leitura do ficheiro config dos servers
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
                else if(serverConfig.contains("neighborAddress:"))
                {
                    neighborAddress =  serverConfig.substring((17));
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("[Server side] - Falha na leitura da config do servidor. Usará os valores default [Heartbeat:" + heartbeat + "|Failbeat:" + failbeat + "|Port:" + primaryPort + "]");
            System.out.println("[Server side] - Error: " + e.getMessage());
        }
        int firstPort = primaryPort;

        innitServer(firstPort, heartbeat, failbeat, neighborAddress, configPathManual);
    }

    // Simplesmente relança o inicio do servidor
    private static void innitServer(int serverPort, int heartbeat, int failbeat, String neighborAddress, String configPathManual)
    {
        ucServer servidorAtual = new ucServer(serverPort, heartbeat, failbeat, neighborAddress);

        // Thread para TCP
        Thread tcpThread = new Thread()
        {
            public void run()
            {
                    tcpRunningThread(servidorAtual, serverPort);
            }
        };

        // Thread para UDP
        Thread udpThread = new Thread()
        {
            public void run()
            {
                udpRunningThread(servidorAtual, serverPort);
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
                innitServer(servidorAtual.ServerPort, heartbeat, failbeat, neighborAddress, configPathManual);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // Retorna o Socket de ligação ao Servidor ou então que é o Segundo Servidor
    private static synchronized boolean isPrimary(ucServer thisServer, int port)
    {
        try
        {
            // Vai enviar um pacote UDP para verificar se é primário ou secundário
            DatagramSocket udpSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(thisServer.neighborAddress);

            // convert the String input into the byte array.
            byte buffer[] = "PRIMARY".getBytes();

            // Step 2 : Create the datagramPacket for sending the data.
            DatagramPacket DpSend = new DatagramPacket(buffer, buffer.length, ip, thisServer.ServerPort);

            buffer = null;

            // Step 3 : invoke the send call to actually send the data.
            udpSocket.send(DpSend);

            // Step 4 : Espera receber
            udpSocket.setSoTimeout(5000); // Permite lançar a SocketTimeoutException

            try
            {
                byte[] receive = new byte[65535];
                DatagramPacket DpReceived = new DatagramPacket(receive, receive.length);
                udpSocket.receive(DpReceived);
                System.out.println("[UDP CONNECTION] - Recebemos: " + data(receive));

                System.out.println("TESTES: Inet:" + udpSocket.getInetAddress() + "     Local:" + udpSocket.getLocalAddress());

                thisServer.isPrimary = false;

                udpSocket.close();

                return false;
            }
            catch (SocketTimeoutException ste)
            {
                System.out.println("[UDP CONNECTION] - Não recebemos resposta. Logo será o primário!!");

                ServerSocket listenSocket = new ServerSocket(port);
                System.out.println(listenSocket.getInetAddress());
                thisServer.usersFolderPath = System.getProperty("user.dir") + "\\Servidor 1\\Users";
                thisServer.isPrimary = true;
                thisServer.listenSocket = listenSocket;
                System.out.println("[TCP Server] - Server Listen Socket = " + listenSocket);

                udpSocket.close();
                return true;
            }
        }
        catch (Exception e)
        {
            System.out.println("[Server Side] - Erro ao criar a Socket que verifica qual servidor será!");
            thisServer.isPrimary = false;

            return false;
        }
    }

    // Thread das comunicaçõs UDP entre o servidor principal e os clientes
    public static synchronized void tcpRunningThread(ucServer thisServer, int port)
    {
        try
        {
            System.out.println("[TCP CONNECTION] - À Escuta no Porto " + port);

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
        catch (IOException io)
        {
            io.printStackTrace();
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

                    thisServer.secondaryAddress = String.valueOf(DpReceive.getAddress());
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
                InetAddress ip = InetAddress.getByName(thisServer.neighborAddress);
                byte buffer[] = null;
                int contadorFalhas = 0;

                // Thread para receber ficheiros por UDP do servidor principal
                (new Thread()
                {
                    public void run()
                    {
                        udpReceiveFileClass channelForFiles = new udpReceiveFileClass();
                        channelForFiles.createAndListenSocket();
                    }
                }).start();


                while (true)
                {
                    // convert the String input into the byte array.
                    buffer = "PING".getBytes();

                    // Step 2 : Create the datagramPacket for sending the data.
                    DatagramPacket DpSend = new DatagramPacket(buffer, buffer.length, ip, firstPort);

                    // Step 3 : invoke the send call to actually send the data.
                    udpSocket.send(DpSend);

                    // Step 4 : Espera receber

                    udpSocket.setSoTimeout(1); // Permite lançar a SocketTimeoutException
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

                    if (contadorFalhas > thisServer.failbeat)
                    {
                        udpSocket.close();
                        System.out.println("[UDP CONNECTION] - Demasiadas falhas seguidas. Irá assumir como servidor principal!");
                        return;
                    }

                    // Faz sleep do tempo suposto (Do ServerConfig) entre envios
                    sleep(thisServer.heartbeat * 1000L);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // Auxiliar - Passa de Bytes para String
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
        {
            return null;
        }
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





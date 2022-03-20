
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ucServer {
    static final String rootFolderPath = System.getProperty("user.dir");

    static String usersFolderPath;
    List<String> usersConnected;
    int ServerPort;


    public ucServer(int serverPort, boolean isPrimary)
    {
        this.usersConnected = new ArrayList<>();
        this.ServerPort = serverPort;
        if (isPrimary) // Para saber se é o primary ou não.
        {
            this.usersFolderPath = System.getProperty("user.dir") + "\\Servidor 1\\Users";
        }
        else
        {
            this.usersFolderPath = System.getProperty("user.dir") + "\\Servidor 2\\Users";
        }
    }

    public static void main(String args[])
    {
        int numero = 0;

        // o True serve para dizer que é o Primary Server, a false seria o secundario.
        ucServer servidorPrimario = new ucServer(7000, true);

        String configPathManual = ucServer.rootFolderPath + "\\ServerConfig";
        int  heartbeat , port = 7000, failbeat;

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
                else if(serverConfig.contains("port:"))
                {
                    port =  Integer.parseInt(serverConfig.substring((6)));
                }
               
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Lê Config
        try
        {
            System.out.println("A Escuta no Porto " + port);
            ServerSocket listenSocket = new ServerSocket(port);

            System.out.println("Root Folder Directory = " + rootFolderPath);
            System.out.println("Users Folder Directory = " + usersFolderPath);

            System.out.println("LISTEN SOCKET= " + listenSocket);
            while(true)
            {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())= " + clientSocket);
                numero ++;
                new Connection(clientSocket, numero, servidorPrimario);
            }
        }catch(IOException e) {System.out.println("Listen: " + e.getMessage());} catch (Exception e) {
            e.printStackTrace();
        }
    }
}





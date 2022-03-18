
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ucServer {
    static final String rootFolderPath = System.getProperty("user.dir");
    static final String usersFolderPath = System.getProperty("user.dir") + "\\Users";

    public static void main(String args[])
    {
        int numero=0;


        String configPathManual = ucServer.rootFolderPath + "\\ServerConfig";
        int  heartbeat , port = 7000, failbeat ;

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
        try{

            int serverPort = port;
            System.out.println("A Escuta no Porto " + serverPort);
            ServerSocket listenSocket = new ServerSocket(serverPort);

            System.out.println("Root Folder Directory = " + rootFolderPath);
            System.out.println("Users Folder Directory = " + usersFolderPath);

            System.out.println("LISTEN SOCKET= "+ listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())= " + clientSocket);
                numero ++;
                new Connection(clientSocket, numero);
            }
        }catch(IOException e)
        {System.out.println("Listen: " + e.getMessage());}
    }
}

class Connection extends Thread
{
    DataOutputStream out;
    DataInputStream in;
    ObjectOutputStream outo;
    ObjectInputStream ino;

    Socket clientSocket;
    int thread_number;

    String configPath = ucServer.rootFolderPath + "\\UsersConfig";

    public Connection(Socket aClientSocket, int numero)
    {
        thread_number = numero;

        try
        {
            clientSocket = aClientSocket;

            outo = new ObjectOutputStream(clientSocket.getOutputStream());
            ino = new ObjectInputStream(clientSocket.getInputStream());

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }

    //=============================
    public synchronized void run()
    {

        System.out.println("Root Folder Path > " + ucServer.rootFolderPath);

        // Se quisermos adicionar USERS manualmente usando o Ficheiro de texto
        /*

        String configPathManual = ucServer.rootFolderPath + "\\UsersConfigManual";

        try
        {
            File config = new File(configPathManual);
            BufferedReader br = new BufferedReader(new FileReader(config));
            String usersConfigRead, username = null, userDirectory = null, pass = null;

            /*
            while ((usersConfigRead = br.readLine()) != null)
            {

                if (usersConfigRead.contains("username:"))
                {
                    username = usersConfigRead.substring(10);
                }
                else if (usersConfigRead.contains("password:"))
                {
                    pass = usersConfigRead.substring(10);
                }
                else if(usersConfigRead.contains("directory:"))
                {
                    userDirectory = usersConfigRead.substring(11);

                    // Directory será o ultimo campo de um user por isso, cria-se aqui.
                    User tmp = new User(username,pass, userDirectory);
                    users.add(tmp);
                }
            }

            WriteUsersToFile((List<User>) users, configPath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */

        login();

    }

    // Faz a escrita da lista de Users do Ficheiro de Objetos
    public synchronized void WriteUsersToFile(List<User> listUsers, String filePath)
    {
        System.out.println("Writing all Users to File!");

        try
        {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(listUsers);

            objectOut.close();

            System.out.println("User foi escrito para o ficheiro!");

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    // Faz a leitura da lista de Users do Ficheiro de Objetos
    public synchronized List<User> ReadUsersFromFile(String filePath)
    {
        System.out.println("Reading Users from File Config!");

        List<User> listUsersRead = null;

        try
        {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            listUsersRead = (List<User>) objectIn.readObject();
            objectIn.close();

            for (User u:listUsersRead)
            {
                System.out.println("Read User: " + u.getUsername());
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return listUsersRead;
    }


    public synchronized void login()
    {
        List<User> users = new ArrayList<>();
        System.out.println(ucServer.rootFolderPath);
        String configPath = ucServer.rootFolderPath + "\\UsersConfig";

        users = ReadUsersFromFile(configPath);

        boolean login = true;

        // LOGIN
        try
        {
            // Continua a fazer até conseguir fazer login
            while(login)
            {
                User user = (User) ino.readObject();

                // Leitura dos clientes a fazer login
                System.out.println("Received from client[" + thread_number + "] - Username: " + user.getUsername());

                User foundUser = null;

                // Percorre todos os users à procura do pedido
                for(User u :users)
                {
                    if (u.getUsername().equals(user.getUsername()) && u.getPassword().equals(user.getPassword()))
                    {
                        // True -> Login com sucesso
                        foundUser = u;
                        break;
                    }
                }

                if (foundUser != null)
                {
                    // True -> Login com sucesso
                    outo.writeObject(new RespostaLogin(true, foundUser.getDirectory()));
                    System.out.println("[Server Side] - Enviei confirmação");
                    login = false;


                    menu(user);

                }
                else
                {
                    // False -> Falha no login
                    outo.writeObject(new RespostaLogin(false, null));
                    System.out.println("[Server Side] - Enviei falha");
                }
            }

        }
        catch(EOFException e){System.out.println("EOF:" + e);}
        catch(IOException e){System.out.println("IO:" + e);}
        catch (ClassNotFoundException e) {e.printStackTrace();}
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public synchronized void menu(User user) throws Exception
    {
        //Listar dir
        //Mudar dir
        //Descarregar dir
        System.out.println("[Server Side] - Waiting for commands from " + user.getUsername());

        // LOOP para ler o comando do cliente
        while (true)
        {
            // Apenas lê quando houver algo a ler
            if (in.available() > 0)
            {
                String escolhaCliente = in.readUTF();

                // Verifica que a escolha está dentro dos comandos possíveis
                while (Integer.parseInt(escolhaCliente) < 0 || Integer.parseInt(escolhaCliente) > 8)
                {
                    System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: Valor Errado (" + escolhaCliente +") - Tente novamente!");

                    String texto = "Valor errado - Introduza novo valor dentro dos possiveis!";
                    RespostaServidor respostaServidor = new RespostaServidor("WrongValue", texto);
                    outo.writeObject(respostaServidor);

                    escolhaCliente = in.readUTF();
                }


                switch (escolhaCliente)
                {
                    case "0":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [0] > Alterar PW");

                        changePassword(user);

                        break;
                    case "1":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [1] > Alterar endereços");

                        break;
                    case "2":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [2] > Listar Dir Servidor");

                        break;
                    case "3":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [3] > Mudar Dir Servidor");

                        break;
                    case "4":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [4] > Listar Dir Cliente");

                        break;
                    case "5":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [5] > Mudar Dir Cliente");

                        break;
                    case "6":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [6] > Descarregar ficheiro");

                        break;
                    case "7":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [7] > Carregar ficheiro");

                        break;
                    case "8":
                        System.out.println("Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [8] > Exit");
                        outo.writeObject(new RespostaServidor("EXIT", "O servidor fecha a ligação mediante pedido!"));
                        clientSocket.close();
                        break;
                }


                break;
            }
        }

        System.out.println("FINISHED SERVER");


    }

    private synchronized void changePassword(User userAtual) throws Exception
    {

        try
        {
            System.out.println("Client [" + userAtual.getUsername()+"] quer trocar de Password.");
            RespostaServidor respostaServidor = new RespostaServidor("PW", "Introduza nova PW ?");

            outo.writeObject(respostaServidor);

            String newPw = in.readUTF();
            System.out.println("PW nova do client [" + userAtual.getUsername()+"]: " + newPw);

            userAtual.setPassword(newPw);

            // Vai à lista de Users e altera a PW, escrevendo de novo no ficheiro objeto a nova Password
            List<User> users = ReadUsersFromFile(configPath);
            User foundUser = null;
            // Percorre os users até encontrar
            for(User u : users)
            {
                // Encontrou o USER
                if (u.getUsername().equals(userAtual.getUsername()))
                {
                    u.setPassword(newPw);
                    WriteUsersToFile(users, configPath);
                    break;
                }
            }

            System.out.println("ATUALIZAMOS A PW");

            String texto = "PW do cliente " + userAtual.getUsername() + " modificada. Terá de fazer login de novo!";
            outo.writeObject(new RespostaServidor("PWaccept", texto));

            // ISTO AQUI ESTÁ CORRETO ???? - FALTA
            login();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }
}


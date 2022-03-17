
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


        //Lê Config
        try{
            int serverPort = 7000;
            System.out.println("A Escuta no Porto 7000");
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

        System.out.println(ucServer.rootFolderPath);
        String configPath = ucServer.rootFolderPath + "\\UsersConfig";

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

            WriteUserToFile((List<User>) users, configPath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */

        login();

    }

    // Faz a escrita da lista de Users do Ficheiro de Objetos
    public synchronized void WriteUserToFile(List<User> listUsers, String filePath)
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


    }

    public synchronized void menu(User user) throws IOException
    {
        //Listar dir
        //Mudar dir
        //Descarregar dir
        System.out.println("ENTREI NO MENU");

        // FALTA - Mudar para cliente ou servidor ?
        String menu = "[0] Listar Dir  | [1] Mudar Dir | [2] Descarregar ficheiro | [3] Carregar ficheiro | [4] Mudar Endereços";

        try
        {
            System.out.println("[Server Side] - " + menu);

            out.writeUTF(menu);
        }
        catch(IOException e){System.out.println("IO:" + e);}

    }
}


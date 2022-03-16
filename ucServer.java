
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
    DataInputStream in;
    DataOutputStream out;
    ObjectInputStream ino;
    ObjectOutputStream outo;
    Socket clientSocket;
    int thread_number;

    public Connection (Socket aClientSocket, int numero)
    {
        thread_number = numero;

        try
        {
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            ino = new ObjectInputStream(clientSocket.getInputStream());
            outo = new ObjectOutputStream(clientSocket.getOutputStream());

            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    //=============================
    public void run()
    {

         login();

    }

    public void login()
    {
        List<User> users = new ArrayList<>();
        try
        {

            System.out.println(ucServer.rootFolderPath);

            File config = new File(ucServer.rootFolderPath + "\\UsersConfig");

            BufferedReader br = new BufferedReader(new FileReader(config));
            String usersConfigRead, username = null, userDirectory = null, pass = null;


            while ((usersConfigRead = br.readLine()) != null)
            {
                User temp = null;
                if (usersConfigRead.contains("username:"))
                {
                    username = usersConfigRead.substring(10);
                }
                else if (usersConfigRead.contains("password:"))
                {
                    pass = usersConfigRead.substring(10);
                    temp = new User(username,pass);
                    users.add(temp);
                }/*else if(st.contains("userDirectory:")){
                    home = st.substring(5, st.length() - 1);
                }*/
            }

            // FALTA APAGAR
            System.out.println("Lista de Users lida: " + users);

        } catch (IOException e) {
            e.printStackTrace();
        }


        boolean login = true;
        try{
            while(login)
            {
                //Login

                User user = (User) ino.readObject();

                // FALTA APAGAR

                System.out.println("Cliente[" + thread_number + "] -> Received");
                System.out.println("Cliente[" + thread_number + "] -> " + user.toString());

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
                    out.writeBoolean(true);
                    out.writeUTF(foundUser.getDirectory());
                    out.writeUTF(foundUser.getDirectory());
                    out.writeUTF(foundUser.getDirectory());
                    out.writeUTF(foundUser.getDirectory());
                    System.out.println("Enviei confirmação");
                    menu(user);
                }
                else
                {
                    // False -> Falha no login
                    out.writeBoolean(false);
                    System.out.println("Enviei falha");
                }
            }

        }catch(EOFException e){System.out.println("EOF:" + e);
        }catch(IOException e){System.out.println("IO:" + e);} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void menu(User user)
    {
        //Listar dir
        //Mudar dir
        //Descarregar dir
        System.out.println("Login com sucesso");


    }
}


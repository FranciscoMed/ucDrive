
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ucServer {

    public static void main(String args[]){
        int numero=0;

        //Lê Config


        try{
            int serverPort = 7000;
            System.out.println("A Escuta no Porto 7000");
            ServerSocket listenSocket = new ServerSocket(serverPort);

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

class Connection extends Thread {
    DataInputStream in;
    DataOutputStream out;
    ObjectInputStream ino;
    ObjectOutputStream outo;
    Socket clientSocket;
    int thread_number;

    public Connection (Socket aClientSocket, int numero) {
        thread_number = numero;
        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            ino = new ObjectInputStream(clientSocket.getInputStream());
            outo = new ObjectOutputStream(clientSocket.getOutputStream());

            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    //=============================
    public void run(){

        login();

    }

    public void login(){
        List<User> users = new ArrayList<>();
        try {
            File config = new File("D:\\JamHUB\\UCDrive\\src\\Users");

            BufferedReader br= new BufferedReader(new FileReader(config));
            String st, username = null, home = null, pass = null;


            while ((st = br.readLine()) != null) {
                User temp = null;
                if (st.contains("username:")) {
                    username = st.substring(10);


                } else if (st.contains("password:")) {
                    pass = st.substring(10);
                    temp = new User(username,pass);
                    users.add(temp);
                }/*else if(st.contains("home:")){
                    home = st.substring(5, st.length() - 1);
                }*/
            }

            System.out.println(users);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        boolean login = true;
        try{
            while(login){
                //Login

                User user = (User) ino.readObject();
                System.out.println("Received");
                System.out.println(user.toString());
                if(){
                    out.writeBoolean(false);
                    System.out.println("Enviei confirmação");
                    menu(user);
                }else{
                    out.writeBoolean(true);
                    System.out.println("Enviei falha");

                }
            }

        }catch(EOFException e){System.out.println("EOF:" + e);
        }catch(IOException e){System.out.println("IO:" + e);} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void menu(User user){
        //Listar dir
        //Mudar dir
        //Descarregar dir
        System.out.println("Login com sucesso");


    }
}


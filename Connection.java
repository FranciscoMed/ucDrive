import java.io.*;
import java.net.*;
import java.util.*;

class Connection extends Thread
{
    DataOutputStream out;
    DataInputStream in;
    ObjectOutputStream outo;
    ObjectInputStream ino;

    String userDirectory;

    Socket clientSocket;
    int thread_number;

    ucServer servidorLigado;

    String configPath = ucServer.rootFolderPath + "\\UsersConfig";

    public Connection(Socket aClientSocket, ucServer servidorPrimario)
    {
        this.servidorLigado = servidorPrimario;

        try {
            clientSocket = aClientSocket;

            outo = new ObjectOutputStream(clientSocket.getOutputStream());
            ino = new ObjectInputStream(clientSocket.getInputStream());

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
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


            List<User> users = new ArrayList<>();

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
                    tmp.toString();
                }
            }

            WriteUsersToFile((List<User>) users, configPath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         */

        System.out.println();

        // Faz o login do user e passa para o menu
        login(servidorLigado.usersConnected);
    }

    // Faz a escrita da lista de Users do Ficheiro de Objetos
    public synchronized void WriteUsersToFile(List<User> listUsers, String filePath)
    {
        System.out.println("[TCP Server] - Writing all Users to File!");

        try
        {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(listUsers);

            for (User u : listUsers)
            {
                System.out.println(u.toString());
            }

            objectOut.close();

            System.out.println("[TCP Server] - Users foram escritos para o ficheiro!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Faz a leitura da lista de Users do Ficheiro de Objetos
    public synchronized List<User> ReadUsersFromFile(String filePath)
    {
        System.out.println("[TCP Server] - Reading Users from File Config!");

        List<User> listUsersRead = null;

        try
        {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            listUsersRead = (List<User>) objectIn.readObject();
            objectIn.close();


            for (User u : listUsersRead)
            {
                System.out.println(u.toString());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return listUsersRead;
    }

    // Faz o Login do User e passa para o menu
    public synchronized void login(List<String> usersConnected)
    {
        List<User> users = new ArrayList<>();
        System.out.println(ucServer.rootFolderPath);
        String configPath = ucServer.rootFolderPath + "\\UsersConfig";

        users = ReadUsersFromFile(configPath);
        User user = null;
        boolean login = true;

        // LOGIN
        try
        {
            // Continua a fazer até conseguir fazer login
            while (login)
            {
                outo.writeObject(new RespostaServidor("LoginUser", "Introduza username: "));
                String usernameReceived = in.readUTF();

                outo.writeObject(new RespostaServidor("LoginPW", "Introduza a password: "));
                String passwordReceived = in.readUTF();

                user = new User(usernameReceived, passwordReceived);

                // Leitura dos clientes a fazer login
                System.out.println("Received from client[" + thread_number + "] - Username: " + user.getUsername());

                User foundUser = null;

                // Percorre todos os users à procura do pedido
                for (User u : users)
                {
                    if (u.getUsername().equals(user.getUsername()) && u.getPassword().equals(user.getPassword())) {
                        // True -> Login com sucesso
                        foundUser = u;
                        break;
                    }
                }

                if (foundUser != null) {

                    boolean userAlreadyConnected = false;
                    // Verifica se o User não está ligado já ao servidor!
                    for (String usernameToCompare : usersConnected) {
                        if (usernameToCompare.equals(foundUser.getUsername())) {
                            userAlreadyConnected = true;
                        }
                    }

                    if (userAlreadyConnected) {
                        // False -> Falha no login
                        outo.writeObject(new RespostaLogin(false, null));
                        System.out.println("[TCP Server] - User já logado - Enviei falha");
                    } else {
                        // True -> Login com sucesso
                        usersConnected.add(foundUser.getUsername());
                        outo.writeObject(new RespostaLogin(true, foundUser.getDirectory()));
                        System.out.println("[TCP Server] - Enviei confirmação");
                        login = false;

                        this.userDirectory = foundUser.getDirectory();
                        user = foundUser;

                        menu(user);
                    }
                } else {
                    // False -> Falha no login
                    outo.writeObject(new RespostaLogin(false, null));
                    System.out.println("[TCP Server] - Enviei falha");
                }
            }

        }
        catch (EOFException e)
        {
            if(!(user == null))
            {
                System.out.println("EOF:" + e.getMessage());
                removeLoggedUser(servidorLigado,user);
            }

        }
        catch (IOException e)
        {
            if(!(user == null))
            {
                System.out.println("IO:" + e.getMessage());
                removeLoggedUser(servidorLigado,user);
            }
        }
        catch (Exception e)
        {
            if(!(user == null))
            {
                System.out.println("Exception:" + e.getMessage());
                removeLoggedUser(servidorLigado,user);
            }
        }
    }

    public synchronized void menu(User user) throws Exception
    {

        // LOOP para ler o comando do cliente
        while (true)
        {
            System.out.println("[TCP Server] - Waiting for commands from " + user.getUsername());

            // Apenas lê quando houver algo a ler
            String escolhaCliente = in.readUTF();

            // Verifica que a escolha está dentro dos comandos possíveis
            while (Integer.parseInt(escolhaCliente) < 0 || Integer.parseInt(escolhaCliente) > 8)
            {
                System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: Valor Errado (" + escolhaCliente + ") - Tente novamente!");

                String texto = "Valor errado - Introduza novo valor dentro dos possiveis!";
                RespostaServidor respostaServidor = new RespostaServidor("WrongValue", texto);
                outo.writeObject(respostaServidor);

                escolhaCliente = in.readUTF();
            }


            switch (escolhaCliente)
            {
                case "0":
                    // Alterar PW
                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [0] > Alterar PW");
                    changePassword(user);
                    break;

                case "1":
                    // Alterar endereços
                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [1] > Alterar endereços");
                    break;

                case "2":
                    // Listar Dir Servidor
                    System.out.println(userDirectory);


                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [2] > Listar Dir Servidor");
                    outo.writeObject(new RespostaServidor("ServerDir", "A diretoria atual do user[" + user.getUsername() + "] no servidor é: " + user.getDirectory()));
                    outo.writeObject(new RespostaDiretorias("ServerDir", user.getFullDirectory()));
                    break;

                case "3":
                    // Mudar Dir Servidor
                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [3] > Mudar Dir Servidor");

                    outo.writeObject(new RespostaServidor("ChangeDir", "A diretoria atual do User[" + user.getUsername() + "] no servidor é: " + user.getDirectory() + "!\nIntroduza a nova diretoria, [Back] para voltar atrás ou [Cancel] para cancelar a operação!"));
                    changeUserDirectory(user);

                    break;

                case "6":
                    // Descarregar ficheiro
                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [6] > Descarregar ficheiro");


                    // Escolher o ficheiro
                    outo.writeObject(new RespostaServidor("ChooseFile", "Qual o ficheiro que pretende descarregar ?"));
                    RespostaDiretorias diretoriaAtual = new RespostaDiretorias("ServerDir", user.getFullDirectory());
                    outo.writeObject(diretoriaAtual);

                    downloadFile(user, diretoriaAtual);
                    break;

                case "7":
                    // Carregar ficheiro
                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [7] > Carregar ficheiro");

                    RespostaServidor respostaServidor = new RespostaServidor("upFile", "Carregar para >> " + user.getFullDirectory());
                    outo.writeObject(respostaServidor);
                    System.out.println("[TCP Server] - Enviei upFile");
                    diretoriaAtual = new RespostaDiretorias("ServerDirectory", user.getFullDirectory());

                    uploadFile(user, diretoriaAtual);
                    break;

                case "8":
                    System.out.println("[TCP Server] - Received from client[" + user.getUsername() + " - " + thread_number + "] - Escolha: [8] > Exit");
                    // remove o user da lista de ligações
                    removeLoggedUser(servidorLigado,user);
                    outo.writeObject(new RespostaServidor("EXIT", "A ligação foi fechada!"));
                    clientSocket.close();
                    return;

            }
        }
    }

    private synchronized void downloadFile(User userAtual, RespostaDiretorias diretoriaAtual) throws Exception
    {


        String fileName = in.readUTF();
        String fullFilePath = "";

        // Verifica se é um ficheiro existente e se é possível ser descarregado.
        boolean foundFileOnDirectory = false;
        while (!foundFileOnDirectory)
        {
            if (fileName.equals("Cancel"))
            {
                return;
            }

            for (String fAtual : diretoriaAtual.getDirectoryList())
            {
                if (fAtual.equals(fileName))
                {
                    fullFilePath = userAtual.getFullDirectory() + "\\" + fileName;

                    if (new File(fullFilePath).isFile()) {
                        foundFileOnDirectory = true;
                    }
                }
            }

            if (!foundFileOnDirectory)
            {
                System.out.println("[TCP Server] - File[" + fileName + "] não existe ou não pode ser descarregado.");
                RespostaServidor respostaServidor = new RespostaServidor("RepeatFile", "Ficheiro não pode ser descarregado. Introduza novo nome!");
                outo.writeObject(respostaServidor);

                fileName = in.readUTF();
            }
        }

        System.out.println("Nome do ficheiro a dar upload: " + fileName);
        System.out.println("Ficheiro a dar upload: " + fullFilePath);

        RespostaServidor respostaServidor = new RespostaServidor("YesFile", "Ficheiro escolhido com sucesso. A iniciar download!");
        outo.writeObject(respostaServidor);

        // Cria Socket independente para Download do ficheiro
        int downloadPort = 6500;
        ServerSocket listenDownloadSocket = new ServerSocket(downloadPort);
        out.writeInt(listenDownloadSocket.getLocalPort());
        System.out.println("[TCP Server] - Download Socket = " + listenDownloadSocket);

        Socket downloadSocket = listenDownloadSocket.accept(); // BLOQUEANTE
        System.out.println("Download_Client_SOCKET (created at accept())= " + downloadSocket);

        new DownloadConnection(downloadSocket, fullFilePath);

        System.out.println("[TCP Server] - Ficheiro recebido com sucesso!");
        listenDownloadSocket.close();
        System.out.println("[TCP Server] - Fechando socket Download");

        respostaServidor = new RespostaServidor("DownloadFinish", "Ficheiro enviado com sucesso!");
        outo.writeObject(respostaServidor);


    }

    private synchronized void uploadFile(User userAtual, RespostaDiretorias diretoriaAtual) throws Exception
    {

        System.out.println("[TCP Server] - A espera do nome do ficheiro");
        //Pede Nome Ficheiro
        String fileName = in.readUTF();
        System.out.println("[TCP Server] - Nome recebido: " + fileName);


        ServerSocket listenUploadSocket = new ServerSocket(6500);
        System.out.println("[TCP Server] - Download Socket no Porto " + listenUploadSocket.getLocalPort());
        out.writeInt(listenUploadSocket.getLocalPort());
        System.out.println("Upload SOCKET Listening = " + listenUploadSocket);



        Socket uploadSocket = listenUploadSocket.accept(); // BLOQUEANTE
        System.out.println("[TCP Server] - Upload Client SOCKET (created at accept())= " + uploadSocket);

        new UploadConnection(uploadSocket, fileName, userAtual.getFullDirectory());

        System.out.println("[TCP Server] - Ficheiro recebido com sucesso!");
        System.out.println("[TCP Server] - Fechando socket Upload");
        listenUploadSocket.close();

        RespostaServidor respostaServidor = new RespostaServidor("uploadFinish", "Ficheiro enviado com sucesso!");
        outo.writeObject(respostaServidor);


    }


    private synchronized void changePassword(User userAtual) throws Exception
    {

        System.out.println("[TCP Server] - Client [" + userAtual.getUsername()+"] quer trocar de Password.");
        RespostaServidor respostaServidor = new RespostaServidor("PW", "Introduza nova PW ?");

        outo.writeObject(respostaServidor);

        String newPw = in.readUTF();
        System.out.println("[TCP Server] - PW nova do client [" + userAtual.getUsername()+"]: " + newPw);

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

        String texto = "PW do cliente " + userAtual.getUsername() + " modificada. Terá de fazer login de novo!";
        outo.writeObject(new RespostaServidor("PWaccept", texto));

        // remove o user da lista de ligações
        for (String u2 : servidorLigado.usersConnected)
        {
            if (u2.equals(userAtual.getUsername()))
            {
                System.out.println("[TCP Server] - Removeu User(" + u2 + ") da lista de ligações.");
                servidorLigado.usersConnected.remove((String) u2);
                break;
            }
        }

        login(servidorLigado.usersConnected);
    }

    // Faz o print da lista que recebemos como árvore de diretorias
    private synchronized void changeUserDirectory(User userAtual) throws Exception
    {
        String userDirectory = userAtual.getFullDirectory();
        String shortDirectory = userAtual.getDirectory();

        String[] split = userDirectory.split("\\\\");

        // Verificamos quais os possíveis destinos
        String[] listaDiretoria = new File(userDirectory).list();
        String inputDir = "";

        boolean changedDirectory = false;
        while (!changedDirectory)
        {
            inputDir = in.readUTF();

            // Bloqueia para que o user só aceda ao que tem permissão (tudo dentro da sua própria pasta)
            if (inputDir.equals("Back") && split[split.length - 1].equals(userAtual.getUsername()))
            {
                System.out.println("[TCP Server] - User[" + userAtual.getUsername() + "] não tem permissões para aceder à pasta " + split[split.length - 1]);
                RespostaServidor respostaServidor = new RespostaServidor("RepeatDir", "Não tem acesso a essa pasta. Introduza novo caminho!");
                outo.writeObject(respostaServidor);
            }
            // Termina a mudança de Diretoria no servidor
            else if (inputDir.equals("Cancel"))
            {
                outo.writeObject(new RespostaServidor("YesDir", "Recebemos o Cancel! Poderá sair!"));
                return;
            }
            // Faz o recuo da Diretoria do user
            else if (inputDir.equals("Back") && !split[split.length - 1].equals(userAtual.getUsername()))
            {
                inputDir = split[0];

                for (int i = 1; i < split.length - 1; i++)
                {
                    inputDir = inputDir + "\\" + split[i];
                }

                changedDirectory = true;

                userAtual.setDirectory(inputDir.split("\\\\")[inputDir.split("\\\\").length - 1]);
                userDirectory = inputDir;
            }
            else
            {
                // Percorre a lista de possíveis destinos
                for (String d : listaDiretoria)
                {
                    if (inputDir.equals(d))
                    {
                        // Verifica que a próxima diretoria é uma pasta.
                        if (new File(userDirectory + "\\" + inputDir).isDirectory())
                        {
                            changedDirectory = true;

                            userAtual.setDirectory(shortDirectory + "\\" + inputDir);
                        }
                        break;
                    }
                }

                // Se ainda não mudou pede de novo
                if (!changedDirectory)
                {
                    String textoResposta = "A diretoria " + inputDir + " não existe dentro da diretoria atual " + split[split.length - 1] + "ou não é uma pasta. Utilize uma das possíveis!";
                    System.out.println("[TCP Server] - " + textoResposta);
                    RespostaServidor respostaServidor = new RespostaServidor("WrongDir", textoResposta);
                    outo.writeObject(respostaServidor);
                    outo.writeObject(new RespostaDiretorias("ServerDir", userDirectory));
                }
            }
        }

        String textoResposta = "A diretoria do user[" + userAtual.getUsername() + "] mudou para " + userAtual.getDirectory() + "!";
        System.out.println("[TCP Server] - " + textoResposta);
        outo.writeObject(new RespostaServidor("YesDir", textoResposta));

        // Vai à lista de Users e altera a diretoria, escrevendo de novo no ficheiro objeto a nova diretoria
        List<User> users = ReadUsersFromFile(configPath);
        User foundUser = null;
        // Percorre os users até encontrar
        for(User u : users)
        {
            // Encontrou o USER
            if (u.getUsername().equals(userAtual.getUsername()))
            {

                u.setDirectory(userAtual.getDirectory());
                break;
            }
        }

        WriteUsersToFile(users, configPath);
    }


    // Remove o User da lista de users conectados num certo servidor!
    private synchronized void removeLoggedUser(ucServer servidorLigado, User user)
    {
        // remove o user da lista de ligações
        for (String u2 : servidorLigado.usersConnected)
        {
            if (u2.equals(user.getUsername()))
            {
                System.out.println("[TCP Server] - Removeu User(" + u2 + ") da lista de ligações.");
                servidorLigado.usersConnected.remove((String) u2);
                break;
            }
        }
    }
}
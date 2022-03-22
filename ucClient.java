import java.net.*;
import java.io.*;
import java.util.*;

public class ucClient
{

	String localDirectory = System.getProperty("user.dir") + "\\Home";

	public static void main(String args[])
	{

		Socket s = null;
		int serversocket = 7000;

		try
		{

			s = new Socket("localhost", serversocket);

			ucClient localClient = new ucClient();

			System.out.println("SOCKET = " + s);

			StreamsClass clientStreams = new StreamsClass(s);

			DataInputStream in = clientStreams.getIn();
			ObjectInputStream ino = clientStreams.getIno();
			DataOutputStream out = clientStreams.getOut();
			BufferedReader reader = clientStreams.getReader();

			// Faz o login do cliente
			clientLogin(clientStreams);

			while(true)
			{
				// Imprime menu com Possiveis comandos
				String menu = "[CLIENT SIDE] - O que deseja fazer ? [Digite o numero apenas!]\n[0] Alterar PW  \n[1] Alterar endereços \n[2] Listar Dir Servidor \n[3] Mudar Dir Servidor \n[4] Listar Dir Cliente\n[5] Mudar Dir Cliente \n[6] Descarregar ficheiro \n[7] Carregar ficheiro \n[8] Exit";
				System.out.println(menu);
				System.out.print("-- ");

				// Recebe a escolha do cliente e envia para o server
				String escolha = reader.readLine();

				// Verifica se a escolha será um Inteiro (escolha possivel)
				while (!isInteger(escolha))
				{
					System.out.println("Valor incorrecto.Introduza novamente:\n[0] Alterar PW  \n[1] Alterar endereços \n[2] Listar Dir Servidor \n[3] Mudar Dir Servidor \n[4] Listar Dir Cliente\n[5] Mudar Dir Cliente \n[6] Descarregar ficheiro \n[7] Carregar ficheiro \n[8] Exit");
					System.out.print("-- ");
					escolha = reader.readLine();
				}

				if (escolha.equals("4"))
				{
					System.out.println("[Client Side] - Operação a ser executada no cliente");

					File tmp = new File(localClient.localDirectory);

					System.out.print("[Cliente Side] - ");
					printDirectoryClient(Objects.requireNonNull(tmp.list()), localClient.localDirectory);
				}
				else if (escolha.equals("5"))
				{
					System.out.println("[Client Side] - Operação a ser executada no cliente");
					System.out.print("[Cliente Side] - ");
					changeDirectoryClient(localClient, clientStreams);
				}
				else
				{
					out.writeUTF(escolha);

					RespostaServidor respostaServidor = (RespostaServidor) ino.readObject();

					// Se a mensagem não tiver umas da opções teremos de introduzir novo comando.
					while (respostaServidor.getMensagemCompleta().contains("Valor errado"))
					{
						System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
						System.out.print("-- ");

						// Recebe a escolha do cliente e envia para o server
						escolha = reader.readLine();
						// System.out.println("[Client Side] > " + escolha);
						out.writeUTF(escolha);

						respostaServidor = (RespostaServidor) ino.readObject();
					}

					// Trabalha os comandos - MENU !!
					switch (respostaServidor.getResposta()) {
						default:
							System.out.println("DEFAULT - FALTA TRATAR ISTO - Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							return;


						case "upFile":
							Diretoria diretoria = new Diretoria(localClient.localDirectory);
							printDirectoryClient(diretoria.getDirectoryList(), diretoria.getMainDirectory());
							System.out.println("Escolha o ficheiro a dar upload");

							String upFile = reader.readLine();
							System.out.println("[Client Side] > " + upFile);



							boolean foundFileOnDirectory = false;
							String fullFilePath = "";
							while (!foundFileOnDirectory) {
								if (upFile.equals("Cancel")) {
									break;
								}

								for (String fAtual : diretoria.getDirectoryList()) {
									if (fAtual.equals(upFile)) {
										fullFilePath = diretoria.getMainDirectory() + "\\" + upFile;

										if (new File(fullFilePath).isFile()) {
											foundFileOnDirectory = true;
										}
									}
								}

								if (!foundFileOnDirectory) {
									System.out.println("File[" + upFile + "] não existe ou não pode ser descarregado.");

									System.out.println("Escolha o ficheiro a dar upload ou Cancel para cancelar a operação");
									upFile = reader.readLine();

								}

							}

							out.writeUTF(upFile);


							File file = new File(fullFilePath);
							FileInputStream fis = new FileInputStream(file);
							BufferedInputStream bis = new BufferedInputStream(fis);

							int port = in.readInt();
							s = new Socket("localhost", port);

							System.out.println("SOCKET= " + s );

							out = new DataOutputStream(s.getOutputStream());

							try
							{
								byte[] contents;
								long fileLength = file.length();
								long current = 0;

								while (current != fileLength)
								{
									int size = 10000;
									if (fileLength - current >= size)
									{
										current = current + size;
									}
									else
									{
										size = (int) (fileLength - current);
										current = fileLength;
									}

									contents = new byte[size];

									bis.read(contents, 0, size);
									out.write(contents);

									System.out.println("Sending File[" + file.getName() + "] ... " + (current*100)/fileLength + "% done!" );
								}

								out.flush();
								s.close();

							} catch (IOException e) {
								e.printStackTrace();
							}

							respostaServidor = (RespostaServidor) ino.readObject();

							if (respostaServidor.getResposta().equals("uploadFinish"))
							{
								System.out.println("Ficheiro enviado com sucesso!");

							}else {
								System.out.println("Erro a enviar o ficheiro");
							}

							break;



						// Descarregar o ficheiro
						case "ChooseFile":
							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							RespostaDiretorias teste = (RespostaDiretorias) ino.readObject();
							System.out.print("Recebeu do servidor > ");
							printDirectoryClient(teste.getDirectoryList(), teste.getMainDirectory());

							System.out.print("-- ");
							String newFilename = reader.readLine();
							System.out.println("[Client Side] > " + newFilename);
							out.writeUTF(newFilename);


							respostaServidor = (RespostaServidor) ino.readObject();
							// Faz o tratamento das respostas que não são positivas (Ficheiro que não pode ser descarregado)
							while (respostaServidor.getResposta().equals("RepeatFile"))
							{
								System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
								System.out.print("-- ");
								newFilename = reader.readLine();
								System.out.println("[Client Side] > " + newFilename);
								out.writeUTF(newFilename);
								respostaServidor = (RespostaServidor) ino.readObject();

							}

							if (respostaServidor.getResposta().equals("YesFile"))
							{
								System.out.println("Vai escrever para -> " + localClient.localDirectory + "\\" + newFilename);

								int uploadport = in.readInt();
								s = new Socket("localhost", uploadport);

								System.out.println("SOCKET= " + s );

								byte[] contents = new byte[10000];
								FileOutputStream fos = new FileOutputStream(localClient.localDirectory + "\\" + newFilename);
								BufferedOutputStream bos = new BufferedOutputStream(fos);

								InputStream is = s.getInputStream();
								int bytesRead = 0;

								while ((bytesRead = is.read(contents)) != -1)
								{
									bos.write(contents, 0, bytesRead);
								}

								bos.flush();
								s.close();


								respostaServidor = (RespostaServidor) ino.readObject();
								System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							}
							break;

						//Lista a diretoria do servidor
						case "ServerDir":
							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							RespostaDiretorias dirObject = (RespostaDiretorias) ino.readObject();
							System.out.print("Recebeu do servidor > ");
							printDirectoryClient(dirObject.getDirectoryList(), dirObject.getMainDirectory());

							break;

						case "ChangeDir":
							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							System.out.print("-- ");
							String newServerDirectory = reader.readLine();
							System.out.println("[Client Side] > " + newServerDirectory);
							out.writeUTF(newServerDirectory);

							respostaServidor = (RespostaServidor) ino.readObject();
							// Faz o tratamento das respostas que não são positivas (Diretoria que não existe, ou tentar dar Back para pastas sem acesso)
							while (!respostaServidor.getResposta().equals("YesDir"))
							{
								switch (respostaServidor.getResposta())
								{
									case "RepeatDir":
										System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
										System.out.print("-- ");
										newServerDirectory = reader.readLine();
										System.out.println("[Client Side] > " + newServerDirectory);
										out.writeUTF(newServerDirectory);
										respostaServidor = (RespostaServidor) ino.readObject();

										break;

									case "WrongDir":
										System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());

										dirObject = (RespostaDiretorias) ino.readObject();
										System.out.print("Recebeu do servidor >");
										printDirectoryClient(dirObject.getDirectoryList(), dirObject.getMainDirectory());

										// Leitura do novo
										System.out.print("-- ");
										newServerDirectory = reader.readLine();
										System.out.println("[Client Side] > " + newServerDirectory);
										out.writeUTF(newServerDirectory);
										respostaServidor = (RespostaServidor) ino.readObject();
										break;
								}
							}

							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							break;

						case "PW":
							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							System.out.print("-- ");
							String newPW = reader.readLine();
							System.out.println("[Client Side] > " + newPW);
							out.writeUTF(newPW);

							respostaServidor = (RespostaServidor) ino.readObject();

							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							// FALTA -- CHAMAR AQUI UMA FUNÇÃO LOGIN
							clientLogin(clientStreams);

							break;

						case "LOGOUT":
							System.out.println("LOGOUT");

							break;
						case "EXIT":
							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
							return;
					}
				}
			}

		}
		catch (UnknownHostException e) {
			System.out.println("Socket:" + e.getMessage());
		}
		catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		}
		catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				System.out.println("close:" + e.getMessage());
			}
		}
    }


	// Verifica se o numero em formato String, pode ser transformado num INT (os menus são Inteiros)
	private static boolean isInteger(String s)
	{
		try
		{
			Integer.parseInt(s);
		}
		catch (NumberFormatException | NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	// Faz o print da lista que recebemos como árvore de diretorias
	private static void printDirectoryClient(String[] list, String main)
	{
		System.out.println(" Lista Directoria do Cliente: ");
		String[] split = main.split("\\\\");
		System.out.println("." + split[split.length - 1]);

		for (String atual : list)
		{
			System.out.println("'-- " + atual);
		}

	}

	// Faz o print da lista que recebemos como árvore de diretorias
	private static void changeDirectoryClient(ucClient thisClient, StreamsClass clientStreams) throws Exception
	{
		BufferedReader reader = clientStreams.getReader();

		// Verificamos quais os possíveis destinos
		String[] listaDiretoria = new File(thisClient.localDirectory).list();

		String[] split = thisClient.localDirectory.split("\\\\");
		System.out.println(" Mudar Directoria do Cliente > Atualmente estamos na " + split[split.length - 1]);
		System.out.println("Introduza a nova diretoria ou [Back] para voltar atrás!");

		boolean changedDirectory = false;
		while (!changedDirectory)
		{
			String newDirectory = reader.readLine();

			if (newDirectory.equals("Back") && !split[split.length - 1].equals("ucDrive"))
			{
				split = thisClient.localDirectory.split("\\\\");
				newDirectory = split[0];

				for (int i = 1; i < split.length - 1; i++)
				{
					newDirectory = newDirectory + "\\" + split[i];
				}

				changedDirectory = true;
				thisClient.localDirectory = newDirectory;
			}
			else if (!newDirectory.equals("Back"))
			{
				// Percorre a lista de possíveis destinos
				for (String d : listaDiretoria)
				{
					if (newDirectory.equals(d))
					{
						// Verifica que a próxima diretoria é uma pasta.
						if (new File(thisClient.localDirectory + "\\" + newDirectory).isDirectory())
						{
							thisClient.localDirectory = thisClient.localDirectory + "\\" + newDirectory;
							changedDirectory = true;
						}
						break;
					}
				}
			}

			if (changedDirectory)
			{
				System.out.println("[Client Side] - A diretoria do cliente mudou para: " + thisClient.localDirectory);
			}
			else
			{
				System.out.println("Esse destino " + newDirectory + " não existe ou não é uma pasta! Tente um novo dentro dos possíveis.");
				printDirectoryClient(listaDiretoria, split[split.length - 1]);
			}

		}
	}

	private static void clientLogin(StreamsClass clientStreams) throws Exception
	{
		ObjectInputStream ino = clientStreams.getIno();
		ObjectOutputStream outo = clientStreams.getOuto();
		DataOutputStream out = clientStreams.getOut();
		DataInputStream in = clientStreams.getIn();
		InputStreamReader input = clientStreams.getInput();
		BufferedReader reader = clientStreams.getReader();

		// Repete login até dar um combo certo
		while(true)
		{
			try
			{
				RespostaServidor respostaServidor = (RespostaServidor) ino.readObject();
				System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
				String name = reader.readLine();
				out.writeUTF(name);

				respostaServidor = (RespostaServidor) ino.readObject();
				System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
				String pass = reader.readLine();
				out.writeUTF(pass);


				RespostaLogin respostaLogin = (RespostaLogin) ino.readObject();
				// System.out.println("[CLIENT SIDE] -> " + respostaLogin.toString());

				boolean login = (Boolean) respostaLogin.getResposta();
				if (login)
				{
					String diretoriaAtual = (String) respostaLogin.getDirectoryAtual();
					System.out.println("[CLIENT SIDE] LOGIN COM SUCESSO!");

					break;
				}
				else
				{
					System.out.println("[CLIENT SIDE] LOGIN SEM SUCESSO!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
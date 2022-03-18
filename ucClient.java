import java.net.*;
import java.io.*;
import java.security.spec.RSAOtherPrimeInfo;

public class ucClient
{
	public static void main(String args[])
	{

		Socket s = null;
		int serversocket = 7000;

		try {

			s = new Socket("localhost", serversocket);

			System.out.println("SOCKET= " + s);

			ObjectInputStream ino = new ObjectInputStream(s.getInputStream());
			ObjectOutputStream outo = new ObjectOutputStream(s.getOutputStream());


			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream in = new DataInputStream(s.getInputStream());

			InputStreamReader input = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(input);


			// FALTA PASSAR ISTO PARA UMA FUNÇÃO

			// Repete login até dar um combo certo
			while(true)
			{

				// FALTA -> ESTA CONVERSA TEM DE SER ENTRE SERVIDOR OR ?
				System.out.println("Introduza username: ");
				String name = reader.readLine();

				System.out.println("Introduza password: ");
				String pass = reader.readLine();

				User novo = new User(name, pass);
				outo.writeObject(novo);

				RespostaLogin respostaLogin = (RespostaLogin) ino.readObject();
				System.out.println("[CLIENT SIDE] -> " + respostaLogin.toString());

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
			}

			while(true)
			{

				// Imprime menu com Possiveis comandos
				String menu = "[CLIENT SIDE] - O que deseja fazer ? [Digite o numero apenas!]\n[0] Alterar PW  \n[1] Alterar endereços \n[2] Listar Dir Servidor \n[3] Mudar Dir Servidor \n[4] Listar Dir Cliente\n[5] Mudar Dir Cliente \n[6] Descarregar ficheiro \n[7] Carregar ficheiro \n[8] Exit";
				System.out.println(menu);
				System.out.print("-- ");

				// Recebe a escolha do cliente e envia para o server
				String escolha = reader.readLine();
				// System.out.println("[Client Side] > " + escolha);
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
				switch (respostaServidor.getResposta())
				{
					default:
						System.out.println("DEFAULT - FALTA TRATAR ISTO - Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
						return;

					case "PW":
						System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
						System.out.print("-- ");
						String newPW = reader.readLine();
						System.out.println("[Client Side] > " + newPW);
						out.writeUTF(newPW);

						respostaServidor = (RespostaServidor) ino.readObject();

						if (respostaServidor.getResposta().contains("PWaccept"))
						{
							System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());

							// FALTA -- CHAMAR AQUI UMA FUNÇÃO LOGIN
							// clientLogin()

							System.out.println("FALTA CHAMAR FUNÇÃO LOGIN");

						}
						else
						{
							// FALTA - O QUE SE FAZ AQUI ? É SEQUER PRECISO ???
							new Exception("Don't know this answer from server");
						}


						break;

					case "LOGOUT":
						System.out.println("LOGOUT");

						break;
					case "EXIT":
						System.out.println("Recebeu do servidor > " + respostaServidor.getMensagemCompleta());
						return;
				}

				System.out.println("TESTE -> QUEREMOS NOVO COMANDO!");
			}




		} catch (UnknownHostException e) {
			System.out.println("Socket:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		} catch (ClassNotFoundException e) {
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
}
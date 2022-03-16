import java.net.*;
import java.io.*;

public class ucClient {
    public static void main(String args[]) {

	Socket s = null;
	int serversocket = 7000;

	try {
	    // 1o passo
	    s = new Socket("localhost", serversocket);

	    System.out.println("SOCKET= " + s);

		ObjectInputStream ino = new ObjectInputStream(s.getInputStream());
		ObjectOutputStream outo = new ObjectOutputStream(s.getOutputStream());


		DataOutputStream out = new DataOutputStream(s.getOutputStream());
	    DataInputStream in = new DataInputStream(s.getInputStream());

	    InputStreamReader input = new InputStreamReader(System.in);
	    BufferedReader reader = new BufferedReader(input);

		String name = "", pass = "";
		boolean login = false;

		// Repete login até dar um combo certo
		while(!login)
		{
			System.out.println("Introduza username: ");
			name = reader.readLine();

			System.out.println("Introduza password: ");
			pass = reader.readLine();

			User novo = new User(name, pass);

			outo.writeObject(novo);


			RespostaLogin respostaLogin = (RespostaLogin) ino.readObject();

			System.out.println("[CLIENT SIDE] -> " + respostaLogin.toString());

			login = (Boolean) respostaLogin.getResposta();

			if (login)
			{
				String diretoriaAtual = (String) respostaLogin.getDirectoryAtual();
				System.out.println("[CLIENT SIDE] LOGIN COM SUCESSO!");
			}
			else
			{
				System.out.println("[CLIENT SIDE] LOGIN SEM SUCESSO!");
			}
		}


		System.out.println("Wanna Die");

	} catch (UnknownHostException e) {
	    System.out.println("Sock:" + e.getMessage());
	} catch (EOFException e) {
	    System.out.println("EOF:" + e.getMessage());
	} catch (IOException e) {
	    System.out.println("IO:" + e.getMessage());
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} finally {
	    if (s != null)
		try {
		    s.close();
		} catch (IOException e) {
		    System.out.println("close:" + e.getMessage());
		}
	}
    }
}
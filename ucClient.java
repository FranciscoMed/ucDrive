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


	    DataInputStream in = new DataInputStream(s.getInputStream());
	    DataOutputStream out = new DataOutputStream(s.getOutputStream());

		ObjectOutputStream outo = new ObjectOutputStream(s.getOutputStream());

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
			login = in.readBoolean();

			System.out.println("[CLIENT SIDE] LOGIN COM SUCESSO!");

			String diretoriaAtual = in.readUTF();
			System.out.println(diretoriaAtual);


			// FALTA APAGAR
			System.out.println(in.readUTF());
		}

		System.out.println("Wanna Die");

	} catch (UnknownHostException e) {
	    System.out.println("Sock:" + e.getMessage());
	} catch (EOFException e) {
	    System.out.println("EOF:" + e.getMessage());
	} catch (IOException e) {
	    System.out.println("IO:" + e.getMessage());
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
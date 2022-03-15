import java.net.*;
import java.io.*;

public class ucClient {
    public static void main(String args[]) {
	// args[0] <- hostname of destination
	if (args.length == 0) {
	    System.out.println("java TCPClient hostname");
	    System.exit(0);
	}

	Socket s = null;
	int serversocket = 7000;
	try {
	    // 1o passo
	    s = new Socket(args[0], serversocket);

	    System.out.println("SOCKET= " + s);


	    DataInputStream in = new DataInputStream(s.getInputStream());
	    DataOutputStream out = new DataOutputStream(s.getOutputStream());

		ObjectOutputStream outo = new ObjectOutputStream(s.getOutputStream());

	    InputStreamReader input = new InputStreamReader(System.in);
	    BufferedReader reader = new BufferedReader(input);

		String name = "", pass = "";
		boolean login = true;

		while(login) {
			System.out.println("Introduza username: ");
			name = reader.readLine();

			System.out.println("Introduza password: ");
			pass = reader.readLine();

			User novo = new User(name, pass);

			outo.writeObject(novo);

			System.out.println(in.readUTF());
			login = in.readBoolean();
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
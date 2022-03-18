import java.io.*;
import java.net.Socket;

public class StreamsClass
{

    ObjectInputStream ino;
    ObjectOutputStream outo;
    DataOutputStream out;
    DataInputStream in;
    InputStreamReader input;
    BufferedReader reader;

    public StreamsClass(Socket s) throws Exception
    {
        this.ino = new ObjectInputStream(s.getInputStream());;
        this.outo  = new ObjectOutputStream(s.getOutputStream());
        this.out = new DataOutputStream(s.getOutputStream());
        this.in = new DataInputStream(s.getInputStream());
        this.input = new InputStreamReader(System.in);
        this.reader = new BufferedReader(input);
    }

    public ObjectInputStream getIno() {
        return ino;
    }

    public ObjectOutputStream getOuto() {
        return outo;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public InputStreamReader getInput() {
        return input;
    }

    public BufferedReader getReader() {
        return reader;
    }
}

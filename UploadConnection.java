import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class UploadConnection extends Thread
{
    String path;
    Socket uploadSocket;
    String filename;
    InputStream in;

    public UploadConnection(Socket uploadSocket, String filename, String path) {
        this.uploadSocket = uploadSocket;
        this.filename = filename;
        this.path =path;
        this.start();

    }


    public synchronized void run() {


        try {


            byte[] contents = new byte[10000];
            FileOutputStream fos = new FileOutputStream(path + "\\" + filename);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            in = uploadSocket.getInputStream();
            int bytesRead = 0;

            while ((bytesRead = in.read(contents)) != -1) {
                bos.write(contents, 0, bytesRead);
            }

            bos.flush();
            bos.close();
            fos.close();
            uploadSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

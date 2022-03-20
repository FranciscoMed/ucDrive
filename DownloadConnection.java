import java.io.*;
import java.net.Socket;

class DownloadConnection extends Thread
{
    DataOutputStream out;
    DataInputStream in;
    ObjectOutputStream outo;
    ObjectInputStream ino;
    BufferedOutputStream bos;
    BufferedInputStream bis;

    Socket downloadSocket;

    String filePath;

    public DownloadConnection(Socket inputDownloadSocket, String filePath)
    {
        this.filePath = filePath;
        this.downloadSocket = inputDownloadSocket;
        System.out.println("1 - COMEÇOU O RUN DO DOWNLOAD CONNECTION");

        try
        {

            outo = new ObjectOutputStream(downloadSocket.getOutputStream());
            ino = new ObjectInputStream(downloadSocket.getInputStream());
            out = new DataOutputStream(downloadSocket.getOutputStream());
            in = new DataInputStream(downloadSocket.getInputStream());
            bos = new BufferedOutputStream(downloadSocket.getOutputStream());
            bis = new BufferedInputStream(downloadSocket.getInputStream());


            System.out.println("teste antes");
            this.start();
            System.out.println("teste");

        }
        catch(Exception e){System.out.println("Connection:" + e.getMessage());}

    }

    //=============================
    public synchronized void run()
    {

        System.out.println("COMEÇOU O RUN DO DOWNLOAD CONNECTION");

        try
        {
            File file = new File(filePath);
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



                System.out.println("Sending File[" + filePath + "] ... " + (current*100)/fileLength + "% done!" );
            }

            out.flush();
            downloadSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
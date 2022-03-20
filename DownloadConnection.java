import java.io.*;
import java.net.Socket;

class DownloadConnection extends Thread
{
    DataOutputStream out;

    Socket downloadSocket;
    String filePath;

    public DownloadConnection(Socket inputDownloadSocket, String filePath)
    {
        this.filePath = filePath;
        this.downloadSocket = inputDownloadSocket;

        this.start();
    }

    public synchronized void run()
    {
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            out = new DataOutputStream(downloadSocket.getOutputStream());
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

                    System.out.println("[Server Side] - Sending File[" + file.getName() + "] ... " + (current*100)/fileLength + "% done!" );
                }

                out.flush();
                downloadSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        catch(Exception e){System.out.println("Connection:" + e.getMessage());}
    }
}
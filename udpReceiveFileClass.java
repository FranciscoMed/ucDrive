import java.io.*;
import java.net.*;

public class udpReceiveFileClass extends Thread
{
    private DatagramSocket socket = null;
    private FileEvent fileEvent = null;

    public udpReceiveFileClass()
    {

    }

    public void createAndListenSocket()
    {
        try
        {
            socket = new DatagramSocket(9876);
            byte[] incomingData = new byte[1024 * 1000 * 50];

            while (true)
            {
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);

                byte[] data = incomingPacket.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);
                fileEvent = (FileEvent) is.readObject();
                if (fileEvent.getStatus().equalsIgnoreCase("Error"))
                {
                    System.out.println("Houve erros no envio do ficheiro!");
                    System.exit(0);
                }
                createAndWriteFile(); // writing the file to hard disk
                InetAddress IPAddress = incomingPacket.getAddress();
                int port = incomingPacket.getPort();
                byte[] reply = "Thank you for the file!".getBytes();
                DatagramPacket replyPacket = new DatagramPacket(reply, reply.length, IPAddress, port);
                socket.send(replyPacket);
                Thread.sleep(3000);
            }
        }
        catch (Exception e)
        {
            System.out.println("[Server Secundário | UDP BACKUP] - A recepção de ficheiros para backup teve erros:");
            System.out.println(e.getMessage());
        }
    }

    public void createAndWriteFile()
    {
        String outputFile = System.getProperty("user.dir") + "\\" + fileEvent.getDestinationDirectory();

        if (!new File(fileEvent.getDestinationDirectory()).exists())
        {
            new File(fileEvent.getDestinationDirectory()).mkdirs();
        }
        File dstFile = new File(outputFile);
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(dstFile);
            fileOutputStream.write(fileEvent.getFileData());
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("[UDP BACKUP] - Ficheiro: " + outputFile + " guardado com sucesso!");

        }
        catch (Exception e)
        {
            System.out.println("[Server Secundário | UDP BACKUP] - A leitura do ficheiros para backup teve erros:");
            System.out.println(e.getMessage());
            fileEvent.setStatus("Error");
        }

    }

    public static void main(String[] args)
    {
        udpReceiveFileClass server = new udpReceiveFileClass();
        server.createAndListenSocket();
    }
}
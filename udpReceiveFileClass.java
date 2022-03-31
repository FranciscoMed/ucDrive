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
        System.out.println("Extend a thread.");

        try
        {
            socket = new DatagramSocket(9876);
            byte[] incomingData = new byte[1024 * 1000 * 50];


            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);

            System.out.println("DEPOIS DO RECEIVE INICIAL");


            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            fileEvent = (FileEvent) is.readObject();
            if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
                System.out.println("Some issue happened while packing the data @ client side");
                System.exit(0);
            }
            createAndWriteFile(); // writing the file to hard disk
            InetAddress IPAddress = incomingPacket.getAddress();
            int port = incomingPacket.getPort();
            String reply = "Thank you for the message";
            byte[] replyBytea = reply.getBytes();
            DatagramPacket replyPacket =
                    new DatagramPacket(replyBytea, replyBytea.length, IPAddress, port);
            socket.send(replyPacket);
            Thread.sleep(3000);

            System.out.println("FIM");

        }
        catch (Exception e)
        {
            System.out.println("[Server Secundário] - A recepção de ficheiros para backup teve erros:");
            System.out.println(e.getMessage());
        }
    }

    public void createAndWriteFile()
    {

        String outputFile = System.getProperty("user.dir") + "\\" + fileEvent.getDestinationDirectory();

        System.out.println("PATH ------------------> " + outputFile);

        if (!new File(fileEvent.getDestinationDirectory()).exists()) {
            new File(fileEvent.getDestinationDirectory()).mkdirs();
        }
        File dstFile = new File(outputFile);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(dstFile);
            fileOutputStream.write(fileEvent.getFileData());
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("Output file : " + outputFile + " is successfully saved ");

        }
        catch (Exception e)
        {
            System.out.println("[Server Secundário] - A leitura do ficheiros para backup teve erros:");
            System.out.println(e.getMessage());
            fileEvent.setStatus("Error");
        }

    }

    public static void main(String[] args) {

        System.out.println("ENTROU NO SERVER CLASS DO UDP");

        udpReceiveFileClass server = new udpReceiveFileClass();
        server.createAndListenSocket();
    }
}
import java.io.*;
import java.net.*;

public class udpSendFileClass
{

    private DatagramSocket socket = null;
    private FileEvent event = null;
    private String sourceFilePath;
    private String destinationPath;
    private String neighborHostName;

    public udpSendFileClass(String relativeFilePath, ucServer servidorAtual)
    {
        this.sourceFilePath = System.getProperty("user.dir") + relativeFilePath;
        this.destinationPath = relativeFilePath;
        this.neighborHostName = servidorAtual.secondaryAddress;
    }

    public void createConnection()
    {
        try
        {
            socket = new DatagramSocket();
            System.out.println("PRIMARY -> " + neighborHostName);

            InetAddress IPAddress = InetAddress.getByName(neighborHostName);
            byte[] incomingData = new byte[1024];
            event = getFileEvent();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(event);
            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
            socket.send(sendPacket);
            System.out.println("File sent from client");
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);
            String response = new String(incomingPacket.getData());
            System.out.println("Response from server:" + response);
            Thread.sleep(2000);

            socket.close();

        }
        catch (Exception e)
        {
            System.out.println("[Server Primário] - O envio de ficheiros para backup teve erros:");
            System.out.println(e.getMessage());
        }
    }

    public FileEvent getFileEvent()
    {
        FileEvent fileEvent = new FileEvent();
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("\\") + 1, sourceFilePath.length());
        String path = sourceFilePath.substring(0, sourceFilePath.lastIndexOf("\\") + 1);
        fileEvent.setDestinationDirectory(destinationPath);
        System.out.println("Destination-> " + destinationPath);
        fileEvent.setFilename(fileName);
        System.out.println("filename-> " + fileName);
        fileEvent.setSourceDirectory(sourceFilePath);
        System.out.println("setSourceDirectory-> " + sourceFilePath);
        File file = new File(sourceFilePath);
        if (file.isFile()) {
            try {
                DataInputStream diStream = new DataInputStream(new FileInputStream(file));
                long len = (int) file.length();
                byte[] fileBytes = new byte[(int) len];
                int read = 0;
                int numRead = 0;
                while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
                    read = read + numRead;
                }
                fileEvent.setFileSize(len);
                fileEvent.setFileData(fileBytes);
                fileEvent.setStatus("Success");
            }
            catch (Exception e)
            {
                System.out.println("[Server Primário] - A leitura do ficheiros para backup teve erros:");
                System.out.println(e.getMessage());
                fileEvent.setStatus("Error");
            }
        }
        else
        {
            System.out.println("[Server Primário] - O caminho especificado não é válido.");
            fileEvent.setStatus("Error");
        }
        return fileEvent;
    }
}
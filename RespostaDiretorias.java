import java.io.File;
import java.io.Serializable;

public class RespostaDiretorias implements Serializable
{
    private String response;
    private String[] directoryList;
    private String mainDirectory;

    public RespostaDiretorias(String response, String mainDirectory)
    {
        this.response = response;
        this.mainDirectory = mainDirectory;
        File tmp = new File(mainDirectory);
        // Precisa deste IF para ter algo iniciado
        if (tmp.list() == null)
        {
            this.directoryList = new String[]{};
        }
        else
        {
            this.directoryList = tmp.list();
        }

    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String[] getDirectoryList()
    {
        return directoryList;
    }

    public String getMainDirectory() {
        return mainDirectory;
    }

}

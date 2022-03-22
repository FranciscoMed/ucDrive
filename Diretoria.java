import java.io.File;
import java.io.Serializable;

public class Diretoria implements Serializable {
    private String[] directoryList;
    private String mainDirectory;

    public Diretoria(String mainDirectory)
    {

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


    public String[] getDirectoryList()
    {
        return directoryList;
    }

    public String getMainDirectory() {
        return mainDirectory;
    }

}

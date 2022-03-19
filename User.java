import java.io.Serializable;

public class User implements Serializable
{
    private static final long serialVersionUID = 3687753553092940838L;

    private String username;
    private String password;
    private String directory;
    private String fullDirectory;

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
        this.directory = username;
        this.fullDirectory = ucServer.usersFolderPath + "\\" + username;
    }

    public User(String username, String password, String directory)
    {
        this.username = username;
        this.password = password;
        this.directory = directory;
        this.fullDirectory = ucServer.usersFolderPath + "\\" + directory;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", directory='" + directory + '\'' +
                ", fullDirectory='" + fullDirectory + '\'' +
                '}';
    }

    public String getFullDirectory() {
        return fullDirectory;
    }

    public void setFullDirectory(String fullDirectory) {
        this.fullDirectory = fullDirectory;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private String directory;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.directory = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", directory='" + directory + '\'' +
                '}';
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

package db.vo;

/**
 * @author xin
 * @version 1.0
 */
public class ConnMsg {

    private String url;
    private int port;
    private String databaseName;
    private String username;
    private String password;

    public ConnMsg(String url, int port, String databaseName, String username, String password) {
        this.url = url;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

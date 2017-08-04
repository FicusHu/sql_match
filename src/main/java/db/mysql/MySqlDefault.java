package db.mysql;


import db.vo.ConnMsg;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class MySqlDefault extends ConnMsg {

    public MySqlDefault(String dbName) {
        this("localhost",3306,dbName,"root","root");
    }

    private MySqlDefault(String url, int port, String databaseName, String username, String password) {
        super(url, port, databaseName, username, password);
    }
}

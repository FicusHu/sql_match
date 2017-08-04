package db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author xin
 * @version 1.0
 */
public interface DataBase {

    Connection connect() throws SQLException, ClassNotFoundException;

    void close() throws SQLException;


}

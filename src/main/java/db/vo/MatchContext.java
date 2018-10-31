package db.vo;

import db.mysql.MySql;
import entity.Column;
import entity.Index;
import entity.TableSchedule;
import property.ConnMsg;
import sql.SqlSelect;

import java.sql.SQLException;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class MatchContext {
    private MySql mySql;
    private ConnMsg connMsg;

    public MatchContext(ConnMsg connMsg) {
        this.connMsg = connMsg;
        this.mySql = new MySql(connMsg);
    }

    public MySql getMySql() {
        return mySql;
    }

    public List<TableSchedule> getTableSchedules() throws SQLException {
        return SqlSelect.selectTableSchedules(mySql, connMsg.getDatabase());
    }

    public List<Index> getIndexs(TableSchedule tableSchedule) throws SQLException {
        return SqlSelect.selectIndexs(mySql, tableSchedule);
    }

    public List<Column> getColumn(TableSchedule tableSchedule) throws SQLException {
        return SqlSelect.selectColumns(mySql, tableSchedule);
    }
}

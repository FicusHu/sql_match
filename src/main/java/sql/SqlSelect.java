package sql;

import com.alibaba.fastjson.JSON;
import db.mysql.MySql;
import entity.Column;
import entity.Index;
import entity.TableSchedule;

import java.sql.SQLException;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description 负责sql的查询, 查询索引, 字段等信息
 */
public class SqlSelect {

    public static List<TableSchedule> selectTableSchedules(MySql mySql, String scheduleName) throws SQLException {
        return JSON.parseArray(JSON.toJSONString(mySql.executeSql("SELECT * FROM information_schema. TABLES WHERE TABLE_SCHEMA = '" + scheduleName + "'")), TableSchedule.class);
    }

    public static List<Index> selectIndexs(MySql mysql, TableSchedule tableSchedule) throws SQLException {
        return JSON.parseArray(JSON.toJSONString(mysql.executeSql("SHOW INDEX FROM " + tableSchedule.getTABLE_SCHEMA() + "." + tableSchedule.getTABLE_NAME())), Index.class);
    }

    public static List<Column> selectColumns(MySql mySql, TableSchedule tableSchedule) throws SQLException {
        return JSON.parseArray(JSON.toJSONString(mySql.executeSql("SHOW FULL COLUMNS FROM " + tableSchedule.getTABLE_SCHEMA() + "." + tableSchedule.getTABLE_NAME())), Column.class);
    }
}

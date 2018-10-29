package boot;

import db.mysql.MySql;
import entity.Column;
import entity.Index;
import entity.TableSchedule;
import org.apache.commons.lang3.StringUtils;
import sql.SqlCreate;
import sql.SqlSelect;
import sql.TableCreate;
import tool.Tools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class Main {

    public static final MySql mySqlA;
    public static final MySql mySqlB;
    public static final boolean TIP_SHOW = true;

    static {
        try {
            mySqlA = new MySql(Config.connMsgA);
            mySqlA.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            mySqlB = new MySql(Config.connMsgB);
            mySqlB.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用于保存输出所有生成的sql
     */
    public static List<String> allSql = new ArrayList<>();


    public static void main(String[] args) throws SQLException {
        List<TableSchedule> tableSchedules = SqlSelect.selectTableSchedules(mySqlA, Config.connMsgA.getDatabaseName());
        List<TableSchedule> tableSchedules1 = SqlSelect.selectTableSchedules(mySqlB, Config.connMsgB.getDatabaseName());
        //过滤某些表
        tableSchedules = tableSchedules.stream().filter(Config.tableSchedulePredicate).collect(Collectors.toList());
        tableSchedules1 = tableSchedules1.stream().filter(Config.tableSchedulePredicate).collect(Collectors.toList());

        Map<String, TableSchedule> tableScheduleMap = Tools.getMap(tableSchedules, TableSchedule::getTABLE_NAME);
        Map<String, TableSchedule> tableScheduleMap1 = Tools.getMap(tableSchedules1, TableSchedule::getTABLE_NAME);
        //表匹配
        Set<String> intersectionTable = matchTables(tableSchedules, tableSchedules1);

        for (String tableName : intersectionTable) {
            TableSchedule aTableSchedule = tableScheduleMap.get(tableName);
            TableSchedule bTableSchedule = tableScheduleMap1.get(tableName);
            //字段匹配
            Set<String> removeColumnName = matchColumn(aTableSchedule, bTableSchedule);
            //索引匹配
            String matchSql = matchIndex(aTableSchedule, bTableSchedule, removeColumnName);
            if (!StringUtils.isEmpty(matchSql)) {
                allSql.add(matchSql);
            }
        }

        for (String sql : allSql) {
            System.out.println(sql);
            System.out.println("");
        }

    }


    /**
     * 字段匹配
     * ALTER TABLE `qrtz_locks`
     * ADD COLUMN `sdfgsdfg`  varchar(255) NULL AFTER `tsttt2`
     */
    private static Set<String> matchColumn(TableSchedule aTableSchedule, TableSchedule bTableSchedule) throws SQLException {
        List<Column> aColumns = SqlSelect.selectColumns(mySqlA, aTableSchedule);
        List<Column> bColumns = SqlSelect.selectColumns(mySqlB, bTableSchedule);

        Map<String, Column> aColumnMap = Tools.getMap(aColumns, Column::getField);
        Map<String, Column> bColumnMap = Tools.getMap(bColumns, Column::getField);

        List<String> aColumnNames = aColumns.stream().map(Column::getField).collect(Collectors.toList());
        List<String> bColumnNames = bColumns.stream().map(Column::getField).collect(Collectors.toList());

        List<String> addOrDropSqlComponent = new ArrayList<>();
        //删除字段
        Set<String> removeTest = new HashSet<>(aColumnNames);
        removeTest.removeAll(bColumnNames);

        for (String columnName : removeTest) {
            addOrDropSqlComponent.add(SqlCreate.getDropColumn(columnName));
        }

        //添加字段
        Set<String> addTest = new TreeSet<>(Comparator.comparingInt(bColumnNames::indexOf));
        addTest.addAll(bColumnNames);
        addTest.removeAll(aColumnNames);

        for (String columnName : addTest) {
            Column column = bColumnMap.get(columnName);

            if (bColumns.indexOf(column) == 0) {
                addOrDropSqlComponent.add(SqlCreate.getAddColumn(columnName, column, null));
            } else {
                addOrDropSqlComponent.add(SqlCreate.getAddColumn(columnName, column, bColumns.get(bColumns.indexOf(column) - 1)));
            }
        }

        //修改字段
        Set<String> intersection = Tools.intersection(aColumnNames, bColumnNames);
        for (String columnName : intersection) {
            Column bcolumn = bColumnMap.get(columnName);
            if (!bcolumn.equals(aColumnMap.get(columnName))) {
                if (bColumns.indexOf(bcolumn) == 0) {
                    addOrDropSqlComponent.add(SqlCreate.getModifyColumn(columnName, bcolumn, null, Column.compareTip(aColumnMap.get(columnName), bcolumn)));
                } else {
                    addOrDropSqlComponent.add(SqlCreate.getModifyColumn(columnName, bcolumn, bColumns.get(bColumns.indexOf(bcolumn) - 1), Column.compareTip(aColumnMap.get(columnName), bcolumn)));
                }
            }
        }

        if (!addOrDropSqlComponent.isEmpty()) {
            allSql.add(SqlCreate.getAlterTable(aTableSchedule, addOrDropSqlComponent));
        }
        return removeTest;
    }


    /**
     * 索引匹配
     *
     * @param aTableSchedule
     * @param bTableSchedule
     * @param deleteColumn
     * @return
     * @throws SQLException
     */
    public static String matchIndex(TableSchedule aTableSchedule, TableSchedule bTableSchedule, Set<String> deleteColumn) throws SQLException {
        List<String> sqlComponent = new ArrayList<>();

        sqlComponent.addAll(matchKeySqlComponent(aTableSchedule, bTableSchedule));

        List<Index> aIndexs = Tools.getIndexsWithoutKey(mySqlA, aTableSchedule);
        List<Index> bIndexs = Tools.getIndexsWithoutKey(mySqlB, bTableSchedule);
        //已经删除的属性就不需要再关注它的index了
        aIndexs = aIndexs.stream().filter(index -> !deleteColumn.contains(index.getColumn_name())).collect(Collectors.toList());

        Map<String, List<Index>> aIndexGroup = Tools.getIndexGroupMap(aIndexs);
        Map<String, List<Index>> bIndexGroup = Tools.getIndexGroupMap(bIndexs);

        //删除索引
        Set<List<Index>> deleteIndex = Tools.removeFrom_A(aIndexGroup.values(), bIndexGroup.values());
        List<String> dropCollection = SqlCreate.dropIndexSql(deleteIndex);
        sqlComponent.addAll(dropCollection);

        //新增索引
        Set<List<Index>> addIndexs = Tools.addToA(aIndexGroup.values(), bIndexGroup.values());
        List<String> addCollect = addIndexs.stream().map(SqlCreate::getAddIndex).collect(Collectors.toList());
        sqlComponent.addAll(addCollect);

        return !sqlComponent.isEmpty() ? SqlCreate.getAlterTable(aTableSchedule, sqlComponent) : "";
    }

    /**
     * 主键匹配
     */
    public static List<String> matchKeySqlComponent(TableSchedule aTableSchedule, TableSchedule bTableSchedule) throws SQLException {
        List<Index> aPrimaryKeys = Tools.getPrimaryKeys(mySqlA, aTableSchedule);
        List<Index> bPrimaryKeys = Tools.getPrimaryKeys(mySqlB, bTableSchedule);

        if (aPrimaryKeys.equals(bPrimaryKeys)) {
            return Collections.emptyList();
        } else {
            //主键需要进行修改, 先删后加
            List<String> components = new ArrayList<>();
            components.add(SqlCreate.getDropPrimaryKey());
            components.add(SqlCreate.getAddPrimaryKey(bPrimaryKeys));
            return components;
        }
    }

    /**
     * 表匹配
     */
    public static Set<String> matchTables(List<TableSchedule> aTableSchedules, List<TableSchedule> bTableSchedules) throws SQLException {
        Set<String> aTableNames = aTableSchedules.stream().map(TableSchedule::getTABLE_NAME).collect(Collectors.toSet());
        Set<String> bTableNames = bTableSchedules.stream().map(TableSchedule::getTABLE_NAME).collect(Collectors.toSet());
        Map<String, TableSchedule> bTableScheduleMap = Tools.getMap(bTableSchedules, TableSchedule::getTABLE_NAME);


        Set<String> removeTest = Tools.removeFrom_A(aTableNames, bTableNames);
        //删除的表
        for (String tableName : removeTest) {
            allSql.add(SqlCreate.getDropTable(tableName));
        }
        Set<String> addTest = Tools.addToA(aTableNames, bTableNames);
        //新增的表
        for (String tableName : addTest) {
            allSql.add(new TableCreate(mySqlB, bTableScheduleMap.get(tableName)).newTableSql() + "\n");
        }
        aTableNames.removeAll(removeTest);
        return aTableNames;
    }


}

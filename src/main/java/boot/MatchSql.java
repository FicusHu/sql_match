package boot;

import db.mysql.MySql;
import db.vo.ModifySql;
import entity.Column;
import entity.Index;
import entity.TableSchedule;
import org.apache.commons.lang3.StringUtils;
import property.ConnMsg;
import property.OtherSetting;
import sql.SqlCreate;
import sql.SqlSelect;
import sql.TableCreate;
import tool.Tools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class MatchSql {


    private Predicate<TableSchedule> tableSchedulePredicate = tableSchedule -> true;
    private MySql mySqlA;

    private MySql mySqlB;
    private ConnMsg connMsgA;
    private ConnMsg connMsgB;
    private OtherSetting otherSetting;

    public MatchSql(ConnMsg connMsgA, ConnMsg connMsgB, OtherSetting otherSetting) throws SQLException {
        this.connMsgA = connMsgA;
        this.connMsgB = connMsgB;
        this.otherSetting = otherSetting;
        this.mySqlA = new MySql(connMsgA);
        mySqlA.connect();
        this.mySqlB = new MySql(connMsgB);
        mySqlB.connect();
        if (StringUtils.isNotEmpty(otherSetting.getFilterTables())) {
            Set<String> filterTables = new HashSet<>(Arrays.asList(otherSetting.getFilterTables().split(",")));
            tableSchedulePredicate = tableSchedule -> !filterTables.contains(tableSchedule.getTABLE_NAME());
        }
    }

    public List<String> match() throws SQLException {

        /*
          用于保存输出所有生成的sql
         */
        List<String> allModifySql = new ArrayList<>();

        List<TableSchedule> tableScheduleAs = getTableSchedules(mySqlA, connMsgA.getDatabase());
        List<TableSchedule> tableScheduleBs = getTableSchedules(mySqlB, connMsgB.getDatabase());

        Map<String, TableSchedule> tableScheduleMap = Tools.getMap(tableScheduleAs, TableSchedule::getTABLE_NAME);
        Map<String, TableSchedule> tableScheduleMap1 = Tools.getMap(tableScheduleBs, TableSchedule::getTABLE_NAME);


        //表匹配
        Set<String> intersectionTable = matchTables(allModifySql);


        for (String tableName : intersectionTable) {
            TableSchedule aTableSchedule = tableScheduleMap.get(tableName);
            TableSchedule bTableSchedule = tableScheduleMap1.get(tableName);
            matchColumnAndIndex(aTableSchedule, bTableSchedule, allModifySql);
        }

        return allModifySql;

    }

    private void matchColumnAndIndex(TableSchedule aTableSchedule, TableSchedule bTableSchedule, List<String> allModifySql) throws SQLException {
        //字段匹配
        List<Column> aColumns = SqlSelect.selectColumns(mySqlA, aTableSchedule);
        List<Column> bColumns = SqlSelect.selectColumns(mySqlB, bTableSchedule);

        Map<String, Column> aColumnMap = Tools.getMap(aColumns, Column::getField);
        Map<String, Column> bColumnMap = Tools.getMap(bColumns, Column::getField);


        List<String> aColumnNames = aColumns.stream().map(Column::getField).collect(Collectors.toList());
        List<String> bColumnNames = bColumns.stream().map(Column::getField).collect(Collectors.toList());

        List<ModifySql> addOrDropSqlComponent = new ArrayList<>();
        //删除字段
        Set<String> removeTest = new HashSet<>(aColumnNames);
        removeTest.removeAll(bColumnNames);

        for (String columnName : removeTest) {
            addOrDropSqlComponent.add(new ModifySql(SqlCreate.getDropColumn(columnName)));
        }

        //添加字段
        Set<String> addTest = new TreeSet<>(Comparator.comparingInt(bColumnNames::indexOf));
        addTest.addAll(bColumnNames);
        addTest.removeAll(aColumnNames);

        for (String columnName : addTest) {
            Column column = bColumnMap.get(columnName);

            if (bColumns.indexOf(column) == 0) {
                addOrDropSqlComponent.add(new ModifySql(SqlCreate.getAddColumn(columnName, column, null)));
            } else {
                addOrDropSqlComponent.add(new ModifySql(SqlCreate.getAddColumn(columnName, column, bColumns.get(bColumns.indexOf(column) - 1))));
            }
        }

        //修改字段
        List<String> intersection = Tools.intersection(aColumnNames, bColumnNames);
        for (String columnName : intersection) {
            Column bcolumn = bColumnMap.get(columnName);
            if (!bcolumn.compared(aColumnMap.get(columnName), otherSetting.isMatchComment())) {
                if (bColumns.indexOf(bcolumn) == 0) {
                    addOrDropSqlComponent.add(new ModifySql(SqlCreate.getModifyColumn(columnName, bcolumn, null), Column.compareTip(aColumnMap.get(columnName), bcolumn)));
                } else {
                    addOrDropSqlComponent.add(new ModifySql(SqlCreate.getModifyColumn(columnName, bcolumn, bColumns.get(bColumns.indexOf(bcolumn) - 1)), Column.compareTip(aColumnMap.get(columnName), bcolumn)));
                }
            }
        }

        //索引匹配
        List<ModifySql> matchSql = matchIndex(aTableSchedule, bTableSchedule, removeTest);

        addOrDropSqlComponent.addAll(matchSql);

        if (!addOrDropSqlComponent.isEmpty()) {
            allModifySql.add(SqlCreate.getAlterTable(aTableSchedule, addOrDropSqlComponent, otherSetting.isTipsShow()));
        }
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
    private List<ModifySql> matchIndex(TableSchedule aTableSchedule, TableSchedule bTableSchedule, Set<String> deleteColumn) throws SQLException {
        List<ModifySql> sqlComponent = new ArrayList<>();

        sqlComponent.addAll(matchKeySqlComponent(aTableSchedule, bTableSchedule));

        List<Index> aIndexs = Tools.getIndexsWithoutKey(mySqlA, aTableSchedule);
        List<Index> bIndexs = Tools.getIndexsWithoutKey(mySqlB, bTableSchedule);
        //已经删除的属性就不需要再关注它的index了
        aIndexs = aIndexs.stream().filter(index -> !deleteColumn.contains(index.getColumn_name())).collect(Collectors.toList());

        Map<String, List<Index>> aIndexGroup = Tools.getIndexGroupMap(aIndexs);
        Map<String, List<Index>> bIndexGroup = Tools.getIndexGroupMap(bIndexs);

        //删除索引
        List<List<Index>> deleteIndex = Tools.removeFrom_A(aIndexGroup.values(), bIndexGroup.values());
        List<ModifySql> dropCollection = SqlCreate.dropIndexSql(deleteIndex);
        sqlComponent.addAll(dropCollection);

        //新增索引
        List<List<Index>> addIndexs = Tools.addToA(aIndexGroup.values(), bIndexGroup.values());
        List<ModifySql> addCollect = addIndexs.stream().map(SqlCreate::getAddIndex).collect(Collectors.toList());
        sqlComponent.addAll(addCollect);

        return sqlComponent;
    }

    /**
     * 主键匹配
     */
    private List<ModifySql> matchKeySqlComponent(TableSchedule aTableSchedule, TableSchedule bTableSchedule) throws SQLException {
        List<Index> aPrimaryKeys = Tools.getPrimaryKeys(mySqlA, aTableSchedule);
        List<Index> bPrimaryKeys = Tools.getPrimaryKeys(mySqlB, bTableSchedule);

        if (aPrimaryKeys.equals(bPrimaryKeys)) {
            return Collections.emptyList();
        } else {
            //主键需要进行修改, 先删后加
            List<ModifySql> components = new ArrayList<>();
            components.add(new ModifySql(SqlCreate.getDropPrimaryKey()));
            components.add(new ModifySql(SqlCreate.getAddPrimaryKey(bPrimaryKeys)));

            return components;
        }
    }

    /**
     * 表匹配
     */
    private Set<String> matchTables(List<String> allModifySql) throws SQLException {

        List<TableSchedule> tableScheduleAs = getTableSchedules(mySqlA, connMsgA.getDatabase());
        List<TableSchedule> tableScheduleBs = getTableSchedules(mySqlB, connMsgB.getDatabase());

        Set<String> aTableNames = tableScheduleAs.stream().map(TableSchedule::getTABLE_NAME).collect(Collectors.toSet());
        Set<String> bTableNames = tableScheduleBs.stream().map(TableSchedule::getTABLE_NAME).collect(Collectors.toSet());
        Map<String, TableSchedule> bTableScheduleMap = Tools.getMap(tableScheduleBs, TableSchedule::getTABLE_NAME);


        List<String> removeTest = Tools.removeFrom_A(aTableNames, bTableNames);
        //删除的表
        for (String tableName : removeTest) {
            allModifySql.add(SqlCreate.getDropTable(tableName));
        }
        List<String> addTest = Tools.addToA(aTableNames, bTableNames);
        //新增的表
        for (String tableName : addTest) {
            allModifySql.add(new TableCreate(mySqlB, bTableScheduleMap.get(tableName)).newTableSql() + "\n");
        }
        aTableNames.removeAll(removeTest);
        return aTableNames;
    }

    public List<TableSchedule> getTableSchedules(MySql mySql, String database) throws SQLException {
        List<TableSchedule> tableSchedules = SqlSelect.selectTableSchedules(mySql, database);
        //过滤某些表
        tableSchedules = tableSchedules.stream().filter(tableSchedulePredicate).collect(Collectors.toList());
        return tableSchedules;
    }


}

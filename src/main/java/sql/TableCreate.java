package sql;

import db.mysql.MySql;
import entity.Column;
import entity.Index;
import entity.TableSchedule;
import org.apache.commons.lang3.StringUtils;
import tool.Tools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description 专门负责生成表的DDL 生成代码
 */
public class TableCreate {

    private final MySql mySql;
    private final TableSchedule tableSchedule;

    public TableCreate(MySql mySql, TableSchedule tableSchedule) {
        this.mySql = mySql;
        this.tableSchedule = tableSchedule;
    }

    public String newTableSql() throws SQLException {
        List<Column> column1s = SqlSelect.selectColumns(mySql, tableSchedule);
        List<Index> indexs = SqlSelect.selectIndexs(mySql, tableSchedule);

        List<String> createTableComponent = new ArrayList<>();

        createTableComponent.addAll(column1s.stream()
                                            .map(Column::toTableCreateColumn)
                                            .collect(Collectors.toList()));

        createTableComponent.add(getKey(indexs));
        createTableComponent.addAll(getIndexWithoutKey(indexs));
        return createTableComponent.stream().filter(s -> !StringUtils.isEmpty(s))
                .map(e -> "  " + e)
                .collect(Collectors.joining(",\n"
                        , "CREATE TABLE `" + tableSchedule.getTABLE_NAME() + "` (\n"
                        , "\n) ENGINE=" + tableSchedule.getENGINE() + " DEFAULT CHARSET=" + tableSchedule.getTABLE_COLLATION().substring(0, tableSchedule.getTABLE_COLLATION().indexOf("_"))
                                                    + " COLLATE=" + tableSchedule.getTABLE_COLLATION() + " " + sql.SqlCreate.getComment(tableSchedule.getTABLE_COMMENT()) + ";"));

    }


    public List<String> getIndexWithoutKey(List<Index> indexs) {
        Map<String, List<Index>> indexGroup = indexs.stream().filter(Tools.IS_PRIMARY_PREDICATE.negate()).collect(Collectors.groupingBy(Index::getKey_name, Collectors.toList()));
        Comparator<Index> comparingIndex = Comparator.comparing(Index::getSeq_in_index);
        for (List<Index> indices : indexGroup.values()) {
            indices.sort(comparingIndex);
        }
        return indexGroup.keySet().stream().map(key -> {
            List<Index> indices = indexGroup.get(key);
            String union = "";
            if (indices.get(0).getNon_unique().equals(0L)) {
                union = "UNIQUE ";
            }
            return union + "KEY `" + key + "` " + indices.stream().map(Index.indexColumn).collect(Collectors.joining(",", "(", ")")) + " USING " + indices.get(0).getIndex_type();
        }).collect(Collectors.toList());
    }

    public String getKey(List<Index> indexs) throws SQLException {
        List<Index> keyGroup = Tools.getPrimaryKeys(indexs);
        return sql.SqlCreate.getPrimaryKey(keyGroup);
    }


}

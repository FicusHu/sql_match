package sql;

import entity.Column;
import entity.Index;
import entity.TableSchedule;
import org.apache.commons.lang3.StringUtils;
import tool.Tools;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description 负责生成一些修改的sql
 */
public class SqlCreate {

    public static List<String> dropIndexSql(Set<List<Index>> deleteIndex) {
        return deleteIndex.stream().map(indices -> "DROP INDEX `" + indices.get(0).getKey_name() + "`").collect(Collectors.toList());
    }

    public static String getIndexTypeSql(Index sampleIndex) {
        String index = "INDEX";
        if (sampleIndex.getNon_unique().equals(0L)) {
            index = "UNIQUE INDEX";
        }
        return index;
    }

    public static String getAddPrimaryKey(List<Index> bPrimaryKeys) {
        return "ADD PRIMARY KEY " + bPrimaryKeys.stream().map(Index.indexColumn).collect(Collectors.joining(",", "(", ")"));
    }

    public static String getDropPrimaryKey() {
        return "DROP PRIMARY KEY";
    }

    public static String getDropTable(String tableName) {
        return "DROP TABLE " + tableName + " ;";
    }

    public static String getDropColumn(String columnName) {
        return "DROP COLUMN `" + columnName + "`";
    }

    public static String getAddColumn(String columnName, Column column, Column preColumn) {
        return "ADD " + getfModityOrAddColumn(columnName, column, preColumn);
    }

    public static String getModifyColumn(String columnName, Column column, Column preColumn) {

        return "MODIFY " + getfModityOrAddColumn(columnName, column, preColumn);
    }

    private static String getfModityOrAddColumn(String columnName, Column column, Column preColumn) {
        String position;
        if (preColumn == null) {
            position = " FIRST";
        } else {
            position = " AFTER `" + preColumn.getField() + "`";
        }


        // 类似 MODIFY COLUMN `tsttt2`  varchar(255) NULL COMMENT '测试属性' AFTER `tettt`
        String template = "COLUMN `%s` %s  %s %s %s";
        return String.format(template
                , columnName
                , column.getType()
                , Tools.isNullStr(column.getNull())
                , getComment(column.getComment())
                , position);
    }


    public static String getAlterTable(TableSchedule aTableSchedule, List<String> addOrDropSqlComponent) {
        return addOrDropSqlComponent.stream().collect(Collectors.joining(",\n", "ALTER TABLE `" + aTableSchedule.getTABLE_NAME() + "` \n", ";"));
    }

    public static String getPrimaryKey(List<Index> indexGroup) {
        return indexGroup.stream().map(Index::getColumn_name).collect(Collectors.joining("`,`", "PRIMARY KEY (`", "`)"));
    }

    public static String getComment(String comment) {
        if (!StringUtils.isEmpty(comment)) {
            return " COMMENT '" + comment + "' ";
        } else {
            return " ";
        }
    }

    /**
     * ADD `idx_school_uid` (`school_uid`) USING BTREE
     */
    public static String getAddIndex(List<Index> indexs) {
        //多个index的comment和 index类型, tablename都是一样的
        Index sampleIndex = indexs.get(0);
        String suffix = ") USING " + sampleIndex.getIndex_type() + getComment(sampleIndex.getComment());
        String prefix = "ADD " + getIndexTypeSql(sampleIndex) + " `" + sampleIndex.getKey_name() + "` " + "(";
        return indexs.stream().map(Index.indexColumn).collect(Collectors.joining(",", prefix, suffix));
    }
}

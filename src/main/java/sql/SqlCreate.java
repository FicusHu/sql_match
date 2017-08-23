package sql;

import entity.Column;
import entity.Index;
import entity.TableSchedule;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description 负责生成一些修改的sql
 */
public class SqlCreate {

    private static final boolean TIP_SHOW = false;

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
        if (bPrimaryKeys == null || bPrimaryKeys.isEmpty()) {
            return "";
        } else {
            return "ADD " + getPrimaryKey(bPrimaryKeys);
        }
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

    public static String getModifyColumn(String columnName, Column column, Column preColumn, String tips) {

        if (TIP_SHOW == true) {
            return "MODIFY " + getfModityOrAddColumn(columnName, column, preColumn) + " -- " + tips;
        } else {
            return "MODIFY " + getfModityOrAddColumn(columnName, column, preColumn);
        }
    }

    private static String getfModityOrAddColumn(String columnName, Column column, Column preColumn) {
        String position;
        if (preColumn == null) {
            position = "FIRST";
        } else {
            position = "AFTER `" + preColumn.getField() + "`";
        }


        // 类似 MODIFY COLUMN `tsttt2`  varchar(255) NULL DEFAULT '0' COMMENT '测试属性' AFTER `tettt`
        String template = "COLUMN `%s` %s %s %s %s %s";
        return String.format(template
                , columnName
                , column.getType()
                , isNullStr(column.getNull())
                , getDefault(column)
                , getComment(column.getComment())
                , position);
    }


    public static String getAlterTable(TableSchedule aTableSchedule, List<String> addOrDropSqlComponent) {
        return addOrDropSqlComponent.stream()
                .map(e -> " " + e)
                .collect(Collectors.joining(",\n", "ALTER TABLE `" + aTableSchedule.getTABLE_NAME() + "`\n", ";"));
    }

    public static String getPrimaryKey(List<Index> indexGroup) {
        if (indexGroup == null || indexGroup.isEmpty()) {
            return "";
        } else {
            return "PRIMARY KEY " + indexGroup.stream().map(Index::getColumn_name).collect(Collectors.joining("`,`", "(`", "`)"));
        }
    }

    public static String getComment(String comment) {
        if (!StringUtils.isEmpty(comment)) {
            return "COMMENT '" + comment + "'";
        } else {
            return "";
        }
    }

    /**
     * ADD `idx_school_uid` (`school_uid`) USING BTREE
     */
    public static String getAddIndex(List<Index> indexs) {
        //多个index的comment和 index类型, tablename都是一样的
        Index sampleIndex = indexs.get(0);
        String suffix = ") USING " + sampleIndex.getIndex_type() + " " + getComment(sampleIndex.getComment());
        String prefix = "ADD " + getIndexTypeSql(sampleIndex) + " `" + sampleIndex.getKey_name() + "` " + "(";
        return indexs.stream().map(Index.indexColumn).collect(Collectors.joining(",", prefix, suffix));
    }

    public static String getDefault(Column column) {
        if (column.getDefault() != null) {
            if ("datetime".equalsIgnoreCase(column.getType()) && "CURRENT_TIMESTAMP".equalsIgnoreCase(column.getDefault())) {
                return "DEFAULT " + column.getDefault() + "";
            } else {
                return "DEFAULT '" + column.getDefault() + "'";
            }
        } else {
            if ("datetime".equalsIgnoreCase(column.getType()) && "NOT".equalsIgnoreCase(column.getNull())) {
                return "DEFAULT '1970-01-01 00:00'";
            }
            return "";
        }
    }

    public static String isNullStr(String isNull) {
        if ("YES".equalsIgnoreCase(isNull)) {
            return "NULL";
        }
        if ("NO".equalsIgnoreCase(isNull)) {
            return "NOT NULL";
        }

        throw new RuntimeException("不知道是什么类型的null");
    }
}

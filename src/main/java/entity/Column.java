package entity; /**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import sql.SqlCreate;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyright 2017 bejson.com
 */
@Data
public class Column {

    private String Field;
    private String Comment;
    private String Type;
    private String Null;
    private String Extra;
    private String Privileges;
    private String Collation;
    private String Default;
    private String Key;

    public static String compareTip(Column aColumn, Column bColumn) {
        String tips = "";
        if (!Objects.equals(aColumn.getType(), bColumn.getType())) {
            tips = tips + String.format("类型type 不一致, 原 %s : 现 %s; ", aColumn.getType(), bColumn.getType());
        }
        if (!Objects.equals(aColumn.getNull(), bColumn.getNull())) {
            tips = tips + String.format("是否可空 不一致, 原 %s : 现 %s; ", SqlCreate.isNullStr(aColumn.getNull()), SqlCreate.isNullStr(bColumn.getNull()));
        }
        if (!Objects.equals(aColumn.getComment(), bColumn.getComment())) {
            tips = tips + String.format("注释 不一致, 原 %s : 现 %s; ", aColumn.getComment(), bColumn.getComment());
        }
        if (!Objects.equals(aColumn.getDefault(), bColumn.getDefault())) {
            tips = tips + String.format("默认值 不一致, 原 %s : 现 %s; ", aColumn.getDefault(), bColumn.getDefault());
        }

        if (!Objects.equals(aColumn.getExtra(), bColumn.getExtra())) {
            tips = tips + String.format("Extra 不一致, 原 %s : 现 %s; ", aColumn.getExtra(), bColumn.getExtra());
        }
//        if (!Objects.equals(aColumn.getCollation(), bColumn.getCollation())) {
//            tips = tips + String.format("编码 不一致, 原 %s : 现 %s; ", aColumn.getCollation(), bColumn.getCollation());
//        }
        return tips;
    }

    public String toTableCreateColumn() {
        String autoIncrement = SqlCreate.getExtra(getExtra());
        String nullStr = SqlCreate.isNullStr(getNull());
        String defaultStr = SqlCreate.getDefault(this);
        String comment = SqlCreate.getComment(getComment());

        return "`" + getField() + "` " + getType() + " "
                + Stream.of(nullStr, autoIncrement, defaultStr, comment)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" "));
    }

    public boolean compared(Column column, boolean isCommentCompared) {
        if (this == column) return true;
        if (column == null || getClass() != column.getClass()) return false;

        if (Field != null ? !Field.equals(column.Field) : column.Field != null) return false;
        if (isCommentCompared) {
            if (Comment != null ? !Comment.replace("\r\n", "\n").equals(column.Comment.replace("\r\n", "\n")) : column.Comment != null)
                return false;
        }
        if (Type != null ? !Type.equals(column.Type) : column.Type != null) return false;
        if (!"datetime".equals(Type)) {
            if (Null != null ? !Null.equals(column.Null) : column.Null != null) return false;
        }
        if (Extra != null ? !Extra.equals(column.Extra) : column.Extra != null) return false;
//        if (Privileges != null ? !Privileges.equals(column.Privileges) : column.Privileges != null) return false;
//        if (Collation != null ? !Collation.equals(column.Collation) : column.Collation != null) return false;
        return Default != null ? Default.equals(column.Default) : column.Default == null;
    }
}
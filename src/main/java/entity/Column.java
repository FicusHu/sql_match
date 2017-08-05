package entity; /**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */

import lombok.Data;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (Field != null ? !Field.equals(column.Field) : column.Field != null) return false;
        if (Comment != null ? !Comment.equals(column.Comment) : column.Comment != null) return false;
        if (Type != null ? !Type.equals(column.Type) : column.Type != null) return false;
        if (Null != null ? !Null.equals(column.Null) : column.Null != null) return false;
//        if (Extra != null ? !Extra.equals(column.Extra) : column.Extra != null) return false;
//        if (Privileges != null ? !Privileges.equals(column.Privileges) : column.Privileges != null) return false;
//        if (Collation != null ? !Collation.equals(column.Collation) : column.Collation != null) return false;
        return Default != null ? Default.equals(column.Default) : column.Default == null;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (Field != null ? Field.hashCode() : 0);
        result = 31 * result + (Comment != null ? Comment.hashCode() : 0);
        result = 31 * result + (Type != null ? Type.hashCode() : 0);
        result = 31 * result + (Null != null ? Null.hashCode() : 0);
//        result = 31 * result + (Extra != null ? Extra.hashCode() : 0);
//        result = 31 * result + (Privileges != null ? Privileges.hashCode() : 0);
//        result = 31 * result + (Collation != null ? Collation.hashCode() : 0);
        result = 31 * result + (Default != null ? Default.hashCode() : 0);
        return result;
    }
}
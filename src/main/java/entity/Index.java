package entity;

import lombok.Data;

import java.util.function.Function;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
@Data
public class Index {

    private String Comment;
    private String Null;
    private String Table;
    //    private Long Cardinality;
    private Long Sub_part;
    //    private Long Packed;
    private Long Non_unique;
    //    private String Collation;
    private String Column_name;
    private String Index_comment;
    private Long Seq_in_index;
    private String Key_name;
    private String Index_type;

    public static Function<Index, String> indexColumn = index -> {
        if (index.getSub_part() != null) {
            return "`" + index.getColumn_name() + "`(" + index.getSub_part() + ")";
        } else {
            return "`" + index.getColumn_name() + "`";
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Index index = (Index) o;

        if (Comment != null ? !Comment.equals(index.Comment) : index.Comment != null) return false;
        if (Null != null ? !Null.equals(index.Null) : index.Null != null) return false;
        if (Table != null ? !Table.equals(index.Table) : index.Table != null) return false;
        if (Sub_part != null ? !Sub_part.equals(index.Sub_part) : index.Sub_part != null) return false;
//        if (Packed != null ? !Packed.equals(index.Packed) : index.Packed != null) return false;
        if (Non_unique != null ? !Non_unique.equals(index.Non_unique) : index.Non_unique != null) return false;
//        if (Collation != null ? !Collation.equals(index.Collation) : index.Collation != null) return false;
        if (Column_name != null ? !Column_name.equals(index.Column_name) : index.Column_name != null) return false;
        if (Index_comment != null ? !Index_comment.equals(index.Index_comment) : index.Index_comment != null)
            return false;
        if (Seq_in_index != null ? !Seq_in_index.equals(index.Seq_in_index) : index.Seq_in_index != null) return false;
        if (Key_name != null ? !Key_name.equals(index.Key_name) : index.Key_name != null) return false;
        return Index_type != null ? Index_type.equals(index.Index_type) : index.Index_type == null;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (Comment != null ? Comment.hashCode() : 0);
        result = 31 * result + (Null != null ? Null.hashCode() : 0);
        result = 31 * result + (Table != null ? Table.hashCode() : 0);
//        result = 31 * result + (Cardinality != null ? Cardinality.hashCode() : 0);
        result = 31 * result + (Sub_part != null ? Sub_part.hashCode() : 0);
//        result = 31 * result + (Packed != null ? Packed.hashCode() : 0);
        result = 31 * result + (Non_unique != null ? Non_unique.hashCode() : 0);
//        result = 31 * result + (Collation != null ? Collation.hashCode() : 0);
        result = 31 * result + (Column_name != null ? Column_name.hashCode() : 0);
        result = 31 * result + (Index_comment != null ? Index_comment.hashCode() : 0);
        result = 31 * result + (Seq_in_index != null ? Seq_in_index.hashCode() : 0);
        result = 31 * result + (Key_name != null ? Key_name.hashCode() : 0);
        result = 31 * result + (Index_type != null ? Index_type.hashCode() : 0);
        return result;
    }
}

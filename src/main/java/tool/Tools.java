package tool;

import db.mysql.MySql;
import entity.Index;
import entity.TableSchedule;
import sql.SqlSelect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description 工具类
 */
public class Tools {


    public static final Predicate<Index> IS_PRIMARY_PREDICATE = index -> "PRIMARY".equals(index.getKey_name());


    /**
     * 获取数据库表的索引
     */
    public static List<Index> getIndexsWithoutKey(MySql mysql, TableSchedule tableSchedule) throws SQLException {
        List<Index> indices = SqlSelect.selectIndexs(mysql, tableSchedule).stream().filter(IS_PRIMARY_PREDICATE.negate()).collect(Collectors.toList());
        indices.sort(Comparator.comparing(Index::getSeq_in_index));
        return indices;
    }

    /**
     * 获取数据库表的primary key
     */
    public static List<Index> getPrimaryKeys(MySql mysql, TableSchedule tableSchedule) throws SQLException {
        return SqlSelect.selectIndexs(mysql, tableSchedule).stream().filter(IS_PRIMARY_PREDICATE).collect(Collectors.toList());
    }

    public static List<Index> getPrimaryKeys(List<Index> indexs) throws SQLException {
        return indexs.stream().filter(IS_PRIMARY_PREDICATE).collect(Collectors.toList());
    }

    /**
     * 求出属于A但不属于B的字符串
     */
    public static <T> List<T> removeFrom_A(Collection<T> aList, Collection<T> bList) {
        List<T> orgAList = new ArrayList<>(aList);
        orgAList.removeAll(bList);
        return orgAList;
    }

    /**
     * 求出属于B但不属于A的字符串
     */
    public static <T> List<T> addToA(Collection<T> aList, Collection<T> bList) {
        List<T> orgAList = new ArrayList<>(bList);
        orgAList.removeAll(aList);
        return orgAList;
    }

    /**
     * 交集
     */
    public static <T> List<T> intersection(Collection<T> aList, Collection<T> bList) {
        ArrayList<T> nAList = new ArrayList<>(aList);
        ArrayList<T> intersection = new ArrayList<>();
        for (T b : bList) {
            if (nAList.remove(b)) {
                intersection.add(b);
            }
        }
        return intersection;
    }

    public static <T, D> Map<D, T> getMap(List<T> list, Function<T, D> keyMap) {
        return list.stream().collect(Collectors.toMap(keyMap, o -> o));
    }

    /**
     * show index 出来的结构是这样子的, 一个索引会对应多行记录, 根据 Seq_in_index 排索引的顺序, 所以需要先
     * 进行key的分组, 分组完最好根据Seq_in_index 排个序
     * Seq_in_index
     * table1	    0	PRIMARY	            1	name	A	2				BTREE
     * table1	    0	PRIMARY	            2	id	        A	2958			BTREE
     * table1	    1	school_id_index	    1	version     A	60				BTREE
     * table1	    1	school_id_index 	2	name	A	60				BTREE
     * table1	    1	school_id_index2	1	time	A	2958			BTREE
     * 根据index进行分组
     */
    public static Map<String, List<Index>> getIndexGroupMap(List<Index> aIndexs) {
        Comparator<Index> comparingIndex = Comparator.comparing(Index::getSeq_in_index);
        Map<String, List<Index>> collect = aIndexs.stream().collect(Collectors.groupingBy(Index::getKey_name, Collectors.toList()));

        for (List<Index> indices : collect.values()) {
            indices.sort(comparingIndex);
        }
        return collect;
    }
}

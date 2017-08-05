import db.vo.ConnMsg;
import entity.TableSchedule;

import java.util.function.Predicate;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class Config {


    public static final ConnMsg connMsgB = new ConnMsg("localhost", 3306, "test1", "root", "root");
    public static final ConnMsg connMsgA = new ConnMsg("localhost", 3306, "test2", "root", "root");

    public static final Predicate<TableSchedule> tableSchedulePredicate = tableSchedule -> true;
}

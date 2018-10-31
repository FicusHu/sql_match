package boot;

import org.apache.commons.beanutils.BeanUtils;
import property.ConnMsg;
import property.OtherSetting;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Main {


    public static void main(String[] args) throws SQLException, IOException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Properties props = new Properties();
        props.load(ClassLoader.getSystemResourceAsStream("config.properties"));

        ConnMsg connMsgA = propertyToObject(props, "from.database.", ConnMsg.class);
        ConnMsg connMsgB = propertyToObject(props, "to.database.", ConnMsg.class);
        OtherSetting otherSetting = propertyToObject(props, "", OtherSetting.class);


        MatchSql matchSql = new MatchSql(connMsgA, connMsgB, otherSetting);
        List<String> allModifySql = matchSql.match();

        System.out.println();
        for (String sql : allModifySql) {
            System.out.println(sql);
            System.out.println();
        }
    }

    private static <T> T propertyToObject(Properties properties, String prex, Class<T> cls) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T bean = cls.newInstance();
        for (Field declaredField : cls.getDeclaredFields()) {
            String fieldName = declaredField.getName();
            BeanUtils.setProperty(bean, fieldName, properties.getProperty(prex + fieldName));
        }
        return bean;
    }
}

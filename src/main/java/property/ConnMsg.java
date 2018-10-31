package property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xin
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnMsg {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
}
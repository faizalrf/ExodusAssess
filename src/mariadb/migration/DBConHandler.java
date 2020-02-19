package mariadb.migration;

import java.sql.Connection;
public interface DBConHandler {
    Connection ConnectDB() throws Exception;
    Connection getDBConnection();
    void setSQLMode();
    void SetCurrentSchema(String SchemaName);
    void DisconnectDB();
    String getSQLMode();
}

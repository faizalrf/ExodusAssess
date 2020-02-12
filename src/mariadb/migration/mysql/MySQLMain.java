package mariadb.migration.mysql;

import mariadb.migration.*;

public class MySQLMain {
    public String LogPath = Util.getPropertyValue("LogPath");
    
    DBCredentialsReader UCR = new DBCredentialsReader("resources/dbdetails.xml");
    
    //ALL Database will be scanned
    public MySQLMain() {
        for (DataSources DC : UCR.DBCredentialsList()) {
            StartExodusAssess(DC);
        }
        System.out.println("\n\n");
    }

    //Specific Database Source Execution and/or ALL
    public MySQLMain(String strSourceDB) {
        for (DataSources DC : UCR.DBCredentialsList()) {
            if (DC.getDBType().toLowerCase().equals(strSourceDB) || strSourceDB.equals("all")) {
                StartExodusAssess(DC);
            }
        }
        System.out.println("\n\n");
    }

    public void StartExodusAssess(DataSources oDataSource) {
        //Read The Database
        MySQLConnect SourceCon = new MySQLConnect(oDataSource.getDBType());
        Util.BoxedText("Current Server: " + oDataSource.getHostName(), "=", 130);

        MySQLDatabase MyDB = new MySQLDatabase(SourceCon.getDBConnection());
        Util.BoxedText("Parsing Completed", "-", 130);

        try {
            for (SchemaHandler oSchema : MyDB.getSchemaList()) {
                //Switch to the respective Schema
                //TargetCon.SetCurrentSchema(oSchema.getSchemaName());
                
                for (TableHandler Tab : oSchema.getTables()) {
                    //System.out.println("Oh What a Lovely Table " + Tab.getTableName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error While Processing");
            new Logger(LogPath + "/Exodus.err", "Error While Processing - " + e.getMessage(), true);
            e.printStackTrace();
        } finally {
            SourceCon.DisconnectDB();
        }
    }
}

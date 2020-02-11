package mariadb.migration.mysql;

import mariadb.migration.*;

public class MySQLMain {
    public String LogPath = Util.getPropertyValue("LogPath");
    public MySQLMain() {
        DBCredentialsReader UCR;
        UCR = new DBCredentialsReader("resources/dbdetails.xml");
        for (DataSources DC : UCR.DBCredentialsList())
            StartExodusAssess(DC.getDBType());

        System.out.println("\n\n");
    }

    public void StartExodusAssess(String strSourceCon) {
        //Read The Database
        MySQLConnect SourceCon = new MySQLConnect(strSourceCon);
        MySQLDatabase MyDB = new MySQLDatabase(SourceCon.getDBConnection());
        System.out.println("\n\n-------------------------------------------------------");
        System.out.println("- Parsing Completed, Starting Single Threaded Process -");
        System.out.println("-------------------------------------------------------");

        try {
            for (SchemaHandler oSchema : MyDB.getSchemaList()) {
                //Switch to the respective Schema
                //TargetCon.SetCurrentSchema(oSchema.getSchemaName());
                
                System.out.println("\n-------------------------------------------------------");
                System.out.println("- Starting `" + oSchema.getSchemaName() + "` Migration");
                System.out.println("-------------------------------------------------------");

                for (TableHandler Tab : oSchema.getTables()) {
                    //Tab.
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

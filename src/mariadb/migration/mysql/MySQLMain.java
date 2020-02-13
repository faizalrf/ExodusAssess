package mariadb.migration.mysql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        Util.BoxedText("\n", "Starting Compatibility Check", "", "=", 130);

        try {
            //Run Validations
            for (SchemaHandler oSchema : MyDB.getSchemaList()) {
                Util.BoxedText("", "Checking for Unsupported Data Types", "", "*", 130);
                ValidateDataTypes(oSchema);

                Util.BoxedText("\n", "Checking for MySQL Specific Functions in Views & Source Code", "", "*", 130);
                ValidateFunctons(oSchema);
            }
            Util.BoxedText("\n", "Checking for SHA256 Plugin based Configuration", "", "*", 130);
            ValidateServerVariables(MyDB, "SHA256PasswordCheck");

            Util.BoxedText("", "Checking for Transparent Data Encryption, TDE", "", "*", 130);
            ValidateServerVariables(MyDB, "EncryptionParametersToCheck");

            Util.BoxedText("", "Checking for Other MySQL Specific System Variables", "", "*", 130);
            ValidateServerVariables(MyDB, "SystemVariablesToCheck");
        } catch (Exception e) {
            System.out.println("Error While Processing");
            new Logger(LogPath + "/Exodus.err", "Error While Processing - " + e.getMessage(), true);
            e.printStackTrace();
        } finally {
            SourceCon.DisconnectDB();
            Util.BoxedText("\n", "Compatibility Check Completed...", "", "#", 130);
        }
    }

    //Validate The Data Types and check against the "DataTypesToCheck"
    private void ValidateDataTypes(SchemaHandler objSchema) {
        boolean bAlert = false;
        for (TableHandler Tab : objSchema.getTables()) {
            for (ColumnHandler Col : Tab.getColumnCollection().getColumnList()) {
                for (String DataType : getListOfValues("DataTypesToCheck")) {
                    if (Col.getDataType().toLowerCase().equals(DataType)) {
                        System.out.println(Tab.getTableName() + "." + Col.getName() + " -> " + Col.getDataType() + "[XX] --> Needs to be altered");
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) {
            System.out.println("No Issues Found...");
        }
    }

    private void ValidateFunctons(SchemaHandler objSchema) {
        boolean bAlert=false;
        System.out.println("- Views -");
        for (ViewHandler View : objSchema.getViewsList()) {
            for (String FunctionName : getListOfValues("FunctionsToCheck")) {
                for (String ViewScript : View.getViewScript()) {
                    if (ViewScript.toLowerCase().contains(FunctionName)) {
                        System.out.println(View.getFullViewName() + " -> [xx] --> This view uses `" + FunctionName + "()` function unsupported by MariaDB!" );
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) {
            System.out.println("No Issues Found...");
        }
        bAlert = false;

        System.out.println("\n- Source Code -");
        for (SourceCodeHandler srcCode : objSchema.getSourceCodeList()) {
            for (String FunctionName : getListOfValues("FunctionsToCheck")) {
                for (String strLine : srcCode.getSourceScript()) {
                    if (strLine.toLowerCase().contains(FunctionName)) {
                        System.out.println(srcCode.getFullObjectName() + " -> [xx] --> This " + srcCode.getSourceType() + " uses `" + FunctionName + "()` function unsupported by MariaDB!" );
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) {
            System.out.println("No Issues Found...");
        }
    }

    private void ValidateServerVariables(MySQLDatabase oMyDB, String sParam) {
        List<String> KeyVal = new ArrayList<String>();
        boolean bAlert=false;

        for (GlobalVariables gVars : oMyDB.getServerVariables()) {
            for (String pVar : getListOfValues(sParam)) {
                String[] tmpArr = pVar.split(":");
                Arrays.parallelSetAll(tmpArr, (i) -> tmpArr[i].trim().toLowerCase());
                //System.out.println(pVar);

                if (tmpArr.length > 0) {
                    KeyVal = Arrays.asList(tmpArr);
                }
                
                if (KeyVal.get(0).toString().equals(gVars.getVariableName().toLowerCase())) {
                    if (!KeyVal.get(1).toString().toLowerCase().equals(gVars.getVariableValue().toString().toLowerCase())) {
                        System.out.println(gVars.getVariableName().toLowerCase() + "(" + gVars.getVariableValue().toString().toLowerCase() + ") Is MySQL specific config & not supported by MariaDB!");
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) {
            System.out.println("No Issues Found...");
        }
    }

    private List<String> getListOfValues(String sParam) {
        String sValues = Util.getPropertyValue(sParam);
        List<String> DataValues = new ArrayList<String>();

        //To Trim all spaces from the Array Read
        String[] tmpArr = sValues.split(",");
        Arrays.parallelSetAll(tmpArr, (i) -> tmpArr[i].trim().toLowerCase());
        if (tmpArr.length > 0) {
            DataValues = Arrays.asList(tmpArr);
        }
        return DataValues;
    }
}
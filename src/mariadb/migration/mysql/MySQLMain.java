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

        Util.BoxedText("\n", "Starting Compatibility Run", "", "=", 130);

        try {
            //Run Validations
            for (SchemaHandler oSchema : MyDB.getSchemaList()) {
                ValidateDataTypes(oSchema);
                ValidateFunctons(oSchema);
            }
            ValidateServerVariables(MyDB);

        } catch (Exception e) {
            System.out.println("Error While Processing");
            new Logger(LogPath + "/Exodus.err", "Error While Processing - " + e.getMessage(), true);
            e.printStackTrace();
        } finally {
            SourceCon.DisconnectDB();
        }
    }

    //Validate The Data Types and check against the "DataTypesToCheck"
    private void ValidateDataTypes(SchemaHandler objSchema) {
        if (objSchema.getTables().size() > 0)
        Util.BoxedText("", "Validating Data Types", "", "*", 130);

        for (TableHandler Tab : objSchema.getTables()) {
            for (ColumnHandler Col : Tab.getColumnCollection().getColumnList()) {
                for (String DataType : getListOfValues("DataTypesToCheck")) {
                    if (Col.getDataType().toLowerCase().equals(DataType)) {
                        System.out.println(Tab.getTableName() + "." + Col.getName() + " -> " + Col.getDataType() + "[XX] --> Needs to be altered") ;
                    }
                }
            }
        }
    }

    private void ValidateFunctons(SchemaHandler objSchema) {
        if (objSchema.getTables().size() > 0)
        Util.BoxedText("\n", "Validating MySQL Functions in Views & Source Code", "", "*", 130);

        System.out.println("- Views -");
        for (ViewHandler View : objSchema.getViewsList()) {
            for (String FunctionName : getListOfValues("FunctionsToCheck")) {
                for (String ViewScript : View.getViewScript()) {
                    if (ViewScript.toLowerCase().contains(FunctionName)) {
                        System.out.println(View.getFullViewName() + " -> [xx] --> This view uses `" + FunctionName + "()` function unsupported by MariaDB!" );
                    }
                }
            }
        }

        System.out.println("\n- Source Code -");
        for (SourceCodeHandler srcCode : objSchema.getSourceCodeList()) {
            for (String FunctionName : getListOfValues("FunctionsToCheck")) {
                for (String strLine : srcCode.getSourceScript()) {
                    if (strLine.toLowerCase().contains(FunctionName)) {
                        System.out.println(srcCode.getFullObjectName() + " -> [xx] --> This " + srcCode.getSourceType() + " uses `" + FunctionName + "()` function unsupported by MariaDB!" );
                    }
                }
            }
        }
    }

    private void ValidateServerVariables(MySQLDatabase oMyDB) {
        List<String> KeyVal = new ArrayList<String>();

        Util.BoxedText("\n", "Validating MySQL Specific Server Variables", "", "*", 130);
        for (GlobalVariables gVars : oMyDB.getServerVariables()) {
            for (String pVar : getListOfValues("SystemVariablesToCheck")) {
                String[] tmpArr = pVar.split(":");
                Arrays.parallelSetAll(tmpArr, (i) -> tmpArr[i].trim().toLowerCase());
                //System.out.println(pVar);

                if (tmpArr.length > 0) {
                    KeyVal = Arrays.asList(tmpArr);
                }
                
                if (KeyVal.get(0).toString().equals(gVars.getVariableName().toLowerCase())) {
                    if (!KeyVal.get(1).toString().toLowerCase().equals(gVars.getVariableValue().toString().toLowerCase())) {
                        System.out.println(gVars.getVariableName().toLowerCase() + "(" + gVars.getVariableValue().toString().toLowerCase() + ") Is MySQL specific variable and does not match the defaults!");
                    }
                }
            }
        }
        Util.BoxedText("\n", "Compatibility Check Completed...", "", "#", 130);
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
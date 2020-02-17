package mariadb.migration.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mariadb.migration.*;

public class MySQLMain {
    public String LogPath = Util.getPropertyValue("LogPath");
    public List<String> StringTerminator;

    
    DBCredentialsReader UCR = new DBCredentialsReader("resources/dbdetails.xml");
    
    //Specific Database Source Execution and/or ALL
    public MySQLMain(String strSourceDB) {
        String tmpStr = Util.getPropertyValue("StringTerminator");
        String[] tmpArr = tmpStr.split(",");
        Arrays.parallelSetAll(tmpArr, (i) -> tmpArr[i].trim().toLowerCase());

        if (tmpArr.length > 0) {
            StringTerminator = Arrays.asList(tmpArr);
        } else {
            StringTerminator = new ArrayList<String>();
        }

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

        Util.BoxedText("\n", "Starting Compatibility Check For Each Schema/Database", "", "=", 130);

        try {
            //Run Validations
            for (SchemaHandler oSchema : MyDB.getSchemaList()) {
                //DataTypesToCheck
                Util.BoxedText("\n", "Checking for Unsupported Data Types in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                ValidateDataTypes(oSchema);

                //FunctionsToCheck
                Util.BoxedText("\n", "Checking for MySQL Specific Functions in Views & Source Code in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "FunctionsToCheck");
                System.out.println();
                CheckInProcedures(oSchema, "FunctionsToCheck");
                System.out.println();
                CheckInTriggers(oSchema, "CheckMaxStatementSourceCode");

                //CheckCreateTableSpace
                Util.BoxedText("\n", "Checking for CREATE TABLESPACVE in Stored Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInProcedures(oSchema, "CheckCreateTableSpace");
                System.out.println();
                CheckInTriggers(oSchema, "CheckMaxStatementSourceCode");

                //CheckKeywords
                Util.BoxedText("\n", "Checking for MariaDB Spcific Keywords used in MySQL Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckKeywords");
                System.out.println();
                CheckInProcedures(oSchema, "CheckKeywords");
                System.out.println();
                CheckInTriggers(oSchema, "CheckMaxStatementSourceCode");

                //CheckJsonOperators
                Util.BoxedText("\n", "Checking for MySQL Spcific JSON Operators `->` & `->>`used in Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckJsonOperators");
                System.out.println();
                CheckInProcedures(oSchema, "CheckJsonOperators");
                System.out.println();
                CheckInTriggers(oSchema, "CheckMaxStatementSourceCode");

                //CheckJSONSearch, this is not a problem but may need further evaluation
                Util.BoxedText("\n", "Checking for JSON_SEARCH() function used in Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckJSONSearch");
                System.out.println();
                CheckInProcedures(oSchema, "CheckJSONSearch");
                System.out.println();
                CheckInTriggers(oSchema, "CheckMaxStatementSourceCode");

                //CheckInnoDBPArtitions, This is not compatible with MariaDB
                Util.BoxedText("\n", "Checking for MySQL Native Partition which is not compatible with MariaDB in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInTableScripts(oSchema, "CheckInnoDBPArtitions");

                //CheckFullTextParserPlugin, This is not compatible with MariaDB
                Util.BoxedText("\n", "Checking FullText Plugins which are not compatible with MariaDB in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInTableScripts(oSchema, "CheckFullTextParserPlugin");
                
                //CheckMaxStatementSourceCode
                Util.BoxedText("\n", "Checking for Hints in SQL statement that are not compatible with MariaDB in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckMaxStatementSourceCode");
                System.out.println();
                CheckInProcedures(oSchema, "CheckMaxStatementSourceCode");
                System.out.println();
                CheckInTriggers(oSchema, "CheckMaxStatementSourceCode");

                //CheckMemCachedPlugin
                Util.BoxedText("\n", "Checking for Tables using MemCache in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckBySQL(SourceCon, oSchema, "CheckMemCachedPlugin");
            }
            Util.BoxedText("\n\n", "Starting Compatibility Check Using Global Scope", "", "=", 130);

            //CheckMaxStatementServerVariables
            Util.BoxedText("", "Checking for Variables Needs to be reviewed as MySQL uses miliseconds vs seconds in MariaDB", "", "*", 130);
            CheckGlobalVariables(MyDB, "CheckMaxStatementServerVariables");

            //SystemVariablesToCheck
            Util.BoxedText("\n", "Checking for Other MySQL Specific System Variables", "", "*", 130);
            CheckGlobalVariables(MyDB, "SystemVariablesToCheck");

            //CheckMySQLXPlugin
            Util.BoxedText("\n", "Checking MySQL X Plugin", "", "*", 130);
            CheckGlobalVariables(MyDB, "CheckMySQLXPlugin");

            //SHA256PasswordCheck
            Util.BoxedText("\n", "Checking for SHA256 Plugin based Configuration", "", "*", 130);
            CheckGlobalVariables(MyDB, "SHA256PasswordCheck");
            
            //EncryptionParametersToCheck
            Util.BoxedText("\n", "Checking for Transparent Data Encryption, TDE", "", "*", 130);
            CheckGlobalVariables(MyDB, "EncryptionParametersToCheck");

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
        if (!bAlert) { System.out.println("No Issues Found..."); }
    }
    
    private void CheckInTableScripts(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;
        String AdditionalCriteria=Util.getPropertyValue(sParam + ".AND").toLowerCase();
        for (TableHandler Tab : objSchema.getTables()) {
            boolean bTableAlert=false;
            for (String TextToSearch : getListOfValues(sParam)) {
                if (Tab.getTableScript().toLowerCase().contains(TextToSearch)) {
                    if (!AdditionalCriteria.isEmpty()) {
                        if (Tab.getTableScript().toLowerCase().contains(AdditionalCriteria)) {
                            bTableAlert = true;
                            bAlert = true;
                        } else {
                            bTableAlert = false;
                        }       
                    } else {
                        bTableAlert = true;
                        bAlert = true;
                    }
                }
                if (bTableAlert) {
                    System.out.println(Tab.getFullTableName() + " -> [xx] --> This " + Tab.getFullTableName() + " uses `" + TextToSearch + "()` function unsupported by MariaDB!" );
                }
            }
        }
        if (!bAlert) { System.out.println("No Issues Found..."); }
    }

    private void CheckInViews(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;
        System.out.println("- Views -");
        for (ViewHandler View : objSchema.getViewsList()) {
            for (String TextToSearch : getListOfValues(sParam)) {
                for (String ViewScript : View.getViewScript()) {
                    if (ViewScript.toLowerCase().contains(TextToSearch)) {
                        System.out.println(View.getFullViewName() + " -> [xx] --> This view uses `" + TextToSearch + "()` function unsupported by MariaDB!" );
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) { System.out.println("No Issues Found..."); }
    }

    private void CheckInProcedures(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;
        System.out.println("- Stored Procedures & Functions -");
        for (SourceCodeHandler srcCode : objSchema.getSourceCodeList()) {
            for (String FunctionName : getListOfValues(sParam)) {
                for (String strLine : srcCode.getSourceScript()) {
                    if (strLine.toLowerCase().contains(FunctionName)) {
                        System.out.println(srcCode.getFullObjectName() + " -> [xx] --> This " + srcCode.getSourceType() + " uses `" + FunctionName + "()` function unsupported by MariaDB!" );
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) { System.out.println("No Issues Found..."); }
    }

    private void CheckInTriggers(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;
        System.out.println("- Triggers -");
        for (TableHandler Tab : objSchema.getTables()) {
            for (String FunctionName : getListOfValues(sParam)) {
                for (String strLine : Tab.getTriggers()) {
                    if (strLine.toLowerCase().contains(FunctionName)) {
                        System.out.println(Tab.getFullTableName() + " -> [xx] --> This Table's Trigger uses `" + FunctionName + "()` function unsupported by MariaDB!");
                        bAlert = true;
                    }
                }
            }
        }
        if (!bAlert) { System.out.println("No Issues Found..."); }
    }

    private void CheckGlobalVariables(MySQLDatabase oMyDB, String sParam) {
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
        if (!bAlert) { System.out.println("No Issues Found..."); }
    }

    private void CheckBySQL(MySQLConnect SourceCon, SchemaHandler objSchema, String sParam) {
		Statement StatementObj = null;
        ResultSet ResultSetObj = null;        
        String SQLScript = Util.getPropertyValue(sParam).replace("?", "'" + objSchema.getSchemaName() + "'");

        boolean bAlert=false;

		try {
			StatementObj = SourceCon.getDBConnection().createStatement();
            ResultSetObj = StatementObj.executeQuery(SQLScript);

            while (ResultSetObj.next()) {
                bAlert=true;
                System.out.println(ResultSetObj.getString("resultSet"));
            }

            if (!bAlert) { System.out.println("No Issues Found..."); }
		} catch (SQLException e) {
			System.out.println("*** Failed to Execute: " + SQLScript);
			e.printStackTrace();
		} finally {
            try {
                if (ResultSetObj != null) { ResultSetObj.close(); }
                if (StatementObj != null) { StatementObj.close(); }
            } catch (Exception ex) {
                ex.printStackTrace();
            } 
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
        int currIndex;
        //Handle the String Terminators
        for (String Terminator : StringTerminator) {
            currIndex=0;
            for (String itemText : DataValues) {
                DataValues.set(currIndex, itemText.replace(Terminator, ""));
                currIndex++;
            }
        }
        return DataValues;
    }
}
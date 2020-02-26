package mariadb.migration.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mariadb.migration.*;

public class MySQLMain {
    public String LogPath = Util.getPropertyValue("LogPath");
    public String ReportPath = Util.getPropertyValue("ReportPath");
    String FolderSeparator = Util.getPropertyValue("FolderSeparator");

    public List<String> StringTerminator;
    String ObjectName, ObjectType, Arg1, Arg2;

    public Logger JSON_Log = new Logger(ReportPath + FolderSeparator + "assessment_report.json");
        
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
                try {
                    StartExodusAssess(DC);
                } catch (Exception ex) {
                    Util.BoxedText("\n\n", "Error while processing database `" + DC.getDBType() + "@" + DC.getHostName() + "`", "\n\n", "!", 130);
                }
            }
        }
        System.out.println("\n\n");
    }

    public void StartExodusAssess(DataSources oDataSource) throws Exception {
        MySQLConnect SourceCon = null;
        MySQLDatabase MyDB = null;
        //Begin the Assessmentv
        try {
            //Initiate a new connection to MySQL server
            SourceCon = new MySQLConnect(oDataSource.getDBType());
            Util.BoxedText("Current Server: " + oDataSource.getHostName(), "=", 130);
    
            //Read The Database structure and objects
            MyDB = new MySQLDatabase(SourceCon.getDBConnection());
            Util.BoxedText("Parsing Completed", "-", 130);

            Util.BoxedText("\n", "Starting Compatibility Check For Each Schema/Database", "", "=", 130);

            //Run Validations
            for (SchemaHandler oSchema : MyDB.getSchemaList()) {
                //DataTypesToCheck
                Util.BoxedText("\n", "Checking for Unsupported Data Types in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                ValidateDataTypes(oSchema, "DataTypesToCheck");

                //FunctionsToCheck
                Util.BoxedText("\n", "Checking for MySQL Specific Functions in Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "FunctionsToCheck");
                System.out.println();
                CheckInProcedures(oSchema, "FunctionsToCheck");
                System.out.println();
                CheckInTriggers(oSchema, "FunctionsToCheck");

                //CheckCreateTableSpace
                Util.BoxedText("\n", "Checking for CREATE TABLESPACVE in Stored Procedures in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInProcedures(oSchema, "CheckCreateTableSpace");
                System.out.println();
                CheckInTriggers(oSchema, "CheckCreateTableSpace");

                //CheckKeywords
                Util.BoxedText("\n", "Checking for MariaDB Spcific Keywords used in MySQL Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckKeywords");
                System.out.println();
                CheckInProcedures(oSchema, "CheckKeywords");
                System.out.println();
                CheckInTriggers(oSchema, "CheckKeywords");

                //CheckJsonOperators
                Util.BoxedText("\n", "Checking for MySQL Spcific JSON Operators `->` & `->>`used in Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckJsonOperators");
                System.out.println();
                CheckInProcedures(oSchema, "CheckJsonOperators");
                System.out.println();
                CheckInTriggers(oSchema, "CheckJsonOperators");

                //CheckJSONSearch, this is not a problem but may need further evaluation
                Util.BoxedText("\n", "Checking for JSON_SEARCH() function used in Views/Procedures/Functions in `" + oSchema.getSchemaName() + "` database", "", "*", 130);
                CheckInViews(oSchema, "CheckJSONSearch");
                System.out.println();
                CheckInProcedures(oSchema, "CheckJSONSearch");
                System.out.println();
                CheckInTriggers(oSchema, "CheckJSONSearch");

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
            throw new Exception("Errors while processing database!");
        } finally {
            SourceCon.DisconnectDB();
            Util.BoxedText("\n", "Compatibility Check Completed...", "", "#", 130);
            System.out.println("\nJSON Report is available in [" + ReportPath + "]");
        }
    }

    //Validate The Data Types and check against the "DataTypesToCheck"
    private JSONObject ValidateDataTypes(SchemaHandler objSchema, String sParam) {
        boolean bAlert = false;
        List<String> KeyVal = new ArrayList<String>();

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();

        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType="Table Column"; Arg1=""; Arg2="";

        for (TableHandler Tab : objSchema.getTables()) {
            for (ColumnHandler Col : Tab.getColumnCollection().getColumnList()) {
                for (String DataType : getListOfValues("DataTypesToCheck")) {
                    String[] tmpArr = DataType.split(":");
                    Arrays.parallelSetAll(tmpArr, (i) -> tmpArr[i].trim().toLowerCase());

                    //Nothing to compare, continue back to the top of the loop
                    if (tmpArr.length <= 0) {
                        continue;
                    }

                    KeyVal = Arrays.asList(tmpArr);

                    ObjectName = Tab.getTableName() + "." + Col.getName();
                    Arg1 = Col.getDataType();
                    Arg2 = KeyVal.get(1);

                    if (Col.getDataType().toLowerCase().equals(KeyVal.get(0))) {
                        JSONObject JsonRow = new JSONObject();
                        MessageText = MessageTemplate.
                                        replace("$OBJECTNAME$", ObjectName).
                                            replace("$OBJECTTYPE$", ObjectType).
                                                replace("$ARG1$", Arg1).
                                                    replace("$ARG2$", Arg2);
                        try {
                            JsonRow.put("ObjectName", ObjectName);
                            JsonRow.put("ObjectType", ObjectType);
                            JsonRow.put("CurrentDataType", Arg1);
                            JsonRow.put("RecommendedDataType", Arg2);
                            JsonRow.put("Message", MessageText);
                            ReportArray.put(JsonRow);
                        } catch (JSONException jex) {
                            System.out.println(jex.getMessage());
                        }

                        System.out.println(MessageText);
                        bAlert = true;
                    }
                }
            }
        }

        try {
            AssessmentReport.put("DatabaseName", objSchema.getSchemaName());
            if (bAlert) {
                if (ReportArray.length() > 0) {
                    AssessmentReport.put("Alert", true);
                    AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                    AssessmentReport.put("Report", ReportArray);
                }
            } else {
                AssessmentReport.put("Alert", false);
                AssessmentReport.put("Summary", "No Issues Found");
                AssessmentReport.put("Report", new JSONArray());
                System.out.println("No Issues Found...");
            }
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return AssessmentReport;
    }

    private JSONObject CheckInTableScripts(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();


        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType=""; Arg1=""; Arg2="";

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

                ObjectName = Tab.getFullTableName();
                ObjectType = "Table";
                Arg1 = TextToSearch;

                if (bTableAlert) {
                    JSONObject JsonRow = new JSONObject();
                    MessageText = MessageTemplate.
                                    replace("$OBJECTNAME$", ObjectName).
                                        replace("$OBJECTTYPE$", ObjectType).
                                            replace("$ARG1$", Arg1).
                                                replace("$ARG2$", Arg2);
                    try {
                        JsonRow.put("ObjectName", ObjectName);
                        JsonRow.put("ObjectType", ObjectType);
                        JsonRow.put("CurrentDataType", Arg1);
                        JsonRow.put("RecommendedDataType", Arg2);
                        JsonRow.put("Message", MessageText);
                        ReportArray.put(JsonRow);
                    } catch (JSONException jex) {
                        System.out.println(jex.getMessage());
                    }
                    System.out.println(MessageText);
                }
            }
        }
        try {
            AssessmentReport.put("DatabaseName", objSchema.getSchemaName());
            if (bAlert) {
                if (ReportArray.length() > 0) {
                    AssessmentReport.put("Alert", true);
                    AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                    AssessmentReport.put("Report", ReportArray);
                }
            } else {
                AssessmentReport.put("Alert", false);
                AssessmentReport.put("Summary", "No Issues Found");
                AssessmentReport.put("Report", new JSONArray());
                System.out.println("No Issues Found...");
            }
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return AssessmentReport;    
    }

    private JSONObject CheckInViews(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();

        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType=""; Arg1=""; Arg2="";

        System.out.println("- Views -");
        for (ViewHandler View : objSchema.getViewsList()) {
            for (String TextToSearch : getListOfValues(sParam)) {
                for (String ViewScript : View.getViewScript()) {
                    if (ViewScript.toLowerCase().contains(TextToSearch)) {
                        ObjectName = View.getFullViewName();
                        ObjectType = "View";
                        Arg1 = TextToSearch;

                        JSONObject JsonRow = new JSONObject();
                        MessageText = MessageTemplate.
                                        replace("$OBJECTNAME$", ObjectName).
                                            replace("$OBJECTTYPE$", ObjectType).
                                                replace("$ARG1$", Arg1).
                                                    replace("$ARG2$", Arg2);
                        try {
                            JsonRow.put("ObjectName", ObjectName);
                            JsonRow.put("ObjectType", ObjectType);
                            JsonRow.put("CurrentDataType", Arg1);
                            JsonRow.put("RecommendedDataType", Arg2);
                            JsonRow.put("Message", MessageText);
                            ReportArray.put(JsonRow);
                        } catch (JSONException jex) {
                            System.out.println(jex.getMessage());
                        }
                        System.out.println(MessageText);
                        bAlert = true;
                    }
                }
            }
        }
        try {
            AssessmentReport.put("DatabaseName", objSchema.getSchemaName());
            if (bAlert) {
                if (ReportArray.length() > 0) {
                    AssessmentReport.put("Alert", true);
                    AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                    AssessmentReport.put("Report", ReportArray);
                }
            } else {
                AssessmentReport.put("Alert", false);
                AssessmentReport.put("Summary", "No Issues Found");
                AssessmentReport.put("Report", new JSONArray());
                System.out.println("No Issues Found...");
            }
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return AssessmentReport;    
    }

    private JSONObject CheckInProcedures(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();

        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType=""; Arg1=""; Arg2="";

        System.out.println("- Stored Procedures & Functions -");
        for (SourceCodeHandler srcCode : objSchema.getSourceCodeList()) {
            for (String FunctionName : getListOfValues(sParam)) {
                for (String strLine : srcCode.getSourceScript()) {
                    if (strLine.toLowerCase().contains(FunctionName)) {
                        ObjectName = srcCode.getFullObjectName();
                        ObjectType = srcCode.getSourceType();
                        Arg1 = FunctionName;

                        JSONObject JsonRow = new JSONObject();
                        //Replace all the tokens in the Message String with values
                        MessageText = MessageTemplate.
                                        replace("$OBJECTNAME$", ObjectName).
                                            replace("$OBJECTTYPE$", ObjectType).
                                                replace("$ARG1$", Arg1).
                                                    replace("$ARG2$", Arg2);
                        try {
                            JsonRow.put("ObjectName", ObjectName);
                            JsonRow.put("ObjectType", ObjectType);
                            JsonRow.put("CurrentDataType", Arg1);
                            JsonRow.put("RecommendedDataType", Arg2);
                            JsonRow.put("Message", MessageText);
                            ReportArray.put(JsonRow);
                        } catch (JSONException jex) {
                            System.out.println(jex.getMessage());
                        }
                        System.out.println(MessageText);
                        bAlert = true;
                    }
                }
            }
        }
        try {
            AssessmentReport.put("DatabaseName", objSchema.getSchemaName());
            if (bAlert) {
                if (ReportArray.length() > 0) {
                    AssessmentReport.put("Alert", true);
                    AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                    AssessmentReport.put("Report", ReportArray);
                }
            } else {
                AssessmentReport.put("Alert", false);
                AssessmentReport.put("Summary", "No Issues Found");
                AssessmentReport.put("Report", new JSONArray());
                System.out.println("No Issues Found...");
            }
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return AssessmentReport;    
    }

    private JSONObject CheckInTriggers(SchemaHandler objSchema, String sParam) {
        boolean bAlert=false;

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();

        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType=""; Arg1=""; Arg2="";

        System.out.println("- Triggers -");
        for (TableHandler Tab : objSchema.getTables()) {
            for (String FunctionName : getListOfValues(sParam)) {
                for (String strLine : Tab.getTriggers()) {
                    if (strLine.toLowerCase().contains(FunctionName)) {
                        ObjectName = Tab.getFullTableName();
                        ObjectType = "Table.Trigger";
                        Arg1 = FunctionName;

                        JSONObject JsonRow = new JSONObject();
                        //Replace all the tokens in the Message String with values
                        MessageText = MessageTemplate.
                                        replace("$OBJECTNAME$", ObjectName).
                                            replace("$OBJECTTYPE$", ObjectType).
                                                replace("$ARG1$", Arg1).
                                                    replace("$ARG2$", Arg2);
                        try {
                            JsonRow.put("ObjectName", ObjectName);
                            JsonRow.put("ObjectType", ObjectType);
                            JsonRow.put("CurrentDataType", Arg1);
                            JsonRow.put("RecommendedDataType", Arg2);
                            JsonRow.put("Message", MessageText);
                            ReportArray.put(JsonRow);
                        } catch (JSONException jex) {
                            System.out.println(jex.getMessage());
                        }
                        System.out.println(MessageText);
                        bAlert = true;
                    }
                }
            }
        }
        try {
            AssessmentReport.put("DatabaseName", objSchema.getSchemaName());
            if (bAlert) {
                if (ReportArray.length() > 0) {
                    AssessmentReport.put("Alert", true);
                    AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                    AssessmentReport.put("Report", ReportArray);
                }
            } else {
                AssessmentReport.put("Alert", false);
                AssessmentReport.put("Summary", "No Issues Found");
                AssessmentReport.put("Report", new JSONArray());
                System.out.println("No Issues Found...");
            }
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return AssessmentReport;    
    }

    private JSONObject CheckGlobalVariables(MySQLDatabase oMyDB, String sParam) {
        List<String> KeyVal = new ArrayList<String>();
        boolean bAlert=false;

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();

        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType=""; Arg1=""; Arg2="";


        for (GlobalVariables gVars : oMyDB.getServerVariables()) {
            for (String pVar : getListOfValues(sParam)) {
                String[] tmpArr = pVar.split(":");
                Arrays.parallelSetAll(tmpArr, (i) -> tmpArr[i].trim().toLowerCase());

                //Nothing to compare, continue back to the top of the loop
                if (tmpArr.length <= 0) {
                    continue;
                }

                KeyVal = Arrays.asList(tmpArr);

                if (KeyVal.get(0).toString().equals(gVars.getVariableName().toLowerCase())) {
                    if (!KeyVal.get(1).toString().toLowerCase().equals(gVars.getVariableValue().toString().toLowerCase())) {

                        ObjectType = "Server Variable";
                        Arg1 = gVars.getVariableName().toLowerCase();
                        Arg2 = gVars.getVariableValue().toString().toLowerCase();

                        JSONObject JsonRow = new JSONObject();
                        //Replace all the tokens in the Message String with values
                        MessageText = MessageTemplate.
                                        replace("$OBJECTNAME$", ObjectName).
                                            replace("$OBJECTTYPE$", ObjectType).
                                                replace("$ARG1$", Arg1).
                                                    replace("$ARG2$", Arg2);
                        try {
                            JsonRow.put("ObjectName", ObjectName);
                            JsonRow.put("ObjectType", ObjectType);
                            JsonRow.put("CurrentDataType", Arg1);
                            JsonRow.put("RecommendedDataType", Arg2);
                            JsonRow.put("Message", MessageText);
                            ReportArray.put(JsonRow);
                        } catch (JSONException jex) {
                            System.out.println(jex.getMessage());
                        }
                        System.out.println(MessageText);
                        bAlert = true;
                    }
                }
            }
        }
        try {
            AssessmentReport.put("DatabaseName", "NULL");
            if (bAlert) {
                if (ReportArray.length() > 0) {
                    AssessmentReport.put("Alert", true);
                    AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                    AssessmentReport.put("Report", ReportArray);
                }
            } else {
                AssessmentReport.put("Alert", false);
                AssessmentReport.put("Summary", "No Issues Found");
                AssessmentReport.put("Report", new JSONArray());
                System.out.println("No Issues Found...");
            }
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return AssessmentReport;    
    }

    private JSONObject CheckBySQL(MySQLConnect SourceCon, SchemaHandler objSchema, String sParam) {
        Statement StatementObj = null;
        ResultSet ResultSetObj = null;        

        JSONArray ReportArray = new JSONArray();
        //Objects to store the JSON Report
        JSONObject AssessmentReport = new JSONObject();

        String SQLScript = Util.getPropertyValue(sParam).replace("?", "'" + objSchema.getSchemaName() + "'");

        //Get the related message text for this validation
        String MessageText = "";
        String MessageTemplate = Util.getPropertyValue(sParam + ".Message");

        //Reset Tokens
        ObjectName=""; ObjectType=""; Arg1=""; Arg2="";
        
        boolean bAlert=false;
        
        try {
            StatementObj = SourceCon.getDBConnection().createStatement();
            ResultSetObj = StatementObj.executeQuery(SQLScript);

            while (ResultSetObj.next()) {
                bAlert=true;
                ObjectName = ResultSetObj.getString("resultSet");
                ObjectType = "Table";

                JSONObject JsonRow = new JSONObject();
                //Replace all the tokens in the Message String with values
                MessageText = MessageTemplate.
                                replace("$OBJECTNAME$", ObjectName).
                                    replace("$OBJECTTYPE$", ObjectType).
                                        replace("$ARG1$", Arg1).
                                            replace("$ARG2$", Arg2);
                try {
                    JsonRow.put("ObjectName", ObjectName);
                    JsonRow.put("ObjectType", ObjectType);
                    JsonRow.put("CurrentDataType", Arg1);
                    JsonRow.put("RecommendedDataType", Arg2);
                    JsonRow.put("Message", MessageText);
                    ReportArray.put(JsonRow);
                } catch (JSONException jex) {
                    System.out.println(jex.getMessage());
                }
                System.out.println(MessageText);
            }

            try {
                AssessmentReport.put("DatabaseName", "NULL");
                if (bAlert) {
                    if (ReportArray.length() > 0) {
                        AssessmentReport.put("Alert", true);
                        AssessmentReport.put("Summary", "Potential Compatibility Issues Found, review the Assessment Report");
                        AssessmentReport.put("Report", ReportArray);
                    }
                } else {
                    AssessmentReport.put("Alert", false);
                    AssessmentReport.put("Summary", "No Issues Found");
                    AssessmentReport.put("Report", new JSONArray());
                    System.out.println("No Issues Found...");
                }
            } catch (JSONException jex) {
                System.out.println(jex.getMessage());
            }
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
        return AssessmentReport;
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
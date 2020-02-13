package mariadb.migration.mysql;

public class GlobalVariables {
    private String VariableName;
    private String VariableValue;

    public GlobalVariables() {
        VariableName = "";
        VariableValue = "";
    }

    public String getVariableName() { return VariableName; }
    public String getVariableValue() { return VariableValue; }
    public void setVariableName(String iVariableName) { VariableName = iVariableName; }    
    public void setVariableValue(String iVariableValue) { VariableValue = iVariableValue; }    
}

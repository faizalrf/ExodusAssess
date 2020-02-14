package mariadb.migration;

//Exodus Unified Main Code
public class ExodusAssess {
    public static void main(String[] args) {
        String CommandLineParam = "mysql", SourceDB = "ALL";
        System.out.println("\n");

        if (args.length == 1) {
            SourceDB = args[0].toLowerCase();
        } else {
            System.out.println("Invalid arguments...");
        }

        System.out.println("Assessment Path: " + SourceDB + "\n");

        switch (CommandLineParam) {
            case "mysql":
                new mariadb.migration.mysql.MySQLMain(SourceDB);
                break;
            case "db2":
                System.out.println("\nExodus " + args[0] + " not ready yet!!!\n\n");
                break;
            case "oracle":
                System.out.println("\nExodus " + args[0] + " not ready yet!!!\n\n");
                break;
            default:
                System.out.println("\nUnknown Source Database!\nValid options: MySQL, DB2, ORACLE\n\n");
                break;                                                                                                                                            
        }
    }
}

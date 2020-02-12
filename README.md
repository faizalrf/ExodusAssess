# Exodus Assessment

Migration Assessment Tool for MySQL 5.7 to MariaDB 10.3+ Migration

The executable script is the Java class file `ExodusAssess.class` which takes one commandline argument.

- Source Database Name
  - This parameter links up with `DataProvider type` of the `resources/dbdetail.xml` 
  - If the specific database name is passed, that database configuration is read
  - alternatively **ALL** can be passed as an argument, which will force this tool to load all the configurations, this is great to run assessment on multiple servers in one go

The `resources/dbdetail.xml` has the following contents, this sample has three database servers defined with their user accounts and passwords. 

```
<?xml version="1.1"?>
<DataSources>
	<DataProvider type="DataBase1">
		<UserName>migration</UserName>
		<Password>password</Password>
		<HostName>192.168.56.101</HostName>
		<PortNumber>3306</PortNumber>
		<DatabaseName>mysql</DatabaseName>
	</DataProvider>
	<DataProvider type="DataBase2">
		<UserName>migration</UserName>
		<Password>password</Password>
		<HostName>192.168.56.102</HostName>
		<PortNumber>3306</PortNumber>
		<DatabaseName>mysql</DatabaseName>
	</DataProvider>
	<DataProvider type="DataBase3">
		<UserName>migration</UserName>
		<Password>password</Password>
		<HostName>192.168.56.103</HostName>
		<PortNumber>3306</PortNumber>
		<DatabaseName>mysql</DatabaseName>
	</DataProvider>
</DataSources>
```

## Running the Assessment

The following sample output passes `database3` as the argument which will read the configuration for mysql running on `192.168.56.103` 

```
shell> java ExodusAssess database3


███╗   ███╗ █████╗ ██████╗ ██╗ █████╗ ██████╗ ██████╗     ███████╗██╗  ██╗ ██████╗ ██████╗ ██╗   ██╗███████╗
████╗ ████║██╔══██╗██╔══██╗██║██╔══██╗██╔══██╗██╔══██╗    ██╔════╝╚██╗██╔╝██╔═══██╗██╔══██╗██║   ██║██╔════╝
██╔████╔██║███████║██████╔╝██║███████║██║  ██║██████╔╝    █████╗   ╚███╔╝ ██║   ██║██║  ██║██║   ██║███████╗
██║╚██╔╝██║██╔══██║██╔══██╗██║██╔══██║██║  ██║██╔══██╗    ██╔══╝   ██╔██╗ ██║   ██║██║  ██║██║   ██║╚════██║
██║ ╚═╝ ██║██║  ██║██║  ██║██║██║  ██║██████╔╝██████╔╝    ███████╗██╔╝ ██╗╚██████╔╝██████╔╝╚██████╔╝███████║
╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═════╝ ╚═════╝     ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚══════╝


Assessment Path: database3

Starting MySQL Assessment...

==================================================================================================================================
= Current Server: 192.168.56.103                                                                                                 =
==================================================================================================================================

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Starting `information_schema` DB Schema Discovery                                                                              ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The Database is Empty!

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Starting `world` DB Schema Discovery                                                                                           ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Parsing `world`.`City`                                                               --> COLUMNS [  5] -->  ROWS [          4,079]
Parsing `world`.`Country`                                                            --> COLUMNS [ 15] -->  ROWS [            239]
Parsing `world`.`CountryLanguage`                                                    --> COLUMNS [  4] -->  ROWS [            984]
Parsing `world`.`j_data`                                                             --> COLUMNS [  3] -->  ROWS [              2]
Reading View Script `world`.`v_dummy`                                                --> [ OK ]
Reading View Script `world`.`v_last_city`                                            --> [ OK ]
Reading Stored Procedure Script `world`.`sp_test`                                    --> [ OK ]
Reading Stored Function Script `world`.`fn_max_value`                                --> [ OK ]

----------------------------------------------------------------------------------------------------------------------------------
- Parsing Completed                                                                                                              -
----------------------------------------------------------------------------------------------------------------------------------

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Validating Views/Stored Procedures/Functions for MySQL Specific Functions                                                      ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
...
...
...

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Validating MySQL Specific System Variables with Non-Default values                                                             ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
...
...
...

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Validating MySQL Specific Plugins                                                                                              ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
...
...
...

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Validating Additional Incompatibilities                                                                                        ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
...
...
...

----------------------------------------------------------------------------------------------------------------------------------
- Validation Completed, review the reports                                                                                       -
----------------------------------------------------------------------------------------------------------------------------------
```

## Additional Configuration

The `resources/Exodus.properties` containt additional configurations for.

```bash
##############################################
# MariaDB Migration Assessment Configuration.
# Author: Faisal
# 01-Jan-2020
##############################################

#Connection Additional Parameters
SourceConnectParams=useUnicode=yes&characterEncoding=utf8&rewriteBatchedStatements=true&useSSL=false
SourceConnectPrefix=jdbc:mariadb://
#useUnicode=yes&characterEncoding=utf8&useSSL=false

#Paths with reference to the current folder. Do not use "/" at the end of the path
LogPath=/../ExodusAssess/logs
DDLPath=/../ExodusAssess/ddl
ReportPath=/../ExodusAssess/report

#Database User's to Migrate
UsersToMigrate=USER NOT LIKE 'mysql%' AND USER NOT LIKE 'root%'

#Databases to Migrate, Comma separated String values, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables
DatabaseToMigrate=SCHEMA_NAME NOT IN ('mysql', 'sys', 'performance_schema', 'information_schema')

#Criteria to be added to the SELECT QUERY to filter down the tables for migration, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables
TablesToMigrate=TABLE_NAME LIKE '%'

#These Tables will be Skipped from Migration, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables.
#The following Names should always be there, any additional tables, just add on to the list or use "AND additional expression"
SkipTableMigration=TABLE_NAME IN ('MigrationLog', 'MigrationLogDETAIL')

#Functions to search for in the Source Code and Views
FunctionsToCheck=GTID_SUBSET(), GTID_SUBTRACT(), WAIT_FOR_EXECUTED_GTID_SET(), WAIT_UNTIL_SQL_THREAD_AFTER_GTIDS(), DISTANCE(), MBRCOVEREDBY(), ST_BUFFER_STRATEGY(), ST_DISTANCE_SPHERE(), ST_GeoHash(), ST_IsValid(), ST_LatFromGeoHash(), ST_LongFromGeoHash(), ST_PointFromGeoHash(), ST_SIMPLIFY(), ST_VALIDATE(), RANDOM_BYTES(), RELEASE_ALL_LOCKS(), VALIDATE_PASSWORD_STRENGTH()

SystemVariablesToCheck=avoid_temporal_upgrade|OFF, binlog_error_action|ABORT_SERVER, etc...

PluginsToCheck=SHA256, ..., etc.
```

This tool assumes that the source database is MySQL 5.7 and uses MariaDB Java Connector to connect to MySQL.

The following are the parameters from the `Exodus.properties` file

- `SourceConnectParams`
  - Additional Connection string parameters like default encoding etc.
- `LogPath`
  - The folder for tool logging
- `ReportPath`
  - Path for the generated report
  - Each report name will be prefix by the database configuration name from `dbdetails.xml` file.
- `UsersToMigrate`
  - Takes a standard SQL syntax to include or exclude users from the processing list
- `DatabaseToMigrate`
  - Takes a standard SQL syntax to include or exclude the databases from the list of assessed databases
- `TablesToMigrate`
  - Takes a standard SQL syntax to include the tables to assess across all databases
- `SkipTableMigration`
  - Takes a standard SQL syntax to define the tables to skip from the assessment across all databases
- `FunctionsToCheck`
  - Functions that are native to MySQL 5.7 will be checked if present in the View's definition or the Stored Procedures/Functions
  - Any other source code like shell scripts or front-end application code will be out of scope for this tool
- `SystemVariablesToCheck`
  - Verify all the MySQL 5.7 specific system variables for "non-default" values setup and highlight the ones that are configured.
- `PluginsToCheck`
  - Additional incompatible plugins to check if existing in the server which are not supported by MariaDB.

Other incompatibilities will be validated like JSON columns, additional plugins that are not compatible etc.

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

==================================================================================================================================
= Current Server: 192.168.56.103                                                                                                 =
==================================================================================================================================

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Starting `world` DB Schema Discovery                                                                                           ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Parsing `world`.`City`                                                               --> COLUMNS [  5] -->  ROWS [          4,079]
Parsing `world`.`Country`                                                            --> COLUMNS [ 15] -->  ROWS [            239]
Parsing `world`.`CountryLanguage`                                                    --> COLUMNS [  4] -->  ROWS [            984]
Parsing `world`.`j_data`                                                             --> COLUMNS [  3] -->  ROWS [              2]
Parsing `world`.`posts`                                                              --> COLUMNS [  3] -->  ROWS [              0]
Reading View Script `world`.`v_dummy`                                                --> [ OK ]
Reading View Script `world`.`v_test`                                                 --> [ OK ]
Reading PROCEDURE Script `world`.`my_procedure_Local_Variables`                      --> [ OK ]

----------------------------------------------------------------------------------------------------------------------------------
- Parsing Completed                                                                                                              -
----------------------------------------------------------------------------------------------------------------------------------


==================================================================================================================================
= Starting Compatibility Check                                                                                                   =
==================================================================================================================================

**********************************************************************************************************************************
* Checking for Unsupported Data Types                                                                                            *
**********************************************************************************************************************************
j_data.c1 -> json[XX] --> Needs to be altered
j_data.c2 -> text[XX] --> Needs to be altered
posts.body -> text[XX] --> Needs to be altered


**********************************************************************************************************************************
* Checking for MySQL Specific Functions in Views & Source Code                                                                   *
**********************************************************************************************************************************
- Views -
`world`.`v_dummy` -> [xx] --> This view uses `wait_for_executed_gtid_set()` function unsupported by MariaDB!
`world`.`v_test` -> [xx] --> This view uses `wait_for_executed_gtid_set()` function unsupported by MariaDB!
`world`.`v_test` -> [xx] --> This view uses `distance()` function unsupported by MariaDB!
`world`.`v_test` -> [xx] --> This view uses `st_distance()` function unsupported by MariaDB!

- Source Code -
`world`.`my_procedure_Local_Variables` -> [xx] --> This PROCEDURE uses `gtid_subtract()` function unsupported by MariaDB!
`world`.`my_procedure_Local_Variables` -> [xx] --> This PROCEDURE uses `distance()` function unsupported by MariaDB!
`world`.`my_procedure_Local_Variables` -> [xx] --> This PROCEDURE uses `st_distance()` function unsupported by MariaDB!


**********************************************************************************************************************************
* Checking for SHA256 Plugin based Configuration                                                                                 *
**********************************************************************************************************************************
default_authentication_plugin(sha256_password) Is MySQL specific config & not supported by MariaDB!

**********************************************************************************************************************************
* Checking for Transparent Data Encryption, TDE                                                                                  *
**********************************************************************************************************************************
No Issues Found...

**********************************************************************************************************************************
* Checking for Other MySQL Specific System Variables                                                                             *
**********************************************************************************************************************************
No Issues Found...


##################################################################################################################################
# Compatibility Check Completed...                                                                                               #
##################################################################################################################################
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
LogPath=/../ExodusAssess/bin/logs
ReportPath=/../ExodusAssess/bin/export

#Database User's to Migrate
UsersToMigrate=USER NOT LIKE 'mysql%' AND USER NOT LIKE 'root%'

#Databases to Migrate, Comma separated String values, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables
DatabaseToMigrate=SCHEMA_NAME NOT IN ('mysql', 'sys', 'performance_schema', 'information_schema')

#Criteria to be added to the SELECT QUERY to filter down the tables for migration, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables
TablesToMigrate=TABLE_NAME LIKE '%'

#These Tables will be Skipped from Migration, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables.
#The following Names should always be there, any additional tables, just add on to the list or use "AND additional expression"
SkipTableMigration=TABLE_NAME IN ('MigrationLog', 'MigrationLogDETAIL')

#Data Types to verify if exists in the MySQL Tables
DataTypesToCheck=JSON

#Functions to search for in the Source Code and Views
FunctionsToCheck=OVER, ROWS, RECURSIVE, ->, ->>, GTID_SUBSET, GTID_SUBTRACT, WAIT_FOR_EXECUTED_GTID_SET, WAIT_UNTIL_SQL_THREAD_AFTER_GTIDS, DISTANCE, MBRCOVEREDBY, ST_BUFFER_STRATEGY, ST_DISTANCE_SPHERE, ST_DISTANCE, ST_GeoHash, ST_IsValid, ST_LatFromGeoHash, ST_LongFromGeoHash, ST_PointFromGeoHash, ST_SIMPLIFY, ST_VALIDATE, RANDOM_BYTES, RELEASE_ALL_LOCKS, VALIDATE_PASSWORD_STRENGTH

#System Variables to search for in the Server Config
SystemVariablesToCheck=avoid_temporal_upgrade:OFF, binlog_error_action:ABORT_SERVER, binlog_group_commit_sync_delay:0, binlog_group_commit_sync_no_delay_count:0, binlog_gtid_simple_recovery:ON, binlog_max_flush_queue_time:0, binlog_order_commits:ON, binlog_rows_query_log_events:OFF, block_encryption_mode:aes-128-ecb, check_proxy_users:OFF, default_password_lifetime:0, disconnect_on_expired_password:ON, end_markers_in_json:OFF, enforce_gtid_consistency:OFF, gtid_executed:*, gtid_next:*, gtid_purged:*, internal_tmp_disk_storage_engine:INNODB, log_bin_use_v1_row_events:OFF, log_builtin_as_identified_by_password:OFF, log_error_verbosity:3, log_statements_unsafe_for_binlog:ON, log_throttle_queries_not_using_indexes:0, master_info_repository:FILE, max_execution_time:0, mysql_native_password_proxy_users:OFF, ngram_token_size:2, offline_mode:OFF, rbr_exec_mode:STRICT, relay_log_info_repository:FILE, require_secure_transport:OFF, rpl_stop_slave_timeout:31536000, server_id_bits:32, session_track_gtids:OFF, sha256_password_proxy_users:OFF, show_compatibility_56:OFF, show_old_temporals:OFF, slave_allow_batching:OFF, slave_checkpoint_group:512, slave_checkpoint_period:300, slave_parallel_type:DATABASE, slave_pending_jobs_size_max:16777216, slave_preserve_commit_order:OFF, super_read_only:OFF, transaction_allow_batching:OFF, transaction_write_set_extraction:OFF

#SHA256 Password Plugin
SHA256PasswordCheck=default_authentication_plugin:mysql_native_password

#InnoDB Encryption Check
EncryptionParametersToCheck=keyring_file_data:data, keyring_encrypted_file_data:data
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
- `DataTypesToCheck`
  - This parameters takes a coma separated list of data types that are not supported by MySQL
    - Currently only `JSON` is defined as the not supported data type which needs to be converted to LONGTEXT before migration. 
- `FunctionsToCheck`
  - This parameter takes a coma separated list of Functions that are native to MySQL 5.7 will be checked if present in the View's definition or the Stored Procedures/Functions
  - Any other source code like shell scripts or front-end application code will be out of scope for this tool
  - This list also includes keywords like OVER, ROWS etc or special operators like MySQL JSON ->  or -->
- `SystemVariablesToCheck`
  - This parameter takes a coma separated list of key/value pairs separated by coma and then colon ":"
  - Configuration in this file is variableName:defaultValue
    - This is will validated against the MySQL server to confirm that the parameter has not been configured differently which may not work in MariaDB
    - Such variables must be removed from the server.cnf/my.cnf file before migrating to MariaDB
- `SHA256PasswordCheck`
  - To validate the SHA256 User Authentication plugin.
    - This is authenticated by vefirying that the value of `default_authentication_plugin` is nolonger `mysql_native_password` Which means that the users created using this new authentication will not work when Migrated to MariaDB
- `EncryptionParametersToCheck`
  - This will verify if Encryption specific parameters exists in the Server config.
    - `keyring_file_data` and `keyring_encrypted_file_data` variables are checked to determine if Encryption is in in use. The InnpDB Encryption from MySQL does not work with MariaDB 

Additional compatibility checks will be added here
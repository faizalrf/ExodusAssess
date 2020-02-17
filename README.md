# Exodus Assessment

Migration Assessment Tool for MySQL 5.7 to MariaDB 10.3+ Migration

The executable script is the Java class file `ExodusAssess.class` which takes one commandline argument.

- Source Database Name
  - This parameter links up with `DataProvider ServerName` of the `resources/dbdetails.xml` 
  - If the specific database name is passed, that database configuration is read
  - alternatively **ALL** can be passed as an argument, which will force this tool to load all the configurations, this is great to run assessment on multiple servers in one go

The `resources/dbdetails.xml` has the following contents, this sample has three database servers defined with their user accounts and passwords. 

**Note:** _The accounts configured in the XML file must have "ALL" privilege as it needs to scan all the tables/objects including global variables/schema spacific objetcs and execute "SHOW" commands for various objects_

```
<?xml version="1.1"?>
<DataSources>
	<DataProvider ServerName="DataBase1">
		<UserName>migration</UserName>
		<Password>password</Password>
		<HostName>192.168.56.101</HostName>
		<PortNumber>3306</PortNumber>
		<DatabaseName>mysql</DatabaseName>
	</DataProvider>
	<DataProvider ServerName="DataBase2">
		<UserName>migration</UserName>
		<Password>password</Password>
		<HostName>192.168.56.102</HostName>
		<PortNumber>3306</PortNumber>
		<DatabaseName>mysql</DatabaseName>
	</DataProvider>
	<DataProvider ServerName="DataBase3">
		<UserName>migration</UserName>
		<Password>password</Password>
		<HostName>192.168.56.103</HostName>
		<PortNumber>3306</PortNumber>
		<DatabaseName>mysql</DatabaseName>
	</DataProvider>
</DataSources>
```

## Running the Assessment

The following sample output using `database3` as the argument which will read the configuration for mysql running on `192.168.56.103`, if `ALL` is passed as the argument instead, the tool will scan through all the databases configured in the `resources/dbdetails.xml` file. 

```
shell> java ExodusAssess database3


Assessment Path: database3

==================================================================================================================================
= Current Server: 192.168.56.103                                                                                                 =
==================================================================================================================================

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Starting `test` DB Schema Discovery                                                                                            ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Parsing `test`.`demo_test`                                                           --> COLUMNS [  5] -->  ROWS [              1]

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~ Starting `world` DB Schema Discovery                                                                                           ~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Parsing `world`.`articles`                                                           --> COLUMNS [  3] -->  ROWS [              0]
Parsing `world`.`City`                                                               --> COLUMNS [  5] -->  ROWS [          4,079]
Parsing `world`.`Country`                                                            --> COLUMNS [ 15] -->  ROWS [            239]
Parsing `world`.`CountryLanguage`                                                    --> COLUMNS [  4] -->  ROWS [            984]
Parsing `world`.`details`                                                            --> COLUMNS [  3] -->  ROWS [              0]
Parsing `world`.`j_data`                                                             --> COLUMNS [  3] -->  ROWS [              2]
Parsing `world`.`posts`                                                              --> COLUMNS [  3] -->  ROWS [              0]
Parsing `world`.`t`                                                                  --> COLUMNS [  2] -->  ROWS [              0]
Reading View Script `world`.`v_dummy`                                                --> [ OK ]
Reading View Script `world`.`v_test`                                                 --> [ OK ]
Reading PROCEDURE Script `world`.`my_procedure_Local_Variables`                      --> [ OK ]

----------------------------------------------------------------------------------------------------------------------------------
- Parsing Completed                                                                                                              -
----------------------------------------------------------------------------------------------------------------------------------


==================================================================================================================================
= Starting Compatibility Check For Each Schema/Database                                                                          =
==================================================================================================================================

**********************************************************************************************************************************
* Checking for Unsupported Data Types in `test` database                                                                         *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking for MySQL Specific Functions in Views & Source Code in `test` database                                                *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for CREATE TABLESPACVE in Stored Procedures/Functions in `test` database                                              *
**********************************************************************************************************************************
- Stored Procedures / Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MariaDB Spcific Keywords used in MySQL Views/Procedures/Functions in `test` database                              *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MySQL Specific Functions in Views & Source Code in `test` database                                                *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for CREATE TABLESPACVE in Stored Procedures/Functions in `test` database                                              *
**********************************************************************************************************************************
- Stored Procedures / Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MariaDB Spcific Keywords used in MySQL Views/Procedures/Functions in `test` database                              *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MySQL Spcific JSON Operators `->` & `->>`used in Views/Procedures/Functions in `test` database                    *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for JSON_SEARCH() function used in Views/Procedures/Functions in `test` database                                      *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MySQL Native Partition which is not compatible with MariaDB in `test` database                                    *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking FullText Plugins which are not compatible with MariaDB in `test` database                                             *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking for Hints in SQL statement that are not compatible with MariaDB in `test` database                                    *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...



**********************************************************************************************************************************
* Checking for Tables using MemCache in `test` database                                                                          *
**********************************************************************************************************************************
`test`.`demo_test`


**********************************************************************************************************************************
* Checking for Unsupported Data Types in `world` database                                                                        *
**********************************************************************************************************************************
j_data.c1 -> json[XX] --> Needs to be altered


**********************************************************************************************************************************
* Checking for MySQL Specific Functions in Views & Source Code in `world` database                                               *
**********************************************************************************************************************************
- Views -
`world`.`v_dummy` -> [xx] --> This view uses `wait_for_executed_gtid_set()` function unsupported by MariaDB!
`world`.`v_test` -> [xx] --> This view uses `wait_for_executed_gtid_set()` function unsupported by MariaDB!
`world`.`v_test` -> [xx] --> This view uses `distance()` function unsupported by MariaDB!
`world`.`v_test` -> [xx] --> This view uses `st_distance()` function unsupported by MariaDB!
- Stored Procedures / Functions -
`world`.`my_procedure_Local_Variables` -> [xx] --> This PROCEDURE uses `gtid_subtract()` function unsupported by MariaDB!
`world`.`my_procedure_Local_Variables` -> [xx] --> This PROCEDURE uses `distance()` function unsupported by MariaDB!
`world`.`my_procedure_Local_Variables` -> [xx] --> This PROCEDURE uses `st_distance()` function unsupported by MariaDB!


**********************************************************************************************************************************
* Checking for CREATE TABLESPACVE in Stored Procedures/Functions in `world` database                                             *
**********************************************************************************************************************************
- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MariaDB Spcific Keywords used in MySQL Views/Procedures/Functions in `world` database                             *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MySQL Spcific JSON Operators `->` & `->>`used in Views/Procedures/Functions in `world` database                   *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for JSON_SEARCH() function used in Views/Procedures/Functions in `world` database                                     *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...


**********************************************************************************************************************************
* Checking for MySQL Native Partition which is not compatible with MariaDB in `world` database                                   *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking FullText Plugins which are not compatible with MariaDB in `world` database                                            *
**********************************************************************************************************************************
`world`.`articles` -> [xx] --> This `world`.`articles` uses `with parser `mecab`()` function unsupported by MariaDB!
`world`.`details` -> [xx] --> This `world`.`details` uses `with parser `mecab`()` function unsupported by MariaDB!
`world`.`posts` -> [xx] --> This `world`.`posts` uses `with parser `ngram`()` function unsupported by MariaDB!
`world`.`posts` -> [xx] --> This `world`.`posts` uses `with parser `mecab`()` function unsupported by MariaDB!


**********************************************************************************************************************************
* Checking for Hints in SQL statement that are not compatible with MariaDB in `world` database                                   *
**********************************************************************************************************************************
- Views -
No Issues Found...

- Stored Procedures & Functions -
No Issues Found...

- Triggers -
No Issues Found...



**********************************************************************************************************************************
* Checking for Tables using MemCache in `world` database                                                                         *
**********************************************************************************************************************************
No Issues Found...


==================================================================================================================================
= Starting Compatibility Check Using Global Scope                                                                                =
==================================================================================================================================

**********************************************************************************************************************************
* Checking for Variables Needs to be reviewed as MySQL uses miliseconds vs seconds in MariaDB                                    *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking for Other MySQL Specific System Variables                                                                             *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking MySQL X Plugin                                                                                                        *
**********************************************************************************************************************************
No Issues Found...


**********************************************************************************************************************************
* Checking for SHA256 Plugin based Configuration                                                                                 *
**********************************************************************************************************************************
default_authentication_plugin(sha256_password) Is MySQL specific config & not supported by MariaDB!


**********************************************************************************************************************************
* Checking for Transparent Data Encryption, TDE                                                                                  *
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

#String Terminator Character
StringTerminator=]

#These Tables will be Skipped from Migration, use SQL WHERE clause compatible with the Source DB Data Dictionary Tables.
#The following Names should always be there, any additional tables, just add on to the list or use "AND additional expression"
SkipTableMigration=TABLE_NAME IN ('MigrationLog', 'MigrationLogDETAIL')

#Data Types to verify if exists in the MySQL Tables
DataTypesToCheck=JSON

#Functions to search for in the Source Code and Views
FunctionsToCheck=GTID_SUBSET, GTID_SUBTRACT, WAIT_FOR_EXECUTED_GTID_SET, WAIT_UNTIL_SQL_THREAD_AFTER_GTIDS, DISTANCE, MBRCOVEREDBY, ST_BUFFER_STRATEGY, ST_DISTANCE_SPHERE, ST_DISTANCE, ST_GeoHash, ST_IsValid, ST_LatFromGeoHash, ST_LongFromGeoHash, ST_PointFromGeoHash, ST_SIMPLIFY, ST_VALIDATE, RANDOM_BYTES, RELEASE_ALL_LOCKS, VALIDATE_PASSWORD_STRENGTH, VALUES ]

#System Variables to search for in the Server Config
SystemVariablesToCheck=avoid_temporal_upgrade:OFF, binlog_error_action:ABORT_SERVER, binlog_group_commit_sync_delay:0, binlog_group_commit_sync_no_delay_count:0, binlog_gtid_simple_recovery:ON, binlog_max_flush_queue_time:0, binlog_order_commits:ON, binlog_rows_query_log_events:OFF, block_encryption_mode:aes-128-ecb, check_proxy_users:OFF, default_password_lifetime:0, disconnect_on_expired_password:ON, end_markers_in_json:OFF, enforce_gtid_consistency:OFF, gtid_executed:*, gtid_next:*, gtid_purged:*, internal_tmp_disk_storage_engine:INNODB, log_bin_use_v1_row_events:OFF, log_builtin_as_identified_by_password:OFF, log_error_verbosity:3, log_statements_unsafe_for_binlog:ON, log_throttle_queries_not_using_indexes:0, master_info_repository:FILE, max_execution_time:0, mysql_native_password_proxy_users:OFF, ngram_token_size:2, offline_mode:OFF, rbr_exec_mode:STRICT, relay_log_info_repository:FILE, require_secure_transport:OFF, rpl_stop_slave_timeout:31536000, server_id_bits:32, session_track_gtids:OFF, sha256_password_proxy_users:OFF, show_compatibility_56:OFF, show_old_temporals:OFF, slave_allow_batching:OFF, slave_checkpoint_group:512, slave_checkpoint_period:300, slave_parallel_type:DATABASE, slave_pending_jobs_size_max:16777216, slave_preserve_commit_order:OFF, super_read_only:OFF, transaction_allow_batching:OFF, transaction_write_set_extraction:OFF

#Check for CREATE TABLESPACE or other special commands that exists in MySQL and not in MariaDB
CheckCreateTableSpace=CREATE TABLESPACE

#Keywords check
CheckKeywords=OVER, ROWS, RECURSIVE

#JSON Operators
CheckJsonOperators=->, ->>

#Check for JSON_SEARCH() function
CheckJSON_SEARCH=JSON_SEARCH

# Full Text Search Plugins Validion whihc are not supported by MariaDB
CheckFullTextParserPlugin=ngram, mecab

#SHA256 Password Plugin
SHA256PasswordCheck=default_authentication_plugin:mysql_native_password

#InnoDB Encryption Check
EncryptionParametersToCheck=keyring_file_data:data, keyring_encrypted_file_data:data

#MySQL X Pugin to secure connections is not supported by MariaDB
CheckMySQLXPlugin=mysqlx-ssl-ca, mysqlx-ssl-cert, mysqlx-ssl-key

#MySQL Native InnoDB Partitioning is not supported by MariaDB
CheckInnoDBPArtitions=PARTITION BY

#Check the timeout parameters in the SELECT statements and server configurations
CheckMaxStatementTime=MAX_STATEMENT_TIME, MAX_EXECUTION_TIME

#Checks for the Memcached Plugin and identifies the list of tables using it.
CheckMemCachedPlugin=plugin:daemon_memcached, CachedTableList:SELECT name,db_schema, db_table FROM tinnodb_memcache.container
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
  - `CheckFullTextParserPlugin`
    - Checks for `ngram` and `mecab` plugins which are not supported by MariaDB in the Plugin list and Table Scripts.
- `SHA256PasswordCheck`
  - To validate the SHA256 User Authentication plugin.
    - This is authenticated by vefirying that the value of `default_authentication_plugin` is nolonger `mysql_native_password` Which means that the users created using this new authentication will not work when Migrated to MariaDB
- `EncryptionParametersToCheck`
  - This will verify if Encryption specific parameters exists in the Server config.
    - `keyring_file_data` and `keyring_encrypted_file_data` variables are checked to determine if Encryption is in in use. The InnpDB Encryption from MySQL does not work with MariaDB
- `CheckMySQLXPlugin`
  - This will verify if MySQL's Plugin X is installed and configured on the MySQL server. This plugin does not work with MariaDB.
- `CheckCreateTableSpace`
  - This will check the stored procedures for any occurance of `CREATE TABLESPACE` command. If this command is being used by the specific database users in external scripts, checking for this won't be possible by Expdus. Database teams needs to validate their respective environments and deployment script for such a command on their own.
- `CheckKeywords`
  - This will check the source code and views for `OVER`, `ROWS` and `RECURSIVE` keywords which MariaDB treats as reserved keywords.
- `CheckJsonOperators`
  - This will look throgh Views, Stored Procedures and Stored Functions for the MySQL specicif JSON operators `->` and `->>`.
- `CheckJSON_SEARCH`
  - This will search for JSON_SEARCH() function in Views, Stored Procedures and Stored Functions, Table Script's Virtual Columns and flag if found.
- `CheckMaxStatementTime`
  - This will scan the Stored Procedures, Views and Functions for MAX_STATEMENT_TIME and MAX_EXECUTION_TIME as these are not supported.
  - These parameters will also be scanned against the ServerConfig as MySQL uses MiliSeconds while MariaDB uses Seconds for these two.
- `CheckMemCachedPlugin`
  - This will validate if the memcached plugin is installed and tables are currently using it. All those tables will be flagged.


## Mapping of incompatibilities vs Exodus

Complete list of incompatibilitis as documented at MariaDB Knowledgbase <https://mariadb.com/kb/en/incompatibilities-and-feature-differences-between-mariadb-103-and-mysql-57/#incompatibilities>

Following we will go through all of the above and map with the Exodus implementation if possible else stated otherwise. Some of the points are not really a concern for Migration and will be indicated as such.

### Functional Differences

There are a few functions that exists in MySQL but not in MariaDB, The complete list of functional differences as documented in the following link is handled by `FunctionsToCheck` Exodus config parameter.

- <https://mariadb.com/kb/en/function-differences-between-mariadb-103-and-mysql-57/>

### System Variables Differences

There are quite a few system variable that exists in MySQL but not in MariaDB. These server variables differences are handled by `SystemVariablesToCheck` Exodus config parameter.

- <https://mariadb.com/kb/en/system-variable-differences-between-mariadb-102-and-mysql-57/>

### MySQL Binaries Differences

MySQL binaries (mysqld, myisamchk etc.) give a warning if one uses a prefix of an option (such as --big-table instead of --big-tables). MariaDB binaries work in the same way as most other Unix commands and don't give warnings when using unique prefixes.

This is skipped as it does not impact Migration as such and whatever worked in MySQL should still work in MariaDB

### MySQL GTID

MariaDB's GTID is not compatible with MySQL's. This means that one can't have MySQL 5.7 as a slave for MariaDB 10.3. However MariaDB 10.3 can be a slave of MySQL 5.7 or any earlier MySQL/MariaDB version. Note that MariaDB and MySQL also have different GTID system variables, so these need to be adjusted when migrating.

This will also mean that after the Migration is done, **Primary/Replica setup needs to be re-done based on MariaDB GTID**.

There are a few additional functions related to MySQL GTID implelementation which are already covered under `FunctionsToCheck`.

### Replication of CREATE TABLE

To make CREATE TABLE ... SELECT work the same way in statement based and row based replication it's by default executed as CREATE OR REPLACE TABLE on the slave. One benefit of this is that if the slave dies in the middle of CREATE ... SELECT it will be able to continue.

- One can use the slave-ddl-exec-mode variable to specify how CREATE TABLE and DROP TABLE is replicated.

This is not really a concern since Replicaiton will need to be setup once again after the Migration.

### PERFORMANCE Schema

MySQL has the performance schema enabled by default. For performance reasons MariaDB 10.3 has it disabled by default. You can enable it by starting mysqld with the option --performance-schema.

This is not a concern as MariaDB keeps it disable by default anyways and can be enabled easily if the applicaiton needs it to be available.

### SYS schema

MySQL 5.7 features a new implementation of the performance_schema and a sys schema wrapper. These are not yet supported in MariaDB and not a concern for Migration.

### InnoDB Encryption

MariaDB 10.3 implements InnoDB encryption in a different way to MySQL 5.7, this will be flagged as a concern if the MySQL database implements InnoDB Encryption using the `EncryptionParametersToCheck` Expdus config parameter.

### CREATE TableSpace

MariaDB 10.3 does not support `CREATE TABLESPACE` for InnoDB tables, this might be a concern if the application uses this command in their deployment scripts which are out of scope for Exodus but `CheckCreateTableSpace` config will check stored procedures within the respective databases for this command and flag as an unsupported item.

### Keywords

The `OVER`, `ROWS` and `RECURSIVE` keywords are reserved words in MariaDB 10.3, but not in MySQL 5.7. Note that in MySQL 8.0 these are also reserved words. This will be checked by the `CheckKeywords` Exodus config parameter.

### JSON Data Type

MariaDB stores JSON as true text, not in binary format as MySQL. MariaDB's JSON functions are much faster than MySQL's so there is no need to store in binary format, which would add complexity when manipulating JSON objects.

This will be checked and flagged by the `DataTypesToCheck` Exodus config parameter. This parameter can also have a list of other data types if one needs to check for a specific one.

For the same reason, MariaDB's JSON data type is an alias for LONGTEXT. If you want to replicate JSON columns from MySQL to MariaDB, you should store JSON objects in MySQL in a TEXT or LONGTEXT column or use statement based replication. If you are using JSON columns and want to upgrade to MariaDB, you need to either convert them to TEXT or use mysqldump to copy these tables to MariaDB.

### JSON Operators

MariaDB 10.3 does not support MySQL's JSON operators (-> and ->>). This incompatibility is handled by `CheckJsonOperators` by scanning Views, Table Script Virtual Columns, Stored Procedures and Functions.

As a work around, in MariaDB:

- `->` Returns value from JSON column after evaluating path; equivalent to `JSON_EXTRACT()`
- `->>` Return value from JSON column after evaluating path and unquoting the result; equivalent to `JSON_UNQUOTE(JSON_EXTRACT())`.

### JSON_SEARCH()

MariaDB 10.3 supports the standard by producing null and a warning for JSON_SEARCH when given invalid data, while MySQL produces an error.

This is application specific and needs to be verified in the front-end and back-end application code. Exodus can't evaluate this but Will flag Views, Stored Procedures if JSON_SEARCH() function is used, it's implementation needs to be evaluated manually.

This is handled by `CheckJSON_SEARCH` Exodus config parameter.

### Full Text Search PLugins

MariaDB 10.3 does not support the ngram and MeCab full-text parser plugins - MDEV-10267, MDEV-10268.

This will be handled by `CheckFullTextParserPlugin` Exodus config parameter and scan the Plugin list/Table Script for the usage of `ngram` & `MeCab` plugins.

### MySQL X Plugin

MariaDB 10.3 does not support the MySQL X plugin. This is handled by `CheckMySQLXPlugin` Exodus config parameter. It validates if Plugin related config parameters are configured in thy MySQL server. If they are, this will be flagged as unsupported.

### InnoDB Native Partitions

MariaDB 10.3 does not support MySQL 5.7's “native” InnoDB partitioning handler. This is handled by `CheckInnoDBPArtitions` Exodus config parameter. It will scan through the all the table structure and flag any table using InnoDB partitions.

Partitions needs to be removed in MySQL and re-partitioned after Migration.

### Aborting Statements

MySQL's implementation of aborting statements that exceed a certain time to execute can only kill SELECTs, while MariaDB's can kill any queries (excluding stored procedures).

This is not a concern for Migration, its just how statement exceeding timeout are internally handled by the server.

### MAX_STATEMENT_TIME

MariaDB 10.3 does not support MySQL's SELECT MAX_STATEMENT_TIME = N ... for MySQL older than 5.7.8 or SELECT /*+ MAX_EXECUTION_TIME(n) */ ... for MySQL 5.7.8 and higher.

The MySQL version of max_statement_time is defined in millseconds, not seconds.

Both of these syntax will be validated using `CheckMaxStatementTime` Exodus config parameter. It will scan the Views, Stored Procedures and Stored Functions and flagged as unsupported. These two parametrers will also be checked in the server config as MySQL uses MiliSeconds for configuration while MariaDB uses Seconds.

### MemCached Plugin

MariaDB 10.3 does not support the MySQL Memcached plugin. However, data stored using memcached can be retrieved because the data is stored as InnoDB tables. MariaDB is able to start successfully with an error message of not being able to find libmemcached.so library.

This is handled by `CheckMemCachedPlugin` Expodus config parameter. This will check if the Plugin is installed and which tables in the databases use this plugin. As stated, this plugin needs to be uninstalled before migrating else an error message is geenrated in the server log.

### SHA256 Password Plugin

Users created with MySQL's SHA256 password algorithm cannot be used in MariaDB 10.3.

This is handled by `SHA256PasswordCheck` Exodus config parameter. It will identify if the plugin is in use as the default authentication plugin and flag this as not supported.

### HEX Literals

In MySQL, X'HHHH', the standard SQL syntax for binary string literals, erroneously works in the same way as 0xHHHH, which could work as a number or string depending on the context. In MariaDB, this has been fixed to behave as a string in all contexts (and never as a number).

This is not a concern as the MySQL behaviour has been fixed in MariaDB.

### SHOW CREATE TABLE

In MariaDB 10.3, SHOW CREATE TABLE does not quote the DEFAULT value of an integer. Older versions of MariaDB, and MySQL, do. Since MariaDB 10.3 can support defaults for BLOB and TEXT fields, while MySQL does not, SHOW CREATE TABLE will also append DEFAULT NULL where no default is explicitly provided to nullable BLOB or TEXT fields in MariaDB.

Not a concern for Migration but might impact the apps team or the DBA as the output of the SHOW CREATE TABLE slightly differs in MariaDB vs MySQL. 

This is out of scope fot Exodus.

### Expressions in DEFAULT

Since MariaDB supports expressions in the DEFAULT clause, in MariaDB, the INFORMATION_SCHEMA.COLUMNS table contains extra fields, and also quotes the DEFAULT value of a string in the COLUMN_DEFAULT field in order to distinguish it from an expression.

This is not a concern for Migration as this points to an additional feature in MariaDB.

### New SET OPERATORS

Since MariaDB supports INTERSECT and EXCEPT, these are both reserved words and can't be used as an identifier without being quoted.

This is not a concern for Migration as this points to an additional feature in MariaDB.

### VALUES Clause

As a result of implementing Table Value Constructors, the VALUES function has been renamed to VALUE()

This will be handled by the `FunctionsToCheck` Exodus config parameter, it will scan all the Views, Stored Procedures and Stored Functions for "VALUE " inluding space to avoid conflict with "VALUES" which is supported. To define the space, "]" character is used as the string terminator for Exodus to interpret based on the requirements. Parameter is configured as `VALUE ]`.

### mysql_install_db

MariaDB does not support the --initialize option. Use mysql_install_db instead in MariaDB.

This is MariaDB installation related and might impact some of the automations that have been setup by the clients. This effects all the databases migrating from MySQL to MariaDB.
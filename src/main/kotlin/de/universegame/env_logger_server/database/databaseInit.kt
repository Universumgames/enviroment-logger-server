package de.universegame.env_logger_server.database

import de.universegame.env_logger_server.config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


var database: Database? = null

/**
 * Init Database:
 * Create Schemas, update server info
 * **/
fun initializeDB() {
    database = Database.connect(config.dbConfig.mysqlUrl, driver = "com.mysql.jdbc.Driver", user = config.dbConfig.mysqlUser, password = config.dbConfig.mysqlPwd)

    transaction {
        //addLogger(StdOutSqlLogger)
        //SchemaUtils.create(CmmInfoTable, UsersTable, DevicesTable, InstalledModulesTable, LoginSecretsTable, SessionsTable, APITokenTable)
    }
}
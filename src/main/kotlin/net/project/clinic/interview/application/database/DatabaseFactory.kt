package net.project.clinic.interview.application.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(port: Int, propertiesFilePath: String) {
        Database.connect(getHikari(port,propertiesFilePath))
        transaction {
            SchemaUtils.create(ClinicTable, ExaminationTable, PricingTable)
        }
    }

    fun getHikari(port: Int, propertiesFilePath: String): HikariDataSource {
        val config = HikariConfig(propertiesFilePath)
        config.addDataSourceProperty("portNumber", port)
        return HikariDataSource(config)
    }

}
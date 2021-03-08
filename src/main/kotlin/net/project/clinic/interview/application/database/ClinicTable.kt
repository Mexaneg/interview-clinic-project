package net.project.clinic.interview.application.database

import org.jetbrains.exposed.dao.id.IntIdTable

object ClinicTable : IntIdTable() {
    val name = varchar("name", 100)
    val email = varchar("email", 100)
    val phoneNumber = varchar("phoneNumber", 20)
    val city = varchar("city", 50)
    val address = varchar("address", 200)
}
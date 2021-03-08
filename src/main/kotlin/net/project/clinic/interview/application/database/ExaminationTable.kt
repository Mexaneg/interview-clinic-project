package net.project.clinic.interview.application.database

import org.jetbrains.exposed.dao.id.IntIdTable

object ExaminationTable : IntIdTable() {
    val title = varchar("title", 100)
    val description = varchar("description", 255)
}
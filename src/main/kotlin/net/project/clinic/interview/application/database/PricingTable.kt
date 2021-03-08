package net.project.clinic.interview.application.database

import org.jetbrains.exposed.dao.id.IntIdTable

object PricingTable : IntIdTable() {
    val clinic = reference("clinicid", ClinicTable.id)
    val examination = reference("examinationid", ExaminationTable.id)
    val price = long("price")
}
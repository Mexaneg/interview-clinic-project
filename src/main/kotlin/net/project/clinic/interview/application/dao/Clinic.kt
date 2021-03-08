package net.project.clinic.interview.application.dao

import io.ktor.application.*
import io.ktor.response.*
import net.project.clinic.interview.application.database.ClinicTable
import net.project.clinic.interview.application.database.PricingTable
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class Clinic(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, Clinic>(ClinicTable) {
        val CLINIC_NOT_FOUND = { id: Int -> "Clinic id = $id not found" }
        val CLINIC_UPDATED = { id: Int -> "Clinic id = $id was updated" }
        val CLINIC_DELETED = { id:Int -> "Clinic id = $id was deleted"}
        const val CLINIC_CREATED = "Clinic was created"
        const val CLINIC_ENTITY = "clinic"

    }


    var name by ClinicTable.name
    var email by ClinicTable.email
    var phoneNumber by ClinicTable.phoneNumber
    var city by ClinicTable.city
    var address by ClinicTable.address
    val examinations by Price referrersOn PricingTable.clinic
}
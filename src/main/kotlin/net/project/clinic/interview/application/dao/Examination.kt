package net.project.clinic.interview.application.dao

import net.project.clinic.interview.application.database.ExaminationTable
import net.project.clinic.interview.application.database.PricingTable
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

class Examination(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, Examination>(ExaminationTable) {
        val EXAMINATION_NOT_FOUND = { id: Int -> "Examination id = $id not found" }
        val EXAMINATION_UPDATED = { id: Int -> "Examination id = $id was updated" }
        val EXAMINATION_DELETED = { id: Int -> "Examination id = $id was deleted" }
        const val EXAMINATION_CREATED = "Examination was created"
        const val EXAMINATION_ENTITY: String = "examination"
    }

    var title by ExaminationTable.title
    var description by ExaminationTable.description
    val prices by Price referrersOn PricingTable.examination
}
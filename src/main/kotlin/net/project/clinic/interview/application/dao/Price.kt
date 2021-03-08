package net.project.clinic.interview.application.dao

import net.project.clinic.interview.application.database.PricingTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Price(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Price>(PricingTable) {
        const val PRICE_CREATED = "Price was created"
        const val PRICE_ENTITY = "price"
        val PRICE_NOT_FOUND = { id: Int -> "Price id = $id not found" }
        val PRICE_UPDATED = { id: Int -> "Price id = $id was updated" }
        val PRICE_DELETED = { id: Int -> "Price id = $id was deleted" }
    }

    var price by PricingTable.price
    var clinic by Clinic referencedOn PricingTable.clinic
    var examination by Examination referencedOn PricingTable.examination
}
package net.project.clinic.interview.application.repo

import net.project.clinic.interview.application.dao.Price
import net.project.clinic.interview.application.database.PricingTable
import net.project.clinic.interview.application.dto.PriceDTO
import net.project.clinic.interview.application.dto.PriceResponseDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PriceRepo {
    fun create(priceDTO: PriceDTO) {
        transaction {
            addLogger(StdOutSqlLogger)
            Price.new {
                price = priceDTO.price
                clinic = priceDTO.clinic
                examination = priceDTO.examination
            }
        }
    }

    fun getAll(): List<PriceResponseDTO> {
        return transaction {
            addLogger(StdOutSqlLogger)
            PricingTable.selectAll().map {
                it.toPrice()
            }
        }
    }

    fun get(id: Int): PriceResponseDTO? {
        return transaction {
            addLogger(StdOutSqlLogger)
            PricingTable.select { PricingTable.id eq id }.map {
                it.toPrice()
            }.firstOrNull()
        }
    }

    fun delete(id: Int) {
        transaction {
            addLogger(StdOutSqlLogger)
            PricingTable.deleteWhere { PricingTable.id eq id }
        }
    }

    private fun ResultRow.toPrice(): PriceResponseDTO {
        return PriceResponseDTO(
            this[PricingTable.id].value,
            this[PricingTable.examination].value,
            this[PricingTable.clinic].value,
            this[PricingTable.price]
        )
    }
}
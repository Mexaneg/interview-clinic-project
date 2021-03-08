package net.project.clinic.interview.application.repo

import net.project.clinic.interview.application.database.ClinicTable
import net.project.clinic.interview.application.dto.ClinicRequestDTO
import net.project.clinic.interview.application.dto.ClinicResponseDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ClinicRepo {
    fun create(clinicResponseDTO: ClinicRequestDTO) {
        transaction {
            addLogger(StdOutSqlLogger)
            ClinicTable.insert {
                it[name] = clinicResponseDTO.name
                it[email] = clinicResponseDTO.email
                it[phoneNumber] = clinicResponseDTO.phoneNumber
                it[city] = clinicResponseDTO.city
                it[address] = clinicResponseDTO.address
            }
        }
    }

    fun get(id: Int): ClinicResponseDTO? {
        return transaction {
            addLogger(StdOutSqlLogger)
            ClinicTable.select { ClinicTable.id eq id }.map {
                it.toClinic()
            }.firstOrNull()
        }
    }

    fun getAll(): List<ClinicResponseDTO> {
        return transaction {
            addLogger(StdOutSqlLogger)
            ClinicTable.selectAll().map { it.toClinic() }
        }
    }

    fun delete(id: Int) {
        transaction {
            addLogger(StdOutSqlLogger)
            ClinicTable.deleteWhere { ClinicTable.id eq id }
        }
    }

    fun update(id: Int, clinicResponseDTO: ClinicRequestDTO) {
        transaction {
            addLogger(StdOutSqlLogger)
            ClinicTable.update {
                ClinicTable.id eq id
                it[name] = clinicResponseDTO.name
                it[email] = clinicResponseDTO.email
                it[phoneNumber] = clinicResponseDTO.phoneNumber
                it[city] = clinicResponseDTO.city
                it[address] = clinicResponseDTO.address
            }
        }
    }

    private fun ResultRow.toClinic(): ClinicResponseDTO {
        return ClinicResponseDTO(
            this[ClinicTable.id],
            this[ClinicTable.name],
            this[ClinicTable.email],
            this[ClinicTable.phoneNumber],
            this[ClinicTable.city],
            this[ClinicTable.address]
        )
    }
}
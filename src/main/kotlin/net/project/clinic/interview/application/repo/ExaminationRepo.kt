package net.project.clinic.interview.application.repo

import net.project.clinic.interview.application.database.ExaminationTable
import net.project.clinic.interview.application.dto.ExaminationRequestDTO
import net.project.clinic.interview.application.dto.ExaminationResponseDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ExaminationRepo {
    fun create(examinationResponseDTO: ExaminationRequestDTO) {
        transaction {
            addLogger(StdOutSqlLogger)
            ExaminationTable.insert {
                it[title] = examinationResponseDTO.title
                it[description] = examinationResponseDTO.description
            }
        }
    }

    fun getAll(): List<ExaminationResponseDTO> {
        return transaction {
            addLogger(StdOutSqlLogger)
            ExaminationTable.selectAll().map {
                it.toExamination()
            }
        }
    }

    fun get(id: Int): ExaminationResponseDTO? {
        return transaction {
            addLogger(StdOutSqlLogger)
            ExaminationTable.select { ExaminationTable.id eq id }.map {
                it.toExamination()
            }.firstOrNull()
        }
    }

    fun delete(id:Int){
        transaction {
            addLogger(StdOutSqlLogger)
            ExaminationTable.deleteWhere { ExaminationTable.id eq id }
        }
    }

    fun update(id: Int, examinationRequestDTO: ExaminationRequestDTO){
        transaction {
            addLogger(StdOutSqlLogger)
            ExaminationTable.update { ExaminationTable.id eq id
                it[title] = examinationRequestDTO.title
                it[description] = examinationRequestDTO.description
            }
        }
    }

    private fun ResultRow.toExamination(): ExaminationResponseDTO {
        return ExaminationResponseDTO(this[ExaminationTable.id].value, this[ExaminationTable.title], this[ExaminationTable.description])
    }
}
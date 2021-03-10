package net.project.clinic.interview.application.routing

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.project.clinic.interview.application.chekId
import net.project.clinic.interview.application.dao.Clinic
import net.project.clinic.interview.application.dao.Examination
import net.project.clinic.interview.application.dao.Price
import net.project.clinic.interview.application.database.PricingTable
import net.project.clinic.interview.application.dto.ExaminationRequestDTO
import net.project.clinic.interview.application.dto.PriceResponseDTO
import net.project.clinic.interview.application.repo.ExaminationRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction


fun Route.examinationRout(examinationRepo: ExaminationRepo) {

    route("/examination") {
        get("/") {
            call.respond(examinationRepo.getAll())
        }

        get("/{examinationId}") {
            val examinationId = call.parameters["examinationId"].chekId(Examination.EXAMINATION_ENTITY)
            examinationRepo.get(examinationId)?.let { examination -> call.respond(examination) } ?: kotlin.run {
                call.respond(Examination.EXAMINATION_NOT_FOUND(examinationId))
            }
        }

        get("/{examinationId}/prices") {
            val examinationId = call.parameters["examinationId"].chekId(Examination.EXAMINATION_ENTITY)
            val examination = transaction { Examination.findById(examinationId) }
            if (examination == null) {
                val message = Examination.EXAMINATION_NOT_FOUND(examinationId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }
            call.respond(transaction { examination.prices.map { it.clinic.name to it.price } })
        }

        get("/{examinationId}/clinic/{clinicId}") {
            val clinicId = call.parameters["clinicId"].chekId(Clinic.CLINIC_ENTITY)
            val examinationId = call.parameters["examinationId"].chekId(Examination.EXAMINATION_ENTITY)
            val price = transaction {
                Price.find { PricingTable.examination eq examinationId and (PricingTable.clinic eq clinicId) }.single()
            }
            val priceResponseDTO = transaction {
                PriceResponseDTO(
                    price.id.value,
                    price.examination.id.value,
                    price.clinic.id.value,
                    price.price
                )
            }
            call.respond(priceResponseDTO)
        }

        delete("/{examinationId}") {
            val examinationId = call.parameters["examinationId"].chekId(Examination.EXAMINATION_ENTITY)
            examinationRepo.delete(examinationId)
            call.respond(Examination.EXAMINATION_DELETED(examinationId))
        }

        put("/{examinationId}") {
            val examinationId = call.parameters["examinationId"].chekId(Examination.EXAMINATION_ENTITY)
            val receivedExamination = call.receive(ExaminationRequestDTO::class)
            examinationRepo.update(examinationId, receivedExamination)
            call.respond(Examination.EXAMINATION_UPDATED(examinationId))
        }

        post("/create") {
            val receivedExamination = call.receive(ExaminationRequestDTO::class)
            examinationRepo.create(receivedExamination)
            call.respond(Examination.EXAMINATION_CREATED)
        }
    }
}
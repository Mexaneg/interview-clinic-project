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
import net.project.clinic.interview.application.dto.ClinicRequestDTO
import net.project.clinic.interview.application.dto.PriceResponseDTO
import net.project.clinic.interview.application.repo.ClinicRepo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction


fun Route.clinicRout(clinicRepo: ClinicRepo) {
    route("/clinic") {

        get("/") {
            call.respond(clinicRepo.getAll())
        }

        get("/{clinicId}") {
            val clinicId = call.parameters["clinicId"].chekId(Clinic.CLINIC_ENTITY)

            clinicRepo.get(clinicId)?.let { clinic -> call.respond(clinic) } ?: kotlin.run {
                call.respond(Clinic.CLINIC_NOT_FOUND(clinicId))
            }
        }

        get("/{clinicId}/examinations") {
            val clinicId = call.parameters["clinicId"].chekId(Clinic.CLINIC_ENTITY)
            val clinic = transaction { Clinic.findById(clinicId) }
            if (clinic == null) {
                val message = Clinic.CLINIC_NOT_FOUND(clinicId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }
            call.respond(transaction { clinic.examinations.map { it.examination.title to it.price } })
        }
        get("/{clinicId}/examination/{examinationId}") {
            val clinicId = call.parameters["clinicId"].chekId(Clinic.CLINIC_ENTITY)
            val examinationId = call.parameters["examinationId"].chekId(Examination.EXAMINATION_ENTITY)
            val price = transaction {
                Price.find { PricingTable.clinic eq clinicId and (PricingTable.examination eq examinationId) }.single()
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

        delete("/{clinicId}") {
            val clinicId = call.parameters["clinicId"].chekId(Clinic.CLINIC_ENTITY)
            clinicRepo.delete(clinicId)
            call.respond(Clinic.CLINIC_DELETED(clinicId))
        }

        put("/{clinicId}") {
            val clinicId = call.parameters["clinicId"].chekId(Clinic.CLINIC_ENTITY)
            val receivedClinic = call.receive(ClinicRequestDTO::class)
            clinicRepo.update(clinicId, receivedClinic)
            call.respond(Clinic.CLINIC_UPDATED(clinicId))
        }

        post("/create") {
            val receivedClinic = call.receive(ClinicRequestDTO::class)
            clinicRepo.create(receivedClinic)
            call.respond(Clinic.CLINIC_CREATED)
        }
    }
}
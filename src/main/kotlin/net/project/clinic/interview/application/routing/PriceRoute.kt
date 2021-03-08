package net.project.clinic.interview.application.routing

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.project.clinic.interview.application.chekId
import net.project.clinic.interview.application.dao.Clinic
import net.project.clinic.interview.application.dao.Examination
import net.project.clinic.interview.application.dao.Price
import net.project.clinic.interview.application.dto.PriceDTO
import net.project.clinic.interview.application.dto.PriceRequestDTO
import net.project.clinic.interview.application.repo.PriceRepo
import org.jetbrains.exposed.sql.transactions.transaction


fun Route.priceRout(priceRepo: PriceRepo) {
    route("/price") {
        get("/") {
            call.respond(priceRepo.getAll())
        }

        post("/create") {
            val receivedPrice = call.receive(PriceRequestDTO::class)
            val clinic = transaction { Clinic.findById(receivedPrice.clinicId) }
            val examination = transaction { Examination.findById(receivedPrice.examinationId) }
            if (clinic == null) {
                val message = Clinic.CLINIC_NOT_FOUND(receivedPrice.clinicId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }
            if (examination == null) {
                val message = Examination.EXAMINATION_NOT_FOUND(receivedPrice.examinationId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }

            transaction { priceRepo.create(PriceDTO(examination, clinic, receivedPrice.price)) }
            call.respond(Price.PRICE_CREATED)
        }

        put("/{priceId}") {
            val priceId = call.parameters["priceId"].chekId(Price.PRICE_ENTITY)
            val receivedPrice = call.receive(PriceRequestDTO::class)
            val price = transaction { Price.findById(priceId) }
            val clinic = transaction { Clinic.findById(receivedPrice.clinicId) }
            val examination = transaction { Examination.findById(receivedPrice.examinationId) }
            if (clinic == null) {
                val message = Clinic.CLINIC_NOT_FOUND(receivedPrice.clinicId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }
            if (examination == null) {
                val message = Examination.EXAMINATION_NOT_FOUND(receivedPrice.examinationId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }
            if (price == null) {
                val message = Price.PRICE_NOT_FOUND(priceId)
                call.respond(message)
                throw IllegalArgumentException(message)
            }
            transaction {
                price.price = receivedPrice.price
                price.clinic = clinic
                price.examination = examination
            }
            call.respond(Price.PRICE_UPDATED(priceId))
        }

        delete("/{priceId}") {
            val priceId = call.parameters["priceId"].chekId(Price.PRICE_ENTITY)
            priceRepo.delete(priceId)
            call.respond(Price.PRICE_DELETED(priceId))
        }
    }
}
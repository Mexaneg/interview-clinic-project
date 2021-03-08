package net.project.clinic.interview

import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.jackson.*
import com.fasterxml.jackson.databind.*
import io.ktor.application.*
import net.project.clinic.interview.application.database.DatabaseFactory
import net.project.clinic.interview.application.repo.ExaminationRepo
import net.project.clinic.interview.application.repo.ClinicRepo
import net.project.clinic.interview.application.repo.PriceRepo
import net.project.clinic.interview.application.routing.examinationRout
import net.project.clinic.interview.application.routing.clinicRout
import net.project.clinic.interview.application.routing.priceRout

fun main(args: Array<String>): Unit =
        io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DataConversion)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    DatabaseFactory.init()
    install(Routing) {
        clinicRout(ClinicRepo())
        examinationRout(ExaminationRepo())
        priceRout(PriceRepo())
    }
}

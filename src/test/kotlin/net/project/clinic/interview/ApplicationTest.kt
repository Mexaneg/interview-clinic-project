package net.project.clinic.interview

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import net.project.clinic.interview.application.dao.Clinic
import net.project.clinic.interview.application.dao.Examination
import net.project.clinic.interview.application.dao.Price
import net.project.clinic.interview.application.database.ClinicTable
import net.project.clinic.interview.application.database.DatabaseFactory
import net.project.clinic.interview.application.database.ExaminationTable
import net.project.clinic.interview.application.database.PricingTable
import net.project.clinic.interview.application.dto.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

import org.testcontainers.containers.PostgreSQLContainer

class ApplicationTest {
    companion object {
        class AppPostgreSQLContainer : PostgreSQLContainer<AppPostgreSQLContainer>("postgres:12.6")

        var postgresContainer:AppPostgreSQLContainer = AppPostgreSQLContainer()


        fun initContainer(){
            if (postgresContainer.isRunning){
                postgresContainer.stop()
            }
            postgresContainer = AppPostgreSQLContainer()
            postgresContainer.start()
            Database.connect(DatabaseFactory.getHikari(postgresContainer.firstMappedPort, "/testHikari.properties"))
        }

        val clinicName = { id: Int -> "Clinic Name $id" }
        val clinicAddress = { id: Int -> "Clinic Address $id" }
        val clinicCity = { id: Int -> "Clinic City $id" }
        val clinicEmail = { id: Int -> "Clinic Email $id" }
        val clinicPhone = { id: Int -> "phone $id" }

        val examinationTitle = { id: Int -> "Examination $id" }
        val examinationDescription = { id: Int -> "Examination description $id" }

        val mapper = jacksonObjectMapper()
    }

    @BeforeTest
    fun init() {
        initContainer()
        transaction {
            //SchemaUtils.drop(ClinicTable, ExaminationTable, PricingTable)
            SchemaUtils.create(ClinicTable, ExaminationTable, PricingTable)
        }
        transaction {
            for (i in 1..5) {
                ClinicTable.insert {
                    it[address] = clinicAddress(i)
                    it[city] = clinicCity(i)
                    it[email] = clinicEmail(i)
                    it[name] = clinicName(i)
                    it[phoneNumber] = clinicPhone(i)
                }
            }
        }
        transaction {
            for (i in 1..5) {
                ExaminationTable.insert {
                    it[description] = examinationDescription(i)
                    it[title] = examinationTitle(i)
                }
            }
        }
        val clinicRecord = transaction { Clinic.findById(1) }
        val examinationRecord = transaction { Examination.findById(1) }
        transaction {
            Price.new {
                price = 1000
                clinic = clinicRecord!!
                examination = examinationRecord!!
            }
        }
    }


    @Test
    fun testGetClinics() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/clinic/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val size = mapper.readTree(response.response.content).size()
            assertEquals(5, size)
        }
    }

    @Test
    fun testGetClinic() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/clinic/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val clinic: ClinicResponseDTO = mapper.readValue(response.response.content!!)
            assertEquals(1, clinic.clinicId)
            assertEquals(clinicCity(1), clinic.city)
        }
    }

    @Test
    fun testGetClinicExaminations() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/clinic/1/examinations") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val size = mapper.readTree(response.response.content).size()
            assertEquals(1, size)
        }
    }

    @Test
    fun testGetClinicExamination() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/clinic/1/examination/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val price: PriceResponseDTO = mapper.readValue(response.response.content!!)
            assertEquals(1, price.clinicId)
        }
    }

    @Test
    fun testDeleteClinic() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Delete, "/clinic/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Clinic.CLINIC_DELETED(1))
            val clinicsCount = transaction { Clinic.count() }
            assertEquals(4, clinicsCount)
        }
    }

    @Test
    fun testUpdateClinic() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val newClinic = ClinicRequestDTO(
                clinicName(333),
                clinicEmail(333),
                clinicPhone(333),
                clinicCity(333),
                clinicAddress(333)
            )
            val response = handleRequest(HttpMethod.Put, "/clinic/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(mapper.writeValueAsString(newClinic))
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Clinic.CLINIC_UPDATED(1))
            assertEquals(transaction { Clinic[1].phoneNumber }, clinicPhone(333))

        }
    }

    @Test
    fun testCreateClinic() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val newClinic = ClinicRequestDTO(
                clinicName(333),
                clinicEmail(333),
                clinicPhone(333),
                clinicCity(333),
                clinicAddress(333)
            )
            val response = handleRequest(HttpMethod.Post, "/clinic/create") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(mapper.writeValueAsString(newClinic))
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Clinic.CLINIC_CREATED)
            assertEquals(transaction { Clinic[6].phoneNumber }, clinicPhone(333))

        }
    }

    @Test
    fun testGetExaminations() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/examination/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val size = mapper.readTree(response.response.content).size()
            assertEquals(5, size)
        }
    }

    @Test
    fun testGetExamination() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/examination/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val examination: ExaminationResponseDTO = mapper.readValue(response.response.content!!)
            assertEquals(1, examination.examinationId)
            assertEquals(examinationDescription(1), examination.description)
        }
    }

    @Test
    fun testGetExaminationPrices() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/examination/1/prices") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val size = mapper.readTree(response.response.content).size()
            assertEquals(1, size)
        }
    }

    @Test
    fun testGetExaminationClinic() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/examination/1/clinic/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val price: PriceResponseDTO = mapper.readValue(response.response.content!!)
            assertEquals(1, price.examinationId)
        }
    }

    @Test
    fun testDeleteExamination() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Delete, "/examination/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Examination.EXAMINATION_DELETED(1))
            val examinationCount = transaction { Examination.count() }
            assertEquals(4, examinationCount)
        }
    }

    @Test
    fun testUpdateExamination() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val newExamination = ExaminationRequestDTO(
                examinationTitle(333), examinationDescription(333)
            )
            val response = handleRequest(HttpMethod.Put, "/examination/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(mapper.writeValueAsString(newExamination))
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Examination.EXAMINATION_UPDATED(1))
            assertEquals(transaction { Examination[1].description }, examinationDescription(333))

        }
    }

    @Test
    fun testCreateExamination() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val newExamination = ExaminationRequestDTO(
                examinationTitle(333), examinationDescription(333)
            )
            val response = handleRequest(HttpMethod.Post, "/examination/create") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(mapper.writeValueAsString(newExamination))
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Examination.EXAMINATION_CREATED)
            assertEquals(transaction { Examination[6].description }, examinationDescription(333))

        }
    }

    @Test
    fun testGetPrices() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val response = handleRequest(HttpMethod.Get, "/price/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            val size = mapper.readTree(response.response.content).size()
            assertEquals(1, size)
        }
    }

    @Test
    fun testUpdatePrice() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val newPrice = PriceRequestDTO(2,2,2000)
            val response = handleRequest(HttpMethod.Put, "/price/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(mapper.writeValueAsString(newPrice))
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Price.PRICE_UPDATED(1))
            assertEquals(transaction { Price[1].clinic.id.value }, 2)

        }
    }

    @Test
    fun testCreatePrice() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {
            val newPrice = PriceRequestDTO(2,2,2000)
            val response = handleRequest(HttpMethod.Post, "/price/create") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(mapper.writeValueAsString(newPrice))
            }
            assertEquals(HttpStatusCode.OK, response.response.status())
            assertEquals(response.response.content, Price.PRICE_CREATED)
            assertEquals(transaction { Price.count()}, 2)
        }
    }
}
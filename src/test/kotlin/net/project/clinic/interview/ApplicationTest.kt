package net.project.clinic.interview

import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*

import org.testcontainers.containers.PostgreSQLContainer

class ApplicationTest {
    companion object {
        class AppPostgreSQLContainer : PostgreSQLContainer<AppPostgreSQLContainer>("postgres:12.6")

        val postgresContainer = AppPostgreSQLContainer()
        init {
            postgresContainer.start()
        }
    }

    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true, postgresContainer.firstMappedPort) }) {

            handleRequest(HttpMethod.Get, "/clinic/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello World!", response.content)
            }
        }
    }
}
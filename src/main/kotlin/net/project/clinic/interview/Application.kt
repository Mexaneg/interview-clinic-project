package net.project.clinic.interview

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.jackson.*
import com.fasterxml.jackson.databind.*
import io.ktor.auth.*
import io.ktor.util.*
import io.ktor.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

fun main(args: Array<String>): Unit =
        io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
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

    val jwtIssuer = environment.config.property("jwt.domain").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    authentication {
        jwt {
            realm = jwtRealm
            verifier(makeJwtVerifier(jwtIssuer, jwtAudience))
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
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
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    routing {
        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

private val algorithm = Algorithm.HMAC256("secret")
private fun makeJwtVerifier(issuer: String, audience: String): JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

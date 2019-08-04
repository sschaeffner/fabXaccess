package cloud.fabx

import cloud.fabx.model.User
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val user1 = User(
    1,
    "Herr Tester",
    "tester",
    "1234",
    false,
    null,
    "0x11223344556677"
)

val userList = listOf(user1)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        route("/api") {
            route("/user") {
                get("") {
                    call.respond(userList)
                }

                get("/{id}") {
                    call.respond(user1)
                }
            }
        }
    }
}


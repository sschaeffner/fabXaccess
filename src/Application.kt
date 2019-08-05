package cloud.fabx

import cloud.fabx.db.DbHandler
import cloud.fabx.model.*
import cloud.fabx.service.DeviceService
import cloud.fabx.service.PermissionService
import cloud.fabx.service.ToolService
import cloud.fabx.service.UserService
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.jackson.jackson
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

val userService = UserService()
val deviceService =  DeviceService()
val toolService = ToolService()
val permissionService = PermissionService()

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    if (!testing) addTestContent()

    install(ContentNegotiation) {
        jackson {}
    }
    install(ForwardedHeaderSupport) // support for reverse proxies
    install(StatusPages) {
        exception<JsonProcessingException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.originalMessage)
        }
        exception<ExposedSQLException> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
        }
    }
    install(Routing) {
        routes()
    }
}

fun addTestContent() {
    transaction(DbHandler.db) {
        addLogger(StdOutSqlLogger)

        val user1 = User.new {
            name = "Tester 1"
            wikiName = "wikiTester1"
            phoneNumber = "0049 123 456"
            locked = false
            lockedReason = ""
            cardId = "11223344556677"
        }

        val device1 = Device.new {
            name = "Device 1"
            mac = "aaffeeaaffee"
            secret = "someSecret"
            bgImageUrl = "http://bla"
        }

        val tool1 = Tool.new {
            device = device1
            name = "Tool 1"
            pin = 0
            toolType = ToolType.UNLOCK
            toolState = ToolState.GOOD
            wikiLink = ""
        }

        user1.permissions = SizedCollection(listOf(tool1))
    }
}
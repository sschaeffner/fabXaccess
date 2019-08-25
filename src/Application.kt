package cloud.fabx

import cloud.fabx.db.DbHandler
import cloud.fabx.model.*
import cloud.fabx.service.*
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
val adminService = AdminService()
val userService = UserService()
val deviceService =  DeviceService()
val toolService = ToolService()
val permissionService = PermissionService()

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(demoContent: Boolean = true, apiAuthentication: Boolean = true, clientApiAuthentication: Boolean = true) {

    if (demoContent) addDemoContent()

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
    install(Authentication) {
        basic(name = "apiAuth") {
            realm = "fabX access API"
            validate { credentials ->
                if (adminService.checkAdminCredentials(credentials.name, credentials.password)) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

    }
    install(Routing) {
        if (apiAuthentication) {
            authenticate("apiAuth") {
                api()
            }
        } else {
            api()
        }
        if (clientApiAuthentication) {
            //TODO client api authentication
            clientApi()
        } else {
            clientApi()
        }
    }
}

fun addDemoContent() {
    transaction(DbHandler.db) {
        addLogger(StdOutSqlLogger)

        Admin.new {
            name = "admin1"
            // password: demopassword
            // echo -n fabXfabXfabX12demopassword | openssl dgst -binary -sha256 | openssl base64
            passwordHash = "gT5X1NWx+kAq+JtuCM3URo4ur22D3XDA0vvShT673P8="
        }

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
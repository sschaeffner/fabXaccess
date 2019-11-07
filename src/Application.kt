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
val qualificationService = QualificationService()

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(demoContent: Boolean = false, apiAuthentication: Boolean = true, clientApiAuthentication: Boolean = true) {

    val dbUrl = environment.config.propertyOrNull("heroku.dbUrl")

    val jdbcUrl = environment.config.propertyOrNull("fabx.db.jdbcUrl")
    val dbUser = environment.config.propertyOrNull("fabx.db.dbUser")
    val dbPassword = environment.config.propertyOrNull("fabx.db.dbPassword")

    if (dbUrl != null) {
        log.info("configuring heroku DATABASE_URL")
        DbHandler.configure(dbUrl.getString())
    } else if (jdbcUrl != null && dbUser != null && dbPassword != null) {
        log.info("configuring jdbc url")
        DbHandler.configure(jdbcUrl.getString(), dbUser.getString(), dbPassword.getString())
    } else {
        log.info("using default database configuration (H2).")
    }


    val demoContentEnabled = environment.config.propertyOrNull("fabx.access.demoContent")?.getString()?.let { it == "true" } ?: demoContent

    if (demoContentEnabled) {
        log.info("Demo Content Enabled")
        addDemoContent()
    } else {
        log.info("Demo Content Disabled")
    }

    install(ContentNegotiation) {
        jackson {}
    }
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)

        header(HttpHeaders.Accept)
        header(HttpHeaders.AccessControlRequestHeaders)
        header(HttpHeaders.AccessControlRequestMethod)
        header(HttpHeaders.XForwardedProto)
        header(HttpHeaders.Origin)
        header(HttpHeaders.Referrer)
        header(HttpHeaders.UserAgent)
        header(HttpHeaders.Authorization)

        anyHost()
        //host("localhost:4200")

        allowCredentials = true
        allowNonSimpleContentTypes = true
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
        basic(name = "clientApiAuth") {
            realm = "fabX access client API"
            validate {credentials ->
                if (deviceService.checkDeviceCredentials(credentials.name, credentials.password)) {
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
            authenticate("clientApiAuth") {
                clientApi()
            }
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
            firstName = "Nikola"
            lastName = "Testler"
            wikiName = "wikiTester1"
            phoneNumber = "0049 123 456"
            locked = false
            lockedReason = ""
            cardId = "11223344556677"
            cardSecret = "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"
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

        val qualification1 = Qualification.new {
            name = "Qualification 1"
            description = "Qualification for some tools"
            colour = "#aabbcc"
        }

        user1.qualifications = SizedCollection(listOf(qualification1))
        tool1.qualifications = SizedCollection(listOf(qualification1))
    }
}
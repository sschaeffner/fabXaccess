package cloud.fabx

import cloud.fabx.application.AuthorizationException
import cloud.fabx.db.DbHandler
import cloud.fabx.model.Admin
import cloud.fabx.model.Device
import cloud.fabx.model.Qualification
import cloud.fabx.model.Tool
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
import cloud.fabx.model.User
import cloud.fabx.service.AuthenticationService
import cloud.fabx.service.DeviceService
import cloud.fabx.service.QualificationService
import cloud.fabx.service.ToolService
import cloud.fabx.service.UserService
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
val authenticationService = AuthenticationService()
val userService = UserService()
val deviceService = DeviceService()
val toolService = ToolService()
val qualificationService = QualificationService()

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(demoContent: Boolean = false, testAdmin: Boolean = false) {

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


    val demoContentEnabled =
        environment.config.propertyOrNull("fabx.access.demoContent")?.getString()?.let { it == "true" } ?: demoContent

    if (demoContentEnabled) {
        log.info("Demo Content Enabled")
        addDemoContent()
    } else {
        log.info("Demo Content Disabled")
    }

    if (testAdmin) {
        log.warn("Test Admin Enabled. This should never be the case on production systems.")
        addTestAdmin()
    } else {
        log.info("Test Admin Disabled. This is the safe configuration for production systems.")
    }

    log.info("connecting to database...")
    DbHandler.db

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
        exception<AuthorizationException> { cause ->
            call.respond(HttpStatusCode.Forbidden, cause.localizedMessage)
        }
    }
    install(Authentication) {
        basic(name = "apiAuth") {
            realm = "fabX access API"
            validate { credentials ->
                authenticationService.checkAdminCredentials(credentials.name, credentials.password)
            }
        }
        basic(name = "clientApiAuth") {
            realm = "fabX access client API"
            validate { credentials ->
                deviceService.checkDeviceCredentials(credentials.name, credentials.password)
            }
        }
    }
    install(Routing) {
        authenticate("apiAuth") {
            api()
        }
        authenticate("clientApiAuth") {
            clientApi()
        }
    }
}

fun addDemoContent() {
    transaction(DbHandler.db) {

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
            backupBackendUrl = "http://fabx.backup"
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
            orderNr = 1
        }

        user1.qualifications = SizedCollection(listOf(qualification1))
        tool1.qualifications = SizedCollection(listOf(qualification1))
    }
}

fun addTestAdmin() {
    transaction(DbHandler.db) {
        Admin.new {
            name = "admin"
            // password: password
            // echo -n fabXfabXfabX8password | openssl dgst -binary -sha256 | openssl base64
            passwordHash = "2o0iqAqKf1UEB2IrsWfSb3aaSL0gwyEQatwW+o6/Qf4="
        }
    }
}
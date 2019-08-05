package cloud.fabx

import cloud.fabx.db.DbHandler
import cloud.fabx.dto.*
import cloud.fabx.model.*
import cloud.fabx.service.DeviceService
import cloud.fabx.service.ToolService
import cloud.fabx.service.UserService
import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.request.receive
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {

    transaction(DbHandler.db) {
        addLogger(StdOutSqlLogger)

        User.new {
            name = "Tester 1"
            wikiName = "wikiTester1"
            phoneNumber = "0049 123 456"
            locked = false
            lockedReason = ""
            cardId = "11223344556677"
        }

        Device.new {
            name = "Device 1"
            mac = "aaffeeaaffee"
            secret = "someSecret"
            bgImageUrl = "http://bla"
        }

        Tool.new {
            name = "Tool 1"
            pin = 0
            toolType = ToolType.UNLOCK
            toolState = ToolState.GOOD
            wikiLink = ""
        }
    }

    io.ktor.server.netty.EngineMain.main(args)
}

val userService = UserService()
val deviceService =  DeviceService()
val toolService = ToolService()

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {}
    }
    install(ForwardedHeaderSupport) // support for reverse proxies
    install(StatusPages) {
        exception<JsonProcessingException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.originalMessage)
        }
    }

    routing {
        route("/api") {
            route("/user") {
                get("") {
                    call.respond(userService.getAllUsers())
                }

                get("/{id}") {
                    val user: UserDto? = call.parameters["id"]?.toInt()?.let {
                        userService.getUserById(it)
                    }

                    if (user != null) {
                        call.respond(user)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("") {
                    val user = call.receive<NewUserDto>()
                    call.respond(userService.createNewUser(user))
                }
            }

            route("/device") {
                get("") {
                    call.respond(deviceService.getAllDevices())
                }

                get("/{id}") {
                    val device: DeviceDto? = call.parameters["id"]?.toInt()?.let {
                        deviceService.getDeviceById(it)
                    }

                    if (device != null) {
                        call.respond(device)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("") {
                    val device = call.receive<NewDeviceDto>()
                    call.respond(deviceService.createNewDevice(device))
                }
            }

            route("/tool") {
                get("") {
                    call.respond(toolService.getAllTools())
                }

                get("/{id}") {
                    val tool: ToolDto? = call.parameters["id"]?.toInt()?.let {
                        toolService.getToolById(it)
                    }

                    if (tool != null) {
                        call.respond(tool)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("") {
                    val tool = call.receive<NewToolDto>()
                    call.respond(toolService.createNewTool(tool))
                }
            }
        }
    }
}
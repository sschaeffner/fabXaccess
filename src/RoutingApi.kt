package cloud.fabx

import cloud.fabx.dto.*
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.api() {
    route("/api/v1") {
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

            patch("/{id}") {
                val user: UserDto? = call.parameters["id"]?.toInt()?.let {
                    userService.getUserById(it)
                }

                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editUser = call.receive<EditUserDto>()
                    userService.editUser(user.id, editUser)

                    call.respond(HttpStatusCode.OK)
                }
            }

            post("/{id}/qualifications") {
                val userQualification = call.receive<UserQualificationDto>()
                qualificationService.addUserQualification(userQualification.userId, userQualification.qualificationId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}/qualifications/{qualificationId}") {
                val userId = call.parameters["id"]!!.toInt()
                val qualificationId = call.parameters["qualificationId"]!!.toInt()

                qualificationService.removeUserQualification(userId, qualificationId)
                call.respond(HttpStatusCode.OK)
            }

            post("") {
                val user = call.receive<NewUserDto>()
                call.respond(userService.createNewUser(user))
            }
        }

        route("/qualification") {
            get("") {
                call.respond(qualificationService.getAllQualifications())
            }

            get("/{id}") {
                val qualification: QualificationDto? = call.parameters["id"]?.toInt()?.let {
                    qualificationService.getQualificationById(it)
                }
                if (qualification != null) {
                    call.respond(qualification)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch("/{id}") {
                val qualification: QualificationDto? = call.parameters["id"]?.toInt()?.let {
                    qualificationService.getQualificationById(it)
                }

                if (qualification == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editQualification = call.receive<EditQualificationDto>()
                    qualificationService.editQualification(qualification.id, editQualification)
                    call.respond(HttpStatusCode.OK)
                }
            }

            post("") {
                val qualification = call.receive<NewQualificationDto>()
                call.respond(qualificationService.createNewQualification(qualification))
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

            patch("/{id}") {
                val device: DeviceDto? = call.parameters["id"]?.toInt()?.let {
                    deviceService.getDeviceById(it)
                }

                if (device == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editDevice = call.receive<EditDeviceDto>()
                    deviceService.editDevice(device.id, editDevice)

                    call.respond(HttpStatusCode.OK)
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

            patch("/{id}") {
                val tool: ToolDto? = call.parameters["id"]?.toInt()?.let {
                    toolService.getToolById(it)
                }

                if (tool == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editTool = call.receive<EditToolDto>()
                    toolService.editTool(tool.id, editTool)

                    call.respond(HttpStatusCode.OK)
                }
            }

            post("") {
                val tool = call.receive<NewToolDto>()
                call.respond(toolService.createNewTool(tool))
            }
        }

        get("/info") {
            val principal = call.authentication.principal<UserIdPrincipal>()
            call.respond(InfoDto(principal!!.name))
        }
    }
}
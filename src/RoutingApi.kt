package cloud.fabx

import cloud.fabx.application.AdminPrincipal
import cloud.fabx.application.XPrincipal
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditDeviceDto
import cloud.fabx.dto.EditQualificationDto
import cloud.fabx.dto.EditToolDto
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.InfoDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.dto.NewQualificationDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.dto.UserDto
import cloud.fabx.dto.UserQualificationDto
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext

fun Route.api() {
    route("/api/v1") {
        route("/user") {
            get("") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                call.respond(userService.getAllUsers(admin))
            }

            get("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                val user: UserDto? = call.parameters["id"]?.toInt()?.let {
                    userService.getUserById(it, principal = admin)
                }

                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@patch
                }
                val user: UserDto? = call.parameters["id"]?.toInt()?.let {
                    userService.getUserById(it, principal = admin)
                }

                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editUser = call.receive<EditUserDto>()
                    userService.editUser(user.id, editUser, principal = admin)

                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@delete
                }
                call.parameters["id"]?.toInt()?.let {
                    try {
                        userService.deleteUser(it, principal = admin)
                        call.respond(HttpStatusCode.OK)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            post("/{id}/qualifications") {
                val admin = requireAdminPrincipalOrElse {
                    return@post
                }
                val userQualification = call.receive<UserQualificationDto>()
                qualificationService.addUserQualification(userQualification.userId, userQualification.qualificationId, principal = admin)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}/qualifications/{qualificationId}") {
                val admin = requireAdminPrincipalOrElse {
                    return@delete
                }

                val userId = call.parameters["id"]!!.toInt()
                val qualificationId = call.parameters["qualificationId"]!!.toInt()

                qualificationService.removeUserQualification(userId, qualificationId, principal = admin)
                call.respond(HttpStatusCode.OK)
            }

            post("") {
                val admin = requireAdminPrincipalOrElse {
                    return@post
                }
                val user = call.receive<NewUserDto>()
                call.respond(userService.createNewUser(user, principal = admin))
            }
        }

        route("/qualification") {
            get("") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                call.respond(qualificationService.getAllQualifications(principal = admin))
            }

            get("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                val qualification: QualificationDto? = call.parameters["id"]?.toInt()?.let {
                    qualificationService.getQualificationById(it, principal = admin)
                }
                if (qualification != null) {
                    call.respond(qualification)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@patch
                }
                val qualification: QualificationDto? = call.parameters["id"]?.toInt()?.let {
                    qualificationService.getQualificationById(it, principal = admin)
                }

                if (qualification == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editQualification = call.receive<EditQualificationDto>()
                    qualificationService.editQualification(qualification.id, editQualification, principal = admin)
                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@delete
                }

                call.parameters["id"]?.toInt()?.let {
                    try {
                        qualificationService.deleteQualification(it, principal = admin)
                        call.respond(HttpStatusCode.OK)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            post("") {
                val admin = requireAdminPrincipalOrElse {
                    return@post
                }
                val qualification = call.receive<NewQualificationDto>()
                call.respond(qualificationService.createNewQualification(qualification, principal = admin))
            }
        }

        route("/device") {
            get("") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                call.respond(deviceService.getAllDevices(principal = admin))
            }

            get("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                val device: DeviceDto? = call.parameters["id"]?.toInt()?.let {
                    deviceService.getDeviceById(it, principal = admin)
                }

                if (device != null) {
                    call.respond(device)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@patch
                }
                val device: DeviceDto? = call.parameters["id"]?.toInt()?.let {
                    deviceService.getDeviceById(it, principal = admin)
                }

                if (device == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editDevice = call.receive<EditDeviceDto>()
                    deviceService.editDevice(device.id, editDevice, principal = admin)

                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@delete
                }
                call.parameters["id"]?.toInt()?.let {
                    try {
                        deviceService.deleteDevice(it, principal = admin)
                        call.respond(HttpStatusCode.OK)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            post("") {
                val admin = requireAdminPrincipalOrElse {
                    return@post
                }
                val device = call.receive<NewDeviceDto>()
                call.respond(deviceService.createNewDevice(device, principal = admin))
            }
        }

        route("/tool") {
            get("") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }
                call.respond(toolService.getAllTools(principal = admin))
            }

            get("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@get
                }

                val tool: ToolDto? = call.parameters["id"]?.toInt()?.let {
                    toolService.getToolById(it, principal = admin)
                }

                if (tool != null) {
                    call.respond(tool)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@patch
                }

                val tool: ToolDto? = call.parameters["id"]?.toInt()?.let {
                    toolService.getToolById(it, principal = admin)
                }

                if (tool == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val editTool = call.receive<EditToolDto>()
                    toolService.editTool(tool.id, editTool, principal = admin)

                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/{id}") {
                val admin = requireAdminPrincipalOrElse {
                    return@delete
                }

                call.parameters["id"]?.toInt()?.let {
                    try {
                        toolService.deleteTool(it, principal = admin)
                        call.respond(HttpStatusCode.OK)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            post("") {
                val admin = requireAdminPrincipalOrElse {
                    return@post
                }
                val tool = call.receive<NewToolDto>()
                call.respond(toolService.createNewTool(tool, principal = admin))
            }
        }

        get("/info") {
            val admin = requireAdminPrincipalOrElse {
                return@get
            }
            call.respond(InfoDto(admin.name))
        }
    }
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.requireAdminPrincipalOrElse(
    onFailure: () -> Unit
): AdminPrincipal {
    val adminPrincipal = call.authentication.principal<AdminPrincipal>()
    if (adminPrincipal == null) {
        call.respond(HttpStatusCode.Forbidden, "Need to authenticate as admin")
        onFailure()
    }
    return adminPrincipal!!
}
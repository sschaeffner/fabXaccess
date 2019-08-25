package cloud.fabx

import cloud.fabx.dto.*
import io.ktor.application.call
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

            post("/{id}/permissions") {
                val userPermission = call.receive<UserPermissionDto>()
                permissionService.addUserPermission(userPermission.userId, userPermission.toolId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}/permissions/{toolId}") {
                val userId = call.parameters["id"]!!.toInt()
                val toolId = call.parameters["toolId"]!!.toInt()

                permissionService.removeUserPermission(userId, toolId)
                call.respond(HttpStatusCode.OK)
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
    }
}
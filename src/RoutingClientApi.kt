package cloud.fabx

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.clientApi() {
    route("/clientApi") {
        get("/{deviceId}/permissions/{cardId}") {
            val deviceId = call.parameters["deviceId"]!!.toInt()
            val cardId = call.parameters["cardId"]!!

            val toolsWithPermission = permissionService.getPermissionsForCardId(deviceId, cardId)

            call.respond(toolsWithPermission)
        }
    }
}
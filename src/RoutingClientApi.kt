package cloud.fabx

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.clientApi() {
    route("/clientApi/v1") {
        get("/{deviceMac}/permissions/{cardId}") {
            val deviceMac = call.parameters["deviceMac"]!!
            val cardId = call.parameters["cardId"]!!

            val qualifiedToolIds = qualificationService.getQualifiedToolsForCardId(deviceMac, cardId).map { it.id }

            val permissionsString = qualifiedToolIds.joinToString("\n")

            call.respond(permissionsString)
        }

        get("/{deviceMac}/config") {
            val deviceMac = call.parameters["deviceMac"]!!
            val device = deviceService.getDeviceByMac(deviceMac)

            if (device == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                var configString = "${device.name}\n"

                device.tools.forEach {
                    configString += "${it.id},${it.pin},${it.toolType},${it.name}\n"
                }

                call.respond(configString)
            }
        }
    }
}
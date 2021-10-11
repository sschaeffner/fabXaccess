package cloud.fabx

import cloud.fabx.application.DevicePrincipal
import cloud.fabx.dto.ToolDto
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext

fun Route.clientApi() {
    route("/clientApi") {
        route("/v1") {
            permission()
            config({ "${it.id},${it.pin},${it.toolType},${it.name}\n" })
        }
        route("/v2") {
            permission()
            permissionPhoneNr()
            config({ "${it.id},${it.pin},${it.toolType},${it.time},${it.idleState},${it.name}\n" })
        }
    }
}

private fun Route.permission() {
    get("/{deviceMac}/permissions/{cardId}/{cardSecret}") {
        val deviceMac = call.parameters["deviceMac"]!!
        val cardId = call.parameters["cardId"]!!
        val cardSecret = call.parameters["cardSecret"]!!

        val devicePrincipal = requireDevicePrincipalWithMacOrElse(deviceMac) {
            return@get
        }

        val qualifiedToolIds =
            qualificationService.getQualifiedToolsForCardId(cardId, cardSecret, devicePrincipal).map { it.id }

        val permissionsString = qualifiedToolIds.joinToString("\n")

        call.respond(permissionsString)
    }
}

private fun Route.permissionPhoneNr() {
    get("/{deviceMac}/permissions/{phoneNr}") {
        val deviceMac = call.parameters["deviceMac"]!!
        val phoneNr = call.parameters["phoneNr"]!!

        val devicePrincipal = requireDevicePrincipalWithMacOrElse(deviceMac) {
            return@get
        }

        val qualifiedToolIds =
            qualificationService.getQualifiedToolsForPhoneNr(phoneNr, devicePrincipal).map { it.id }

        val permissionsString = qualifiedToolIds.joinToString("\n")

        call.respond(permissionsString)
    }
}

private fun Route.config(formatter: (tool: ToolDto) -> String) {
    get("/{deviceMac}/config") {
        val deviceMac = call.parameters["deviceMac"]!!

        val devicePrincipal = requireDevicePrincipalWithMacOrElse(deviceMac) {
            return@get
        }

        val device = deviceService.getDeviceByMac(devicePrincipal.mac, principal = devicePrincipal)

        if (device == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            var configString = "${device.name}\n${device.bgImageUrl}\n${device.backupBackendUrl}\n"

            device.tools.forEach {
                configString += formatter(it)
            }

            call.respond(configString)
        }
    }
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.requireDevicePrincipalWithMacOrElse(
    mac: String, onFailure: () -> Unit
): DevicePrincipal {
    val devicePrincipal = call.authentication.principal<DevicePrincipal>()
    if (devicePrincipal == null) {
        call.respond(HttpStatusCode.Forbidden, "Need to authenticate as device")
        onFailure()
    }
    if (devicePrincipal?.mac != mac) {
        call.respond(HttpStatusCode.Forbidden, "Given mac has to match authentication")
        onFailure()
    }
    return devicePrincipal!!
}
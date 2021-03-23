package cloud.fabx.service

import cloud.fabx.application.AdminPrincipal
import cloud.fabx.application.DevicePrincipal
import cloud.fabx.application.NewDevicePrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.model.Admin
import cloud.fabx.model.Admins
import cloud.fabx.model.Device
import cloud.fabx.model.Devices
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getDigestFunction
import java.util.Base64
import org.jetbrains.exposed.sql.and

@KtorExperimentalAPI
class AuthenticationService(private val deviceService: DeviceService) {

    private val digestFunction = getDigestFunction("SHA-256") { "fabXfabXfabX${it.length}" }

    suspend fun checkAdminCredentials(username: String, password: String): AdminPrincipal? {
        val hash = digestFunction.invoke(password)
        val encodedHash = Base64.getEncoder().encodeToString(hash)

        return findAdmin(username, encodedHash)?.let {
            AdminPrincipal(it.name)
        }
    }

    private suspend fun findAdmin(username: String, encodedHash: String): Admin? = dbQuery {
        Admin.find {
            (Admins.name eq username) and
            (Admins.passwordHash eq encodedHash)
        }.firstOrNull()
    }

    suspend fun checkDeviceCredentials(mac: String, secret: String): DevicePrincipal? {
        val device = findDevice(mac)

        return if (device != null) {
            if (device.secret == secret) {
                DevicePrincipal(mac)
            } else {
                null
            }
        } else {
            val newDeviceDto = deviceService.createNewDevice(
                NewDeviceDto(
                    "new device $mac",
                    mac,
                    secret,
                    "",
                    ""
                ),
                NewDevicePrincipal(mac)
            )

            DevicePrincipal(newDeviceDto.mac)
        }
    }

    private suspend fun findDevice(mac: String): Device? = dbQuery {
        Device.find {
            Devices.mac eq mac
        }.firstOrNull()
    }
}
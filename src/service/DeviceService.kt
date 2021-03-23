package cloud.fabx.service

import cloud.fabx.application.AuthorizationException
import cloud.fabx.application.XPrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.domainEvent
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditDeviceDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.logger
import cloud.fabx.model.Device
import cloud.fabx.model.Devices
import cloud.fabx.model.Mapper
import net.logstash.logback.argument.StructuredArguments.keyValue

class DeviceService(private val mapper: Mapper) {

    private val log = logger()

    suspend fun getAllDevices(principal: XPrincipal): List<DeviceDto> = dbQuery {
        principal.requirePermission("get all devices", XPrincipal::allowedToGetAllDevices)
        Device.all().map{ mapper.toDeviceDto(it) }.toCollection(ArrayList())
    }

    suspend fun getDeviceById(id: Int, principal: XPrincipal): DeviceDto? = dbQuery {
        principal.requirePermission("get device by id", XPrincipal::allowedToGetDevice)
        Device.findById(id)?.let { mapper.toDeviceDto(it) }
    }

    suspend fun getDeviceByMac(mac: String, principal: XPrincipal): DeviceDto? = dbQuery {
        principal.requirePermission("get device by mac", XPrincipal::allowedToGetDevice)
        Device.find { Devices.mac eq mac }.firstOrNull()?.let { mapper.toDeviceDto(it) }
    }

    suspend fun createNewDevice(device: NewDeviceDto, principal: XPrincipal): DeviceDto = dbQuery {
        principal.requirePermission("create new device", XPrincipal::allowedToCreateNewDevice)

        val newDevice = Device.new {
            name = device.name
            mac = device.mac
            secret = device.secret
            bgImageUrl = device.bgImageUrl
            backupBackendUrl = device.backupBackendUrl
        }

        val deviceDto = mapper.toDeviceDto(newDevice)

        log.domainEvent(
            "new device: {} by {}",
            keyValue("deviceDto", deviceDto),
            keyValue("principal", principal)
        )
        deviceDto
    }

    suspend fun editDevice(id: Int, editDevice: EditDeviceDto, principal: XPrincipal) = dbQuery {
        principal.requirePermission("edit device", XPrincipal::allowedToEditDevice)

        val device = Device.findById(id)
        requireNotNull(device) { "Device with id $id does not exist" }

        editDevice.name?.let { device.name = it }
        editDevice.mac?.let { device.mac = it }
        editDevice.secret?.let { device.secret = it }
        editDevice.bgImageUrl?.let { device.bgImageUrl = it }
        editDevice.backupBackendUrl?.let { device.backupBackendUrl = it }

        log.domainEvent(
            "edit device: {} by {}",
            keyValue("deviceDto", mapper.toDeviceDto(device)),
            keyValue("principal", principal)
        )
    }

    suspend fun deleteDevice(id: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("delete device", XPrincipal::allowedToDeleteDevice)

        val device = Device.findById(id)
        requireNotNull(device) { "Device with id $id does not exist" }

        log.domainEvent(
            "delete device: {} by {}",
            keyValue("deviceDto", mapper.toDeviceDto(device)),
            keyValue("principal", principal)
        )
        device.delete()
    }

    private fun XPrincipal.requirePermission(description: String, permission: XPrincipal.() -> Boolean) {
        if (!this.permission()) {
            log.info("$name tried to $description")
            throw AuthorizationException("$name not allowed to $description.")
        }
    }
}
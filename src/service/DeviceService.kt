package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.model.Device

class DeviceService {

    private val toolService = ToolService()

    suspend fun getAllDevices(): List<DeviceDto> = dbQuery {
        Device.all().map{ toDeviceDto(it) }.toCollection(ArrayList())
    }

    suspend fun getDeviceById(id: Int): DeviceDto? = dbQuery {
        Device.findById(id)?.let { toDeviceDto(it) }
    }

    suspend fun createNewDevice(device: NewDeviceDto): DeviceDto = dbQuery {
        val newDevice = Device.new {
            name = device.name
            mac = device.mac
            secret = device.secret
            bgImageUrl = device.bgImageUrl
        }

        toDeviceDto(newDevice)
    }

    private fun toDeviceDto(device: Device): DeviceDto {
        return DeviceDto(
            device.id.value,
            device.name,
            device.mac,
            device.secret,
            device.bgImageUrl,
            device.tools.map { toolService.toToolDto(it) }.toCollection(ArrayList())
        )
    }
}
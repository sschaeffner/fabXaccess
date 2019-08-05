package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.model.Device

class DeviceService {

    suspend fun getAllDevices(): List<DeviceDto> = dbQuery {
        Device.all().map{ toDeviceDto(it) }.toCollection(ArrayList())
    }

    suspend fun getDeviceById(id: Int): DeviceDto? = dbQuery {
        Device.findById(id)?.let { toDeviceDto(it) }
    }

    suspend fun createNewDevice(device: NewDeviceDto): DeviceDto {
        val deviceInDb = dbQuery {
            Device.new {
                name = device.name
                mac = device.mac
                secret = device.secret
                bgImageUrl = device.bgImageUrl
            }
        }

        return toDeviceDto(deviceInDb)
    }

    private fun toDeviceDto(device: Device): DeviceDto {
        return DeviceDto(
            device.id.value,
            device.name,
            device.mac,
            device.secret,
            device.bgImageUrl
        )
    }
}
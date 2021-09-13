package cloud.fabx.model

import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.dto.UserDto

class Mapper {
    fun toDeviceDto(device: Device): DeviceDto {
        return DeviceDto(
            device.id.value,
            device.name,
            device.mac,
            device.secret,
            device.bgImageUrl,
            device.backupBackendUrl,
            device.tools.map { toToolDto(it) }.toCollection(ArrayList())
        )
    }

    fun toQualificationDto(qualification: Qualification): QualificationDto {
        return QualificationDto(
            qualification.id.value,
            qualification.name,
            qualification.description,
            qualification.colour,
            qualification.orderNr
        )
    }

    fun toToolDto(tool: Tool): ToolDto {
        return ToolDto(
            tool.id.value,
            tool.device.id.value,
            tool.name,
            tool.pin,
            tool.toolType,
            tool.time,
            tool.idleState,
            tool.toolState,
            tool.wikiLink,
            tool.qualifications.map { toQualificationDto(it) }
        )
    }

    fun toUserDto(user: User): UserDto {
        return UserDto(
            user.id.value,
            user.firstName,
            user.lastName,
            user.wikiName,
            user.phoneNumber ?: "",
            user.locked,
            user.lockedReason,
            user.cardId,
            user.cardSecret,
            user.qualifications.map { toQualificationDto(it) }.toCollection(ArrayList())
        )
    }
}
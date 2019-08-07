package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.model.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class PermissionService {
    private val deviceService = DeviceService()

    suspend fun addUserPermission(userId: Int, toolId: Int) = dbQuery {
        val user = User.findById(userId)
        val tool = Tool.findById(toolId)

        if (user == null) throw IllegalArgumentException("User with id $userId does not exist")
        if (tool == null) throw IllegalArgumentException("Tool with id $toolId does not exist")

        val newPermissions = user.permissions.toCollection(ArrayList())
        newPermissions.add(tool)

        user.permissions = SizedCollection(newPermissions)
    }

    suspend fun removeUserPermission(userId: Int, toolId: Int) = dbQuery {
        val user = User.findById(userId)
        val tool = Tool.findById(toolId)

        if (user == null) throw IllegalArgumentException("User with id $userId does not exist")
        if (tool == null) throw IllegalArgumentException("Tool with id $toolId does not exist")

        val newPermissions = user.permissions.toCollection(ArrayList())
        val success = newPermissions.remove(tool)

        if (!success) throw IllegalArgumentException("User never head permission for tool ${tool.id}/${tool.name}")

        user.permissions = SizedCollection(newPermissions)
    }

    suspend fun getDevicePermissionsForCardId(deviceMac: String, cardId: String): List<Int> = dbQuery {
        val device = Device.find { Devices.mac eq deviceMac }.firstOrNull()
                        ?.let { deviceService.toDeviceDto(it) }
                        ?: throw IllegalArgumentException("Device with mac $deviceMac does not exist")

        val user = User.find { Users.cardId eq cardId }.firstOrNull() ?: throw IllegalArgumentException("User with card id $cardId does not exist")

        (Tools innerJoin UserPermissions).select {
            (UserPermissions.tool eq Tools.id) and //join condition
            (Tools.device eq device.id) and
            (UserPermissions.user eq user.id.value)
        }.map { it[Tools.id].value }.toCollection(ArrayList())
    }
}
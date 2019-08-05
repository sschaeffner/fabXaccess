package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.model.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class PermissionService {
    suspend fun getPermissionsForCardId(deviceId: Int, cardId: String): List<Int> = dbQuery {
        val device = Device.findById(deviceId) ?: throw IllegalArgumentException("Device with id $deviceId does not exist")

        val user = User.find { Users.cardId eq cardId }.firstOrNull() ?: throw IllegalArgumentException("User with card id $cardId does not exist")

        (Tools innerJoin UserPermissions).select {
            (UserPermissions.tool eq Tools.id) and //join condition
            (Tools.device eq device.id) and
            (UserPermissions.user eq user.id.value)
        }.map { it[Tools.id].value }.toCollection(ArrayList())
    }
}
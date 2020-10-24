package cloud.fabx.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Devices: IntIdTable() {
    val name = varchar("name", 64)
    val mac = varchar("mac", 64).uniqueIndex("deviceMacUniqueIndex")
    val secret = varchar("secret", 64)
    val bgImageUrl = varchar("bgImageUrl", 256)
    val backupBackendUrl = varchar("backupBackendUrl", 256)
}

class Device(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Device>(Devices)

    var name by Devices.name
    var mac by Devices.mac
    var secret by Devices.secret
    var bgImageUrl by Devices.bgImageUrl
    var backupBackendUrl by Devices.backupBackendUrl

    val tools by Tool referrersOn Tools.device
}
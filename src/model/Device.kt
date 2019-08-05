package cloud.fabx.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Devices: IntIdTable() {
    val name = varchar("name", 64)
    val mac = varchar("mac", 64)
    val secret = varchar("secret", 64)
    val bgImageUrl = varchar("bgImageUrl", 256)
}

class Device(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Device>(Devices)

    var name by Devices.name
    var mac by Devices.mac
    var secret by Devices.secret
    var bgImageUrl by Devices.bgImageUrl

    val tools by Tool referrersOn Tools.device
}
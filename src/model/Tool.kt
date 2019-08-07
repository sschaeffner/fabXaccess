package cloud.fabx.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Tools: IntIdTable() {
    val name = varchar("name", 64)
    val pin = integer("pin")
    val toolType = enumeration("toolType", ToolType::class)
    val toolState = enumeration("toolState", ToolState::class)
    val wikiLink = varchar("wikiLink", 256)

    val device = reference("device", Devices)

    init {
        index("ToolDevicePinUniqueIndex", true, device, pin)
    }
}

class Tool(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Tool>(Tools)

    var name by Tools.name
    var pin by Tools.pin
    var toolType by Tools.toolType
    var toolState by Tools.toolState
    var wikiLink by Tools.wikiLink

    var device by Device referencedOn Tools.device
}
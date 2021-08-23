package cloud.fabx.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Tools : IntIdTable() {
    val name = varchar("name", 64)
    val pin = integer("pin")
    val toolType = enumeration("toolType", ToolType::class)
    val time = integer("time") // keep or unlock time in milliseconds
    val toolState = enumeration("toolState", ToolState::class)
    val wikiLink = varchar("wikiLink", 256)

    val device = reference("device", Devices)

    init {
        index("ToolDevicePinUniqueIndex", true, device, pin)
    }
}

class Tool(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Tool>(Tools)

    var name by Tools.name
    var pin by Tools.pin
    var toolType by Tools.toolType
    var time by Tools.time
    var toolState by Tools.toolState
    var wikiLink by Tools.wikiLink

    var device by Device referencedOn Tools.device

    var qualifications by Qualification via ToolQualifications
}
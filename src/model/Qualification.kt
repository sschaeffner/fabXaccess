package cloud.fabx.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Qualifications: IntIdTable() {
    val name = varchar("name", 64)
    val description = varchar("description", 256)
    val colour = varchar("colour", 8)
    val orderNr = integer("orderNr")
}

class Qualification(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Qualification>(Qualifications)

    var name by Qualifications.name
    var description by Qualifications.description
    var colour by Qualifications.colour
    var orderNr by Qualifications.orderNr
}
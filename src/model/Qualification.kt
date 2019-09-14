package cloud.fabx.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Qualifications: IntIdTable() {
    val name = varchar("name", 64)
    val description = varchar("description", 256)
}

class Qualification(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Qualification>(Qualifications)

    var name by Qualifications.name
    var description by Qualifications.description
}
package cloud.fabx.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Admins: IntIdTable() {
    val name = varchar("name", length = 64)
    val passwordHash = varchar("password", length = 64)
}

class Admin(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Admin>(Admins)

    var name by Admins.name
    var passwordHash by Admins.passwordHash
}
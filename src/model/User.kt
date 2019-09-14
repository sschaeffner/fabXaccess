package cloud.fabx.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Users: IntIdTable() {
    val name = varchar("name", 64)
    val wikiName = varchar("wikiName", 64)
    val phoneNumber = varchar("phoneNumber", 64).uniqueIndex("phoneNumberUniqueIndex")
    val locked = bool("locked")
    val lockedReason = varchar("lockedReason", 256)
    val cardId = varchar("cardId", 16).uniqueIndex("cardIdUniqueIndex")
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var wikiName by Users.wikiName
    var phoneNumber by Users.phoneNumber
    var locked by Users.locked
    var lockedReason by Users.lockedReason
    var cardId by Users.cardId

    var qualifications by Qualification via UserQualifications
}
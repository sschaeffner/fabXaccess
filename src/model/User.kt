package cloud.fabx.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

object Users: org.jetbrains.exposed.dao.id.IntIdTable() {
    val firstName = varchar("firstName", 64)
    val lastName = varchar("lastName", 64)
    val wikiName = varchar("wikiName", 64)
    val phoneNumber = varchar("phoneNumber", 64).uniqueIndex("phoneNumberUniqueIndex")
    val locked = bool("locked")
    val lockedReason = varchar("lockedReason", 256)
    val cardId = varchar("cardId", 16).nullable().uniqueIndex("cardIdUniqueIndex")
    val cardSecret = varchar("cardSecret", 128).nullable()
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var wikiName by Users.wikiName
    var phoneNumber by Users.phoneNumber
    var locked by Users.locked
    var lockedReason by Users.lockedReason
    var cardId by Users.cardId
    var cardSecret by Users.cardSecret

    var qualifications by Qualification via UserQualifications
}
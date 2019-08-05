package cloud.fabx.dto

data class EditUserDto (
    val name: String?,
    val wikiName: String?,
    val phoneNumber: String?,
    val locked: Boolean?,
    val lockedReason: String?,
    val cardId: String?
)
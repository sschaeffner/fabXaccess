package cloud.fabx.dto

data class EditUserDto(
    val firstName: String?,
    val lastName: String?,
    val wikiName: String?,
    val phoneNumber: String?,
    val locked: Boolean?,
    val lockedReason: String?,
    val cardId: String?,
    val cardSecret: String?
)
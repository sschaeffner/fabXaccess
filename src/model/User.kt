package cloud.fabx.model

data class User (
    val id: Int,
    val name: String,
    val wikiName: String,
    val phoneNumber: String,
    val locked: Boolean,
    val lockedReason: String?,
    val cardId: String?
)
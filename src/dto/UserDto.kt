package cloud.fabx.dto

data class UserDto(
    val id: Int,
    val name: String,
    val wikiName: String,
    val phoneNumber: String,
    val locked: Boolean,
    val lockedReason: String,
    val cardId: String,

    val qualifications: List<QualificationDto>
)
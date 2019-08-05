package cloud.fabx.dto

import cloud.fabx.model.Tool

data class UserDto(
    val id: Int,
    val name: String,
    val wikiName: String,
    val phoneNumer: String,
    val locked: Boolean,
    val lockedReason: String?,
    val cardId: String,
    val permissions: List<ToolDto>
)
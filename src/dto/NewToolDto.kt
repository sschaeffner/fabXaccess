package cloud.fabx.dto

import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType

data class NewToolDto(
    val deviceId: Int,
    val name: String,
    val pin: Int,
    val toolType: ToolType,
    val toolState: ToolState,
    val wikiLink: String,
    val qualifications: List<Int>
)
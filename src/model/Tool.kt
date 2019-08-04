package cloud.fabx.model

data class Tool(
    val id: Int,
    val deviceId: Int,
    val pin: Int,
    val toolType: ToolType,
    val name: String,
    val toolState: ToolState,
    val wikiLink: String
)

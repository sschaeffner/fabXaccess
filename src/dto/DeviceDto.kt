package cloud.fabx.dto

data class DeviceDto (
    val id: Int,
    val name: String,
    val mac: String,
    val secret: String,
    val bgImageUrl: String,
    val backupBackendUrl: String,

    val tools: List<ToolDto>
)
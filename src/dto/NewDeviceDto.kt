package cloud.fabx.dto

data class NewDeviceDto(
    val name: String,
    val mac: String,
    val secret: String,
    val bgImageUrl: String,
    val backupBackendUrl: String
)
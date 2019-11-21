package cloud.fabx.dto

data class EditDeviceDto(
    val name: String?,
    val mac: String?,
    val secret: String?,
    val bgImageUrl: String?,
    val backupBackendUrl: String?
)
package cloud.fabx.model

data class Device (
    val id: Int,
    val name: String,
    val mac: String,
    val secret: String
)
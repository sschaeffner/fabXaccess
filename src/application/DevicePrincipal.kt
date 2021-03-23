package cloud.fabx.application

data class DevicePrincipal(val mac: String) : XPrincipal {
    override val name: String
        get() = mac

    override fun allowedToGetDevice(): Boolean = true
    override fun allowedToCreateNewDevice(): Boolean = true
}
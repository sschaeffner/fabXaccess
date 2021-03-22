package cloud.fabx.application

class NewDevicePrincipal(override val name: String) : XPrincipal {
    override fun allowedToCreateNewDevice(): Boolean = true
}
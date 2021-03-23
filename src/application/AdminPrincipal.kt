package cloud.fabx.application

data class AdminPrincipal(override val name: String) : XPrincipal {

    // user
    override fun allowedToGetAllUsers(): Boolean = true
    override fun allowedToGetUser(): Boolean = true
    override fun allowedToCreateNewUser(): Boolean = true
    override fun allowedToEditUser(): Boolean = true
    override fun allowedToDeleteUser(): Boolean = true

    // device
    override fun allowedToGetAllDevices(): Boolean = true
    override fun allowedToGetDevice(): Boolean = true
    override fun allowedToCreateNewDevice(): Boolean = true
    override fun allowedToEditDevice(): Boolean = true
    override fun allowedToDeleteDevice(): Boolean = true

    // tools
    override fun allowedToGetAllTools(): Boolean = true
    override fun allowedToGetTool(): Boolean = true
    override fun allowedToCreateTool(): Boolean = true
    override fun allowedToEditTool(): Boolean = true
    override fun allowedToDeleteTool(): Boolean = true

    // qualification
    override fun allowedToGetAllQualifications(): Boolean = true
    override fun allowedToGetQualification(): Boolean = true
    override fun allowedToCreateQualification(): Boolean = true
    override fun allowedToEditQualification(): Boolean = true
    override fun allowedToDeleteQualification(): Boolean = true
    override fun allowedToAddQualificationToUser(): Boolean = true
    override fun allowedToRemoveQualificationFromUser(): Boolean = true
}


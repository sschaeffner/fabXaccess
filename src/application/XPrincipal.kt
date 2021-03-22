package cloud.fabx.application

import io.ktor.auth.Principal

interface XPrincipal: Principal {
    val name: String

    // user
    fun allowedToGetAllUsers(): Boolean = false
    fun allowedToGetUser(): Boolean = false
    fun allowedToCreateNewUser(): Boolean = false
    fun allowedToEditUser(): Boolean = false
    fun allowedToDeleteUser(): Boolean = false

    // device
    fun allowedToGetAllDevices(): Boolean = false
    fun allowedToGetDevice(): Boolean = false
    fun allowedToCreateNewDevice(): Boolean = false
    fun allowedToEditDevice(): Boolean = false
    fun allowedToDeleteDevice(): Boolean = false

    // tools
    fun allowedToGetAllTools(): Boolean = false
    fun allowedToGetTool(): Boolean = false
    fun allowedToCreateTool(): Boolean = false
    fun allowedToEditTool(): Boolean = false
    fun allowedToDeleteTool(): Boolean = false

    // qualification
    fun allowedToGetAllQualifications(): Boolean = false
    fun allowedToGetQualification(): Boolean = false
    fun allowedToCreateQualification(): Boolean = false
    fun allowedToEditQualification(): Boolean = false
    fun allowedToDeleteQualification(): Boolean = false
    fun allowedToAddQualificationToUser(): Boolean = false
    fun allowedToRemoveQualificationFromUser(): Boolean = false
}
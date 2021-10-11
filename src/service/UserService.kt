package cloud.fabx.service

import cloud.fabx.application.AuthorizationException
import cloud.fabx.application.XPrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.domainEvent
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import cloud.fabx.logger
import cloud.fabx.model.Mapper
import cloud.fabx.model.User
import cloud.fabx.model.ValidationException
import net.logstash.logback.argument.StructuredArguments

class UserService(private val mapper: Mapper) {

    private val log = logger()

    suspend fun getAllUsers(principal: XPrincipal): List<UserDto> = dbQuery {
        principal.requirePermission("get all users", XPrincipal::allowedToGetAllUsers)
        User.all().map { mapper.toUserDto(it) }.toCollection(ArrayList())
    }

    suspend fun getUserById(id: Int, principal: XPrincipal): UserDto? = dbQuery {
        principal.requirePermission("get user by id", XPrincipal::allowedToGetUser)
        User.findById(id)?.let { mapper.toUserDto(it) }
    }

    suspend fun createNewUser(user: NewUserDto, principal: XPrincipal): UserDto = dbQuery {
        principal.requirePermission("create new user", XPrincipal::allowedToCreateNewUser)

        if (user.phoneNumber.isNotBlank()) {
            validatePhoneNumber(user.phoneNumber)
        }

        val newUser = User.new {
            firstName = user.firstName
            lastName = user.lastName
            wikiName = user.wikiName
            phoneNumber = user.phoneNumber.ifBlank { null }
            locked = false
            lockedReason = ""
        }

        val userDto = mapper.toUserDto(newUser)
        log.domainEvent(
            "new user: {} by {}",
            StructuredArguments.keyValue("userDto", userDto),
            StructuredArguments.keyValue("principal", principal.name)
        )
        userDto
    }

    suspend fun editUser(id: Int, editUser: EditUserDto, principal: XPrincipal) = dbQuery {
        principal.requirePermission("edit user", XPrincipal::allowedToEditUser)

        val user = User.findById(id)
        requireNotNull(user) { "User with id $id does not exist" }

        editUser.firstName?.let { user.firstName = it }
        editUser.lastName?.let { user.lastName = it }
        editUser.wikiName?.let { user.wikiName = it }
        editUser.phoneNumber?.let {
            if (it.isNotBlank()) {
                validatePhoneNumber(it)
                user.phoneNumber = it
            } else {
                user.phoneNumber = null
            }
        }
        editUser.locked?.let { user.locked = it }
        editUser.lockedReason?.let { user.lockedReason = it }
        editUser.cardId?.let { user.cardId = it }
        editUser.cardSecret?.let { user.cardSecret = it }

        log.domainEvent(
            "edit user: {} by {}",
            StructuredArguments.keyValue("userDto", mapper.toUserDto(user)),
            StructuredArguments.keyValue("principal", principal.name)
        )
    }

    suspend fun deleteUser(id: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("delete user", XPrincipal::allowedToDeleteUser)

        val user = User.findById(id)
        requireNotNull(user) { "User with id $id does not exist" }

        log.domainEvent(
            "delete user: {} by {}",
            StructuredArguments.keyValue("userDto", mapper.toUserDto(user)),
            StructuredArguments.keyValue("principal", principal.name)
        )
        user.delete()
    }

    private fun XPrincipal.requirePermission(description: String, permission: XPrincipal.() -> Boolean) {
        if (!this.permission()) {
            log.info("$name tried to $description")
            throw AuthorizationException("$name not allowed to $description.")
        }
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        if (!phoneNumber.startsWith("+")) {
            throw ValidationException("Phone number has to be entered with + prefix.")
        }
        if (phoneNumber.trim() != phoneNumber) {
            throw ValidationException("Phone number must not contain whitespace.")
        }
        if (!phoneNumber.matches(Regex("\\+[0-9]+"))) {
            throw ValidationException("Phone number must match \"\\+[0-9]+\".")
        }
    }
}
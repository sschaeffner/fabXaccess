package cloud.fabx.service

import cloud.fabx.application.AuthorizationException
import cloud.fabx.application.XPrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.domainEvent
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import cloud.fabx.logger
import cloud.fabx.model.User
import net.logstash.logback.argument.StructuredArguments

class UserService {

    private val log = logger()

    private val qualificationService = QualificationService()

    suspend fun getAllUsers(principal: XPrincipal): List<UserDto> = dbQuery {
        principal.requirePermission("get all users", XPrincipal::allowedToGetAllUsers)
        User.all().map{ toUserDto(it) }.toCollection(ArrayList())
    }

    suspend fun getUserById(id: Int, principal: XPrincipal): UserDto? = dbQuery {
        principal.requirePermission("get user by id", XPrincipal::allowedToGetUser)
        User.findById(id)?.let { toUserDto(it) }
    }

    suspend fun createNewUser(user: NewUserDto, principal: XPrincipal): UserDto = dbQuery {
        principal.requirePermission("create new user", XPrincipal::allowedToCreateNewUser)

        val newUser = User.new {
            firstName = user.firstName
            lastName = user.lastName
            wikiName = user.wikiName
            phoneNumber = user.phoneNumber
            locked = false
            lockedReason = ""
        }

        val userDto = toUserDto(newUser)
        log.domainEvent(
            "new user: {} by {}",
            StructuredArguments.keyValue("userDto", userDto),
            StructuredArguments.keyValue("principal", principal)
        )
        userDto
    }

    suspend fun editUser(id: Int, editUser: EditUserDto, principal: XPrincipal) = dbQuery {
        principal.requirePermission("edit user", XPrincipal::allowedToEditUser)

        val user = User.findById(id) ?: throw IllegalArgumentException("User with id $id does not exist")

        editUser.firstName?.let { user.firstName = it }
        editUser.lastName?.let { user.lastName = it }
        editUser.wikiName?.let { user.wikiName = it }
        editUser.phoneNumber?.let { user.phoneNumber = it }
        editUser.locked?.let { user.locked = it }
        editUser.lockedReason?.let { user.lockedReason = it }
        editUser.cardId?.let { user.cardId = it }
        editUser.cardSecret?.let { user.cardSecret = it }

        log.domainEvent(
            "edit user: {} by {}",
            StructuredArguments.keyValue("userDto", toUserDto(user)),
            StructuredArguments.keyValue("principal", principal)
        )
    }

    suspend fun deleteUser(id: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("delete user", XPrincipal::allowedToDeleteUser)
        val user = User.findById(id) ?: throw IllegalArgumentException("User with id $id does not exist")
        log.domainEvent(
            "delete user: {} by {}",
            StructuredArguments.keyValue("userDto", toUserDto(user)),
            StructuredArguments.keyValue("principal", principal)
        )
        user.delete()
    }

    private fun toUserDto(user: User): UserDto {
        return UserDto(
            user.id.value,
            user.firstName,
            user.lastName,
            user.wikiName,
            user.phoneNumber,
            user.locked,
            user.lockedReason,
            user.cardId,
            user.cardSecret,
            user.qualifications.map { qualificationService.toQualificationDto(it) }.toCollection(ArrayList())
        )
    }

    private fun XPrincipal.requirePermission(description: String, permission: XPrincipal.() -> Boolean) {
        if (!this.permission()) {
            log.info("$name tried to $description")
            throw AuthorizationException("$name not allowed to $description.")
        }
    }
}
package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import cloud.fabx.model.Tool
import cloud.fabx.model.User
import org.jetbrains.exposed.sql.SizedCollection

class UserService {

    private val qualificationService = QualificationService()

    suspend fun getAllUsers(): List<UserDto> = dbQuery {
        User.all().map{ toUserDto(it) }.toCollection(ArrayList())
    }

    suspend fun getUserById(id: Int): UserDto? = dbQuery {
        User.findById(id)?.let { toUserDto(it) }
    }

    suspend fun createNewUser(user: NewUserDto): UserDto = dbQuery {
        val newUser = User.new {
            name = user.name
            wikiName = user.wikiName
            phoneNumber = user.phoneNumber
            locked = false
            lockedReason = ""
        }

        toUserDto(newUser)
    }

    suspend fun editUser(id: Int, editUser: EditUserDto) = dbQuery {
        val user = User.findById(id) ?: throw IllegalArgumentException("User with id $id does not exist")

        editUser.name?.let { user.name = it }
        editUser.wikiName?.let { user.wikiName = it }
        editUser.phoneNumber?.let { user.phoneNumber = it }
        editUser.locked?.let { user.locked = it }
        editUser.lockedReason?.let { user.lockedReason = it }
        editUser.cardId?.let { user.cardId = it }
    }

    private fun toUserDto(user: User): UserDto {
        return UserDto(
            user.id.value,
            user.name,
            user.wikiName,
            user.phoneNumber,
            user.locked,
            user.lockedReason,
            user.cardId,
            user.qualifications.map { qualificationService.toQualificationDto(it) }.toCollection(ArrayList())
        )
    }
}
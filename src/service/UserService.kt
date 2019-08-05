package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import cloud.fabx.model.User

class UserService {

    suspend fun getAllUsers(): List<UserDto> = dbQuery {
        User.all().map{ toUserDto(it) }.toCollection(ArrayList())
    }

    suspend fun getUserById(id: Int): UserDto? = dbQuery {
        User.findById(id)?.let { toUserDto(it) }
    }

    suspend fun createNewUser(user: NewUserDto): UserDto {
        val userInDb = dbQuery {
            User.new {
                name = user.name
                wikiName = user.wikiName
                phoneNumber = user.phoneNumber
                locked = false
                lockedReason = ""
                cardId = user.cardId
            }
        }

        return toUserDto(userInDb)
    }

    private fun toUserDto(user: User): UserDto {
        return UserDto(
            user.id.value,
            user.name,
            user.wikiName,
            user.phoneNumber,
            user.locked,
            user.lockedReason,
            user.cardId
        )
    }
}
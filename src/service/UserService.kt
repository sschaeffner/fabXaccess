package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import cloud.fabx.model.Tool
import cloud.fabx.model.User
import org.jetbrains.exposed.sql.SizedCollection

class UserService {

    private val toolService = ToolService()

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
            cardId = user.cardId
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

    suspend fun addUserPermission(userId: Int, toolId: Int) = dbQuery {
        val user = User.findById(userId)
        val tool = Tool.findById(toolId)

        user?.let { userIt ->
            tool?.let { toolIt ->
                val newPermissions = userIt.permissions.toCollection(ArrayList())
                newPermissions.add(toolIt)

                user.permissions = SizedCollection(newPermissions)
            }
        }
    }

    suspend fun removeUserPermission(userId: Int, toolId: Int) = dbQuery {
        val user = User.findById(userId)
        val tool = Tool.findById(toolId)

        user?.let { userIt ->
            tool?.let { toolIt ->
                val newPermissions = userIt.permissions.toCollection(ArrayList())
                val success = newPermissions.remove(toolIt)

                if (!success) throw IllegalArgumentException("User never head permission for tool ${toolIt.id}/${toolIt.name}")

                user.permissions = SizedCollection(newPermissions)
            }
        }
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
            user.permissions.map { toolService.toToolDto(it) }.toCollection(ArrayList())
        )
    }
}
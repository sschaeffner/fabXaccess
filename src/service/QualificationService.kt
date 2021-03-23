package cloud.fabx.service

import cloud.fabx.application.AuthorizationException
import cloud.fabx.application.DevicePrincipal
import cloud.fabx.application.XPrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.domainEvent
import cloud.fabx.dto.EditQualificationDto
import cloud.fabx.dto.NewQualificationDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.logger
import cloud.fabx.model.Device
import cloud.fabx.model.Devices
import cloud.fabx.model.Mapper
import cloud.fabx.model.Qualification
import cloud.fabx.model.User
import cloud.fabx.model.Users
import net.logstash.logback.argument.StructuredArguments
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and

class QualificationService(private val mapper: Mapper) {

    private val log = logger()

    suspend fun getAllQualifications(principal: XPrincipal): List<QualificationDto> = dbQuery {
        principal.requirePermission("get all qualifications", XPrincipal::allowedToGetAllQualifications)
        Qualification.all().map { mapper.toQualificationDto(it) }.toCollection(ArrayList())
    }

    suspend fun getQualificationById(id: Int, principal: XPrincipal): QualificationDto? = dbQuery {
        principal.requirePermission("get qualification by id", XPrincipal::allowedToGetQualification)
        Qualification.findById(id)?.let { mapper.toQualificationDto(it) }
    }

    suspend fun createNewQualification(qualification: NewQualificationDto, principal: XPrincipal) = dbQuery {
        principal.requirePermission("create new qualification", XPrincipal::allowedToCreateQualification)

        val newQualification = Qualification.new {
            name = qualification.name
            description = qualification.description
            colour = qualification.colour
            orderNr = qualification.orderNr
        }

        val qualificationDto = mapper.toQualificationDto(newQualification)
        log.domainEvent(
            "new qualification: {} by {}",
            StructuredArguments.keyValue("qualificationDto", qualificationDto),
            StructuredArguments.keyValue("principal", principal)
        )
        qualificationDto
    }

    suspend fun editQualification(id: Int, editQualification: EditQualificationDto, principal: XPrincipal) = dbQuery {
        principal.requirePermission("edit qualification", XPrincipal::allowedToEditQualification)

        val qualification = Qualification.findById(id)
        requireNotNull(qualification) { "Qualification with id $id does not exist" }

        editQualification.name?.let { qualification.name = it }
        editQualification.description?.let { qualification.description = it }
        editQualification.colour?.let { qualification.colour = it }
        editQualification.orderNr?.let { qualification.orderNr = it }

        log.domainEvent(
            "edit qualification: {} by {}",
            StructuredArguments.keyValue("qualificationDto", mapper.toQualificationDto(qualification)),
            StructuredArguments.keyValue("principal", principal)
        )
    }

    suspend fun deleteQualification(id: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("delete qualification", XPrincipal::allowedToDeleteQualification)
        val qualification = Qualification.findById(id) ?: throw IllegalArgumentException("Qualification with id $id does not exist")

        log.domainEvent(
            "delete qualification: {} by {}",
            StructuredArguments.keyValue("qualificationDto", mapper.toQualificationDto(qualification)),
            StructuredArguments.keyValue("principal", principal)
        )
        qualification.delete()
    }

    suspend fun addUserQualification(userId: Int, qualificationId: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("add qualification to user", XPrincipal::allowedToAddQualificationToUser)

        val user = User.findById(userId)
        val qualification = Qualification.findById(qualificationId)

        requireNotNull(user) { "User with id $userId does not exist" }
        requireNotNull(qualification) { "Qualification with id $qualificationId does not exist" }

        val newQualifications = user.qualifications.toCollection(ArrayList())
        newQualifications.add(qualification)

        user.qualifications = SizedCollection(newQualifications)
        log.domainEvent(
            "add qualification {} to user {} by {}",
            StructuredArguments.keyValue("qualificationDto", mapper.toQualificationDto(qualification)),
            StructuredArguments.keyValue("user", mapper.toUserDto(user)),
            StructuredArguments.keyValue("principal", principal)
        )
    }

    suspend fun removeUserQualification(userId: Int, qualificationId: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("remove qualification from user", XPrincipal::allowedToRemoveQualificationFromUser)

        val user = User.findById(userId)
        val qualification = Qualification.findById(qualificationId)

        requireNotNull(user) { "User with id $userId does not exist" }
        requireNotNull(qualification) { "Qualification with id $qualificationId does not exist" }

        val newQualifications = user.qualifications.toCollection(ArrayList())
        val success = newQualifications.remove(qualification)

        require(success) { "User never had qualification ${qualification.id}/${qualification.name}" }

        user.qualifications = SizedCollection(newQualifications)
        log.domainEvent(
            "remove qualification {} from user {} by {}",
            StructuredArguments.keyValue("qualificationDto", mapper.toQualificationDto(qualification)),
            StructuredArguments.keyValue("user", mapper.toUserDto(user)),
            StructuredArguments.keyValue("principal", principal)
        )
    }

    suspend fun getQualifiedToolsForCardId(cardId: String, cardSecret: String, devicePrincipal: DevicePrincipal): List<ToolDto> = dbQuery {
        val user = User.find { (Users.cardId eq cardId) and (Users.cardSecret eq cardSecret) }.firstOrNull()
        requireNotNull(user) { "User with cardId $cardId and cardSecret $cardSecret does not exist" }

        val device = Device.find { Devices.mac eq devicePrincipal.mac }.firstOrNull()
        requireNotNull(device) { "Device with mac ${devicePrincipal.mac} does not exist" }

        device.tools.filter { tool ->
            tool.qualifications.all { user.qualifications.contains(it) }
        }.map { mapper.toToolDto(it) }
    }

    private fun XPrincipal.requirePermission(description: String, permission: XPrincipal.() -> Boolean) {
        if (!this.permission()) {
            log.info("$name tried to $description")
            throw AuthorizationException("$name not allowed to $description")
        }
    }
}
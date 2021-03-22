package cloud.fabx.service

import cloud.fabx.application.AuthorizationException
import cloud.fabx.application.XPrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.domainEvent
import cloud.fabx.dto.EditToolDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.logger
import cloud.fabx.model.Device
import cloud.fabx.model.Qualification
import cloud.fabx.model.Tool
import cloud.fabx.qualificationService
import net.logstash.logback.argument.StructuredArguments
import org.jetbrains.exposed.sql.SizedCollection

class ToolService {

    private val log = logger()

    suspend fun getAllTools(principal: XPrincipal): List<ToolDto> = dbQuery {
        principal.requirePermission("get all tools", XPrincipal::allowedToGetAllTools)
        Tool.all().map{ toToolDto(it) }.toCollection(ArrayList())
    }

    suspend fun getToolById(id: Int, principal: XPrincipal): ToolDto? = dbQuery {
        principal.requirePermission("get tool by id", XPrincipal::allowedToGetTool)
        Tool.findById(id)?.let { toToolDto(it) }
    }

    suspend fun createNewTool(tool: NewToolDto, principal: XPrincipal): ToolDto = dbQuery {
        principal.requirePermission("create new tool", XPrincipal::allowedToCreateTool)

        val deviceInDb = Device.findById(tool.deviceId) ?: throw IllegalArgumentException("Could not find deviceId.")

        val qualificationsInDb: List<Qualification> = tool.qualifications.map {
            val qualification = Qualification.findById(it)
            requireNotNull(qualification) { "Could not find qualification with id $it" }

            qualification
        }

        deviceInDb.let { deviceIt ->
            val newTool = Tool.new {
                device = deviceIt
                name = tool.name
                pin = tool.pin
                toolType = tool.toolType
                toolState = tool.toolState
                wikiLink = tool.wikiLink
            }

            newTool.qualifications = SizedCollection(qualificationsInDb)

            val toolDto = toToolDto(newTool)
            log.domainEvent(
                "new tool: {} by {}",
                StructuredArguments.keyValue("toolDto", toolDto),
                StructuredArguments.keyValue("principal", principal)
            )
            toolDto
        }
    }

    suspend fun editTool(id: Int, editTool: EditToolDto, principal: XPrincipal) = dbQuery {
        principal.requirePermission("edit tool", XPrincipal::allowedToEditTool)

        val tool = Tool.findById(id) ?: throw IllegalArgumentException("Tool with id $id does not exist")

        editTool.deviceId?.let {deviceId ->
            Device.findById(deviceId)?.let {device ->
                tool.device = device
            }
        }
        editTool.name?.let { tool.name = it }
        editTool.pin?.let { tool.pin = it }
        editTool.toolType?.let { tool.toolType = it }
        editTool.toolState?.let { tool.toolState = it }
        editTool.wikiLink?.let { tool.wikiLink = it }
        editTool.qualifications?.let { qualifications ->
            val qualificationsInDb: List<Qualification> = qualifications.map {
                val qualification = Qualification.findById(it)
                requireNotNull(qualification) { "Could not find qualification with id $it" }

                qualification
            }
            tool.qualifications = SizedCollection(qualificationsInDb)
        }
        log.domainEvent(
            "edit tool: {} by {}",
            StructuredArguments.keyValue("toolDto", toToolDto(tool)),
            StructuredArguments.keyValue("principal", principal)
        )
    }

    suspend fun deleteTool(id: Int, principal: XPrincipal) = dbQuery {
        principal.requirePermission("delete tool", XPrincipal::allowedToDeleteTool)
        val tool = Tool.findById(id) ?: throw IllegalArgumentException("Tool with id $id does not exist")
        log.domainEvent(
            "delete tool: {} by {}",
            StructuredArguments.keyValue("toolDto", toToolDto(tool)),
            StructuredArguments.keyValue("principal", principal)
        )
        tool.delete()
    }

    fun toToolDto(tool: Tool): ToolDto {
        return ToolDto(
            tool.id.value,
            tool.device.id.value,
            tool.name,
            tool.pin,
            tool.toolType,
            tool.toolState,
            tool.wikiLink,
            tool.qualifications.map { qualificationService.toQualificationDto(it) }
        )
    }

    private fun XPrincipal.requirePermission(description: String, permission: XPrincipal.() -> Boolean) {
        if (!this.permission()) {
            log.info("$name tried to $description")
            throw AuthorizationException("$name not allowed to $description.")
        }
    }
}
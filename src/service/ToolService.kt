package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.EditToolDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.Device
import cloud.fabx.model.Qualification
import cloud.fabx.model.Tool
import cloud.fabx.qualificationService
import org.jetbrains.exposed.sql.SizedCollection

class ToolService {

    suspend fun getAllTools(): List<ToolDto> = dbQuery {
        Tool.all().map{ toToolDto(it) }.toCollection(ArrayList())
    }

    suspend fun getToolById(id: Int): ToolDto? = dbQuery {
        Tool.findById(id)?.let { toToolDto(it) }
    }

    suspend fun createNewTool(tool: NewToolDto): ToolDto = dbQuery {
        val deviceInDb = Device.findById(tool.deviceId) ?: throw IllegalArgumentException("Could not find deviceId.")

        val qualificationsInDb: List<Qualification> = tool.qualifications.map {
            val qualification = Qualification.findById(it)
            requireNotNull(qualification) { "Could not find qualification with id $it" }

            qualification!!
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

            toToolDto(newTool)
        }
    }

    suspend fun editTool(id: Int, editTool: EditToolDto) = dbQuery {
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

                qualification!!
            }
            tool.qualifications = SizedCollection(qualificationsInDb)
        }
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
}
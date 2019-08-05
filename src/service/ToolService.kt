package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.Device
import cloud.fabx.model.Tool

class ToolService {

    suspend fun getAllTools(): List<ToolDto> = dbQuery {
        Tool.all().map{ toToolDto(it) }.toCollection(ArrayList())
    }

    suspend fun getToolById(id: Int): ToolDto? = dbQuery {
        Tool.findById(id)?.let { toToolDto(it) }
    }

    suspend fun createNewTool(tool: NewToolDto): ToolDto = dbQuery {
        val deviceInDb = Device.findById(tool.deviceId) ?: throw IllegalArgumentException("Could not find deviceId.")

        deviceInDb.let { deviceIt ->
            val newTool = Tool.new {
                device = deviceIt
                name = tool.name
                pin = tool.pin
                toolType = tool.toolType
                toolState = tool.toolState
                wikiLink = tool.wikiLink
            }

            toToolDto(newTool)
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
            tool.wikiLink
        )
    }
}
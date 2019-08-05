package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.Tool

class ToolService {

    suspend fun getAllTools(): List<ToolDto> = dbQuery {
        Tool.all().map{ toToolDto(it) }.toCollection(ArrayList())
    }

    suspend fun getToolById(id: Int): ToolDto? = dbQuery {
        Tool.findById(id)?.let { toToolDto(it) }
    }

    suspend fun createNewTool(tool: NewToolDto): ToolDto {
        val toolInDb = dbQuery {
            Tool.new {
                name = tool.name
                pin = tool.pin
                toolType = tool.toolType
                toolState = tool.toolState
                wikiLink = tool.wikiLink
            }
        }

        return toToolDto(toolInDb)
    }

    private fun toToolDto(tool: Tool): ToolDto {
        return ToolDto(
            tool.id.value,
            tool.name,
            tool.pin,
            tool.toolType,
            tool.toolState,
            tool.wikiLink
        )
    }
}
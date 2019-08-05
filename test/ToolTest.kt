package cloud.fabx

import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToolTest {

    @Test
    fun testCreateAndGetTool() {
        withTestApplication({ module(testing = true) }) {
            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/device") {
                setBody(
                    mapper.writeValueAsString(
                        NewDeviceDto(
                            "New Device 1",
                            "aaffeeaaffee",
                            "newSecret",
                            "http://bgurl"
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Post, "/api/tool") {
                setBody(
                    mapper.writeValueAsString(
                        NewToolDto(
                            1,
                            "New Tool 1",
                            0,
                            ToolType.UNLOCK,
                            ToolState.GOOD,
                            "http://wikiurl"
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val toolDto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(toolDto.id, 1)
                assertEquals(toolDto.deviceId, 1)
                assertEquals(toolDto.pin, 0)
                assertEquals(toolDto.toolType, ToolType.UNLOCK)
                assertEquals(toolDto.toolState, ToolState.GOOD)
                assertEquals(toolDto.wikiLink, "http://wikiurl")
            }

            handleRequest(HttpMethod.Get, "/api/tool/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val toolDto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(toolDto.id, 1)
                assertEquals(toolDto.deviceId, 1)
                assertEquals(toolDto.pin, 0)
                assertEquals(toolDto.toolType, ToolType.UNLOCK)
                assertEquals(toolDto.toolState, ToolState.GOOD)
                assertEquals(toolDto.wikiLink, "http://wikiurl")
            }
        }
    }
}
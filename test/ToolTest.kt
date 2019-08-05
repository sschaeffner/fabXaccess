package cloud.fabx

import cloud.fabx.dto.*
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
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToolTest: CommonTest() {

    @Test
    fun testGetAllTools() = runBlocking {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/api/tool").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun testCreateAndGetTool() = runBlocking {
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

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)
                assertEquals(1, deviceDto.id)
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

                assertEquals(1, toolDto.id)
                assertEquals(1, toolDto.deviceId)
                assertEquals(0, toolDto.pin)
                assertEquals(ToolType.UNLOCK, toolDto.toolType)
                assertEquals(ToolState.GOOD, toolDto.toolState)
                assertEquals("http://wikiurl", toolDto.wikiLink)
            }

            handleRequest(HttpMethod.Get, "/api/tool/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val toolDto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(1, toolDto.id)
                assertEquals(1, toolDto.deviceId)
                assertEquals(0, toolDto.pin)
                assertEquals(ToolType.UNLOCK, toolDto.toolType)
                assertEquals(ToolState.GOOD, toolDto.toolState)
                assertEquals("http://wikiurl", toolDto.wikiLink)
            }
        }

        Unit
    }
}
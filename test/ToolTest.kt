package cloud.fabx

import cloud.fabx.dto.EditToolDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class ToolTest: CommonTest() {

    @Test
    fun givenNoToolsWhenGetToolsThenReturnsEmptyList() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/v1/tool").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun whenCreateToolThenToolIsCreated() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            handleRequest(HttpMethod.Post, "/api/v1/tool") {
                setBody(
                    mapper.writeValueAsString(
                        NewToolDto(
                            1,
                            "New Tool 1",
                            0,
                            ToolType.UNLOCK,
                            ToolState.GOOD,
                            "http://wikiurl",
                            listOf(1)
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
                assertEquals("New Tool 1", toolDto.name)
                assertEquals(ToolType.UNLOCK, toolDto.toolType)
                assertEquals(ToolState.GOOD, toolDto.toolState)
                assertEquals("http://wikiurl", toolDto.wikiLink)
                assertEquals(1, toolDto.qualifications.size)
            }
        }

        Unit
    }

    @Test
    fun givenToolExistsWhenGetToolThenOk() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            handleRequest(HttpMethod.Get, "/api/v1/tool/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals(1, dto.deviceId)
                assertEquals(0, dto.pin)
                assertEquals(ToolType.UNLOCK, dto.toolType)
                assertEquals(ToolState.GOOD, dto.toolState)
                assertEquals("http://wikiurl", dto.wikiLink)
            }
        }

        Unit
    }

    @Test
    fun givenToolWhenEditSingleParameterThenParameterIsChanged() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            handleRequest(HttpMethod.Patch, "/api/v1/tool/1") {
                setBody(
                    mapper.writeValueAsString(EditToolDto(
                        null,
                        "Edited Tool Name",
                        null,
                        null,
                        null,
                        null,
                        null
                    ))
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/tool/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals(1, dto.deviceId)
                assertEquals(0, dto.pin)
                assertEquals("Edited Tool Name", dto.name)
                assertEquals(ToolType.UNLOCK, dto.toolType)
                assertEquals(ToolState.GOOD, dto.toolState)
                assertEquals("http://wikiurl", dto.wikiLink)
                assertEquals(1, dto.qualifications.size)
                assertEquals(1, dto.qualifications[0].id)
            }

        }


        Unit
    }

    @Test
    fun givenToolWhenEditAllParametersThenParametersAreChanged() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val deviceDto2 = givenDevice(
                mac = "aaffeeaaffff",
                secret = "newSecret2"
            )
            assertEquals(2, deviceDto2.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            handleRequest(HttpMethod.Patch, "/api/v1/tool/1") {
                setBody(
                    mapper.writeValueAsString(EditToolDto(
                        2,
                        "Edited Tool Name",
                        1,
                        ToolType.KEEP,
                        ToolState.BAD,
                        "http://newwikiurl",
                        listOf(1)
                    ))
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/tool/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals(2, dto.deviceId)
                assertEquals(1, dto.pin)
                assertEquals("Edited Tool Name", dto.name)
                assertEquals(ToolType.KEEP, dto.toolType)
                assertEquals(ToolState.BAD, dto.toolState)
                assertEquals("http://newwikiurl", dto.wikiLink)
                assertEquals(1, dto.qualifications.size)
                assertEquals(1, dto.qualifications[0].id)
            }
        }

        Unit
    }

    @Test
    fun givenToolWhenDeleteToolThenToolNoLongerExists() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val toolDto = givenTool(1, qualifications = listOf())
            assertEquals(1, toolDto.id)

            // DELETE TOOL
            handleRequest(HttpMethod.Delete, "/api/v1/tool/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get,"/api/v1/tool/1").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        Unit
    }
}
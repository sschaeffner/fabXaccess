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
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class ToolTest: CommonTest() {

    @Test
    fun testGetAllTools() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/v1/tool").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun testCreateAndGetTool() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val mapper = jacksonObjectMapper()

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
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

            // CREATE QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(mapper.writeValueAsString(
                    NewQualificationDto(
                        "New Qualification 1",
                        "A Qualification"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val qualificationDto = mapper.readValue<QualificationDto>(response.content!!)
                assertEquals(1, qualificationDto.id)
            }

            // CREATE TOOL
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

            handleRequest(HttpMethod.Get, "/api/v1/tool/1").apply {
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

    @Test
    fun testEditToolSingleParameter() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val mapper = jacksonObjectMapper()

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
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

            // CREATE QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(mapper.writeValueAsString(
                    NewQualificationDto(
                        "New Qualification 1",
                        "A Qualification"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val qualificationDto = mapper.readValue<QualificationDto>(response.content!!)
                assertEquals(1, qualificationDto.id)
            }

            // CREATE TOOL
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

                val toolDto = mapper.readValue<ToolDto>(response.content!!)
                assertEquals(1, toolDto.id)
            }

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

                val toolDto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(1, toolDto.id)
                assertEquals(1, toolDto.deviceId)
                assertEquals(0, toolDto.pin)
                assertEquals("Edited Tool Name", toolDto.name)
                assertEquals(ToolType.UNLOCK, toolDto.toolType)
                assertEquals(ToolState.GOOD, toolDto.toolState)
                assertEquals("http://wikiurl", toolDto.wikiLink)
                assertEquals(1, toolDto.qualifications.size)
                assertEquals(1, toolDto.qualifications[0].id)
            }

        }


        Unit
    }

    @Test
    fun testEditToolAllParameters() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val mapper = jacksonObjectMapper()

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
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

            // CREATE SECOND DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(
                    mapper.writeValueAsString(
                        NewDeviceDto(
                            "New Device 2",
                            "aaffeeaaffff",
                            "newSecret2",
                            "http://bgurl2"
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)
                assertEquals(2, deviceDto.id)
            }

            // CREATE QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(mapper.writeValueAsString(
                    NewQualificationDto(
                        "New Qualification 1",
                        "A Qualification"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val qualificationDto = mapper.readValue<QualificationDto>(response.content!!)
                assertEquals(1, qualificationDto.id)
            }

            // CREATE TOOL
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
                            listOf()
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val toolDto = mapper.readValue<ToolDto>(response.content!!)
                assertEquals(1, toolDto.id)
            }


            // EDIT TOOL
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

                val toolDto = mapper.readValue<ToolDto>(response.content!!)

                assertEquals(1, toolDto.id)
                assertEquals(2, toolDto.deviceId)
                assertEquals(1, toolDto.pin)
                assertEquals("Edited Tool Name", toolDto.name)
                assertEquals(ToolType.KEEP, toolDto.toolType)
                assertEquals(ToolState.BAD, toolDto.toolState)
                assertEquals("http://newwikiurl", toolDto.wikiLink)
                assertEquals(1, toolDto.qualifications.size)
                assertEquals(1, toolDto.qualifications[0].id)
            }
        }

        Unit
    }
}
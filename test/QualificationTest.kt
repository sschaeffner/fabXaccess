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
class QualificationTest: CommonTest() {
    @Test
    fun testQualifications() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

            // CREATE USER
            handleRequest(HttpMethod.Post, "/api/v1/user") {
                setBody(mapper.writeValueAsString(
                    NewUserDto(
                        "New User 1",
                        "New User 1 LastName",
                        "newUserWikiName",
                        "123456"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val userDto = mapper.readValue<UserDto>(response.content!!)
                assertEquals(1, userDto.id)
            }

            // CREATE QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(mapper.writeValueAsString(
                    NewQualificationDto(
                        "New Qualification 1",
                        "A Qualification",
                        "#000000"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val qualificationDto = mapper.readValue<QualificationDto>(response.content!!)
                assertEquals(1, qualificationDto.id)
            }

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl",
                        "http://fabx.backup"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)
                assertEquals(1, deviceDto.id)
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

            // ADD QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/user/1/qualifications") {
                setBody(
                    mapper.writeValueAsString(
                        UserQualificationDto(
                            1,
                            1
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals(1, userDto.qualifications.size)
                assertEquals(1, userDto.qualifications[0].id)
            }
        }

        Unit
    }

    @Test
    fun testDeleteQualification() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

            // CREATE USER
            handleRequest(HttpMethod.Post, "/api/v1/user") {
                setBody(mapper.writeValueAsString(
                    NewUserDto(
                        "New User 1",
                        "New User 1 LastName",
                        "newUserWikiName",
                        "123456"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val userDto = mapper.readValue<UserDto>(response.content!!)
                assertEquals(1, userDto.id)
            }

            // CREATE QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(mapper.writeValueAsString(
                    NewQualificationDto(
                        "New Qualification 1",
                        "A Qualification",
                        "#000000"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val qualificationDto = mapper.readValue<QualificationDto>(response.content!!)
                assertEquals(1, qualificationDto.id)
            }

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl",
                        "http://fabx.backup"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)
                assertEquals(1, deviceDto.id)
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

            // ADD QUALIFICATION
            handleRequest(HttpMethod.Post, "/api/v1/user/1/qualifications") {
                setBody(
                    mapper.writeValueAsString(
                        UserQualificationDto(
                            1,
                            1
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            //REMOVE QUALIFICATION
            handleRequest(HttpMethod.Delete, "/api/v1/user/1/qualifications/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals(0, userDto.qualifications.size)
            }
        }

        Unit
    }
}
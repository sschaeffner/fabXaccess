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
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

@InternalAPI
@KtorExperimentalAPI
class ClientApiAuthTest: CommonTest() {

    @Test
    fun getConfigWithoutAuthenticationThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {
            val mapper = jacksonObjectMapper()
            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)
                assertEquals(1, deviceDto.id)
            }

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/config").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            Unit
        }
    }

    @Test
    fun getConfigWithAuthentication() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {
            val mapper = jacksonObjectMapper()
            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)
                assertEquals(1, deviceDto.id)
            }

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/config"){
                addBasicAuth("aaffeeaaffee", "newSecret")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("New Device 1\n", response.content)
            }

            Unit
        }
    }

    @Test
    fun getPermissionsWithoutAuthentication() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {
            val mapper = jacksonObjectMapper()

            // CREATE USER
            handleRequest(HttpMethod.Post, "/api/v1/user") {
                setBody(mapper.writeValueAsString(
                    NewUserDto(
                        "New User 1",
                        "newUserWikiName",
                        "123456",
                        "aabbccdd"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val userDto = mapper.readValue<UserDto>(response.content!!)
                assertEquals(1, userDto.id)
            }

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
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
                            "http://wikiurl"
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val toolDto = mapper.readValue<ToolDto>(response.content!!)
                assertEquals(1, toolDto.id)
            }

            // ADD PERMISSION
            handleRequest(HttpMethod.Post, "/api/v1/user/1/permissions") {
                setBody(
                    mapper.writeValueAsString(
                        UserPermissionDto(
                            1,
                            1
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/permissions/aabbccdd").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            Unit
        }
    }

    @Test
    fun getPermissionsWithAuthentication() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {
            val mapper = jacksonObjectMapper()

            // CREATE USER
            handleRequest(HttpMethod.Post, "/api/v1/user") {
                setBody(mapper.writeValueAsString(
                    NewUserDto(
                        "New User 1",
                        "newUserWikiName",
                        "123456",
                        "aabbccdd"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val userDto = mapper.readValue<UserDto>(response.content!!)
                assertEquals(1, userDto.id)
            }

            // CREATE DEVICE
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
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
                            "http://wikiurl"
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val toolDto = mapper.readValue<ToolDto>(response.content!!)
                assertEquals(1, toolDto.id)
            }

            // ADD PERMISSION
            handleRequest(HttpMethod.Post, "/api/v1/user/1/permissions") {
                setBody(
                    mapper.writeValueAsString(
                        UserPermissionDto(
                            1,
                            1
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/permissions/aabbccdd") {
                addBasicAuth("aaffeeaaffee", "newSecret")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("1", response.content?.trim())
            }

            Unit
        }
    }
}
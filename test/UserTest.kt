package cloud.fabx

import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
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
class UserTest: CommonTest() {

    @Test
    fun testGetAllUsers() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/user").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun testCreateAndGetUser() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/user") {
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
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("New User 1", userDto.name)
                assertEquals("newUserWikiName", userDto.wikiName)
                assertEquals("123456", userDto.phoneNumer)
                assertEquals(false, userDto.locked)
                assertTrue(userDto.lockedReason.isEmpty())
                assertEquals("aabbccdd", userDto.cardId)
                assertEquals(0, userDto.permissions.size)
            }

            handleRequest(HttpMethod.Get, "/api/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("New User 1", userDto.name)
                assertEquals("newUserWikiName", userDto.wikiName)
                assertEquals("123456", userDto.phoneNumer)
                assertEquals(false, userDto.locked)
                assertTrue(userDto.lockedReason.isEmpty())
                assertEquals("aabbccdd", userDto.cardId)
                assertEquals(0, userDto.permissions.size)

            }
        }

        Unit
    }

    @Test
    fun testEditUserSingleParam() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/user") {
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

            handleRequest(HttpMethod.Put, "/api/user/1") {
                setBody(mapper.writeValueAsString(
                    EditUserDto(
                        "Edit Username 1",
                        null,
                        null,
                        null,
                        null,
                        null

                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("Edit Username 1", userDto.name)
                assertEquals("newUserWikiName", userDto.wikiName)
                assertEquals("123456", userDto.phoneNumer)
                assertEquals(false, userDto.locked)
                assertTrue(userDto.lockedReason.isEmpty())
                assertEquals("aabbccdd", userDto.cardId)
                assertEquals(0, userDto.permissions.size)

            }
        }

        Unit
    }

    @Test
    fun testEditUserAllParams() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/user") {
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

            handleRequest(HttpMethod.Put, "/api/user/1") {
                setBody(mapper.writeValueAsString(
                    EditUserDto(
                        "Edit Username 1",
                        "editWikiName",
                        "54321",
                        true,
                        "edit locked reason",
                        "bbaabbaa"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("Edit Username 1", userDto.name)
                assertEquals("editWikiName", userDto.wikiName)
                assertEquals("54321", userDto.phoneNumer)
                assertEquals(true, userDto.locked)
                assertEquals("edit locked reason", userDto.lockedReason)
                assertEquals("bbaabbaa", userDto.cardId)
                assertEquals(0, userDto.permissions.size)

            }
        }

        Unit
    }
}
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
            handleRequest(HttpMethod.Get, "/api/v1/user").apply {
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
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("New User 1", userDto.firstName)
                assertEquals("New User 1 LastName", userDto.lastName)
                assertEquals("newUserWikiName", userDto.wikiName)
                assertEquals("123456", userDto.phoneNumber)
                assertEquals(false, userDto.locked)
                assertTrue(userDto.lockedReason.isEmpty())
                assertEquals(null, userDto.cardId)
                assertEquals(0, userDto.qualifications.size)
            }

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("New User 1", userDto.firstName)
                assertEquals("New User 1 LastName", userDto.lastName)
                assertEquals("newUserWikiName", userDto.wikiName)
                assertEquals("123456", userDto.phoneNumber)
                assertEquals(false, userDto.locked)
                assertTrue(userDto.lockedReason.isEmpty())
                assertEquals(null, userDto.cardId)
                assertEquals(0, userDto.qualifications.size)

            }
        }

        Unit
    }

    @Test
    fun testEditUserSingleParam() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

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

            handleRequest(HttpMethod.Patch, "/api/v1/user/1") {
                setBody(mapper.writeValueAsString(
                    EditUserDto(
                        "Edit Username 1",
                        null,
                        null,
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

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("Edit Username 1", userDto.firstName)
                assertEquals("New User 1 LastName", userDto.lastName)
                assertEquals("newUserWikiName", userDto.wikiName)
                assertEquals("123456", userDto.phoneNumber)
                assertEquals(false, userDto.locked)
                assertTrue(userDto.lockedReason.isEmpty())
                assertEquals(null, userDto.cardId)
                assertEquals(0, userDto.qualifications.size)

            }
        }

        Unit
    }

    @Test
    fun testEditUserAllParams() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val mapper = jacksonObjectMapper()

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

            handleRequest(HttpMethod.Patch, "/api/v1/user/1") {
                setBody(mapper.writeValueAsString(
                    EditUserDto(
                        "Edit Username 1",
                        "New User 1 LastName",
                        "editWikiName",
                        "54321",
                        true,
                        "edit locked reason",
                        "bbaabbaa",
                        "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, userDto.id)
                assertEquals("Edit Username 1", userDto.firstName)
                assertEquals("New User 1 LastName", userDto.lastName)
                assertEquals("editWikiName", userDto.wikiName)
                assertEquals("54321", userDto.phoneNumber)
                assertEquals(true, userDto.locked)
                assertEquals("edit locked reason", userDto.lockedReason)
                assertEquals("bbaabbaa", userDto.cardId)
                assertEquals("11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF", userDto.cardSecret)
                assertEquals(0, userDto.qualifications.size)
            }
        }

        Unit
    }
}
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class UserTest: CommonTest() {

    @Test
    fun givenNoUsersWhenGetUsersThenReturnsEmptyList() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/v1/user").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun whenCreateUserThenUserIsCreated() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
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
        }

        Unit
    }

    @Test
    fun givenUserExistsWhenGetUserThenOk() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val userDto = givenUser(
                "New User 1",
                "New User 1 LastName",
                "newUserWikiName",
                "123456"
            )
            assertEquals(1, userDto.id)

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("New User 1", dto.firstName)
                assertEquals("New User 1 LastName", dto.lastName)
                assertEquals("newUserWikiName", dto.wikiName)
                assertEquals("123456", dto.phoneNumber)
                assertEquals(false, dto.locked)
                assertTrue(dto.lockedReason.isEmpty())
                assertEquals(null, dto.cardId)
                assertEquals(0, dto.qualifications.size)

            }
        }

        Unit
    }

    @Test
    fun givenUserWhenEditSingleParameterThenParameterIsChanged() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val userDto = givenUser(
                "New User 1",
                "New User 1 LastName",
                "newUserWikiName",
                "123456"
            )
            assertEquals(1, userDto.id)

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

                val dto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("Edit Username 1", dto.firstName)
                assertEquals("New User 1 LastName", dto.lastName)
                assertEquals("newUserWikiName", dto.wikiName)
                assertEquals("123456", dto.phoneNumber)
                assertEquals(false, dto.locked)
                assertTrue(dto.lockedReason.isEmpty())
                assertEquals(null, dto.cardId)
                assertEquals(0, dto.qualifications.size)

            }
        }

        Unit
    }

    @Test
    fun givenUserWhenEditAllParametersThenParametersAreChanged() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val userDto = givenUser(
                "New User 1",
                "New User 1 LastName",
                "newUserWikiName",
                "123456"
            )
            assertEquals(1, userDto.id)

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

                val dto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("Edit Username 1", dto.firstName)
                assertEquals("New User 1 LastName", dto.lastName)
                assertEquals("editWikiName", dto.wikiName)
                assertEquals("54321", dto.phoneNumber)
                assertEquals(true, dto.locked)
                assertEquals("edit locked reason", dto.lockedReason)
                assertEquals("bbaabbaa", dto.cardId)
                assertEquals("11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF", dto.cardSecret)
                assertEquals(0, dto.qualifications.size)
            }
        }

        Unit
    }

    @Test
    fun givenUserWhenDeleteUserThenUserNoLongerExists() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val userDto = givenUser()
            assertEquals(1, userDto.id)

            handleRequest(HttpMethod.Delete, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get,"/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        Unit
    }
}
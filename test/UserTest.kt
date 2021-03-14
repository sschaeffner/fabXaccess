package cloud.fabx

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import isNotSuccess
import isOK
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class UserTest : CommonTest() {

    @Test
    fun `given no users when getting users then returns empty list`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // when
            handleRequest(HttpMethod.Get, "/api/v1/user").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content).isEqualTo("[]")
            }
        }

        Unit
    }

    @Test
    fun `when creating user then user is created`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val firstName = "New User 1"
            val lastName = "New User 1 LastName"
            val wikiName = "newUserWikiName"
            val phoneNumber = "123456"

            // when
            handleRequest(HttpMethod.Post, "/api/v1/user") {
                setBody(
                    mapper.writeValueAsString(
                        NewUserDto(
                            firstName,
                            lastName,
                            wikiName,
                            phoneNumber
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<UserDto>()
                    .isEqualTo(
                        UserDto(
                            1,
                            firstName,
                            lastName,
                            wikiName,
                            phoneNumber,
                            false,
                            "",
                            null,
                            null,
                            listOf()
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `given existing user when getting user then ok`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val firstName = "New User 1"
            val lastName = "New User 1 LastName"
            val wikiName = "newUserWikiName"
            val phoneNumber = "123456"
            val userDto = givenUser(
                firstName,
                lastName,
                wikiName,
                phoneNumber
            )

            // when
            handleRequest(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<UserDto>()
                    .isEqualTo(
                        UserDto(
                            1,
                            firstName,
                            lastName,
                            wikiName,
                            phoneNumber,
                            false,
                            "",
                            null,
                            null,
                            listOf()
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `given user when editing single parameter then parameter is changed`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val userDto = givenUser(
                "New User 1",
                "New User 1 LastName",
                "newUserWikiName",
                "123456"
            )
            val newFirstName = "Edit Username 1"

            // when
            handleRequest(HttpMethod.Patch, "/api/v1/user/${userDto.id}") {
                setBody(
                    mapper.writeValueAsString(
                        EditUserDto(
                            newFirstName,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<UserDto>()
                    .isEqualTo(userDto.copy(firstName = newFirstName))
            }
        }

        Unit
    }

    @Test
    fun `given user when editing all parameters then parameters are changed`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val userDto = givenUser(
                "New User 1",
                "New User 1 LastName",
                "newUserWikiName",
                "123456"
            )

            val newFirstName = "Edit Username 1"
            val newLastName = "New User 1 LastName"
            val newWikiName = "editWikiName"
            val newPhoneNumber = "54321"
            val newLocked = true
            val newLockedReason = "edit locked reason"
            val newCardId = "bbaabbaa"
            val newCardSecret = "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"

            // when
            handleRequest(HttpMethod.Patch, "/api/v1/user/${userDto.id}") {
                setBody(
                    mapper.writeValueAsString(
                        EditUserDto(
                            newFirstName,
                            newLastName,
                            newWikiName,
                            newPhoneNumber,
                            newLocked,
                            newLockedReason,
                            newCardId,
                            newCardSecret
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<UserDto>()
                    .isEqualTo(
                        UserDto(
                            userDto.id,
                            newFirstName,
                            newLastName,
                            newWikiName,
                            newPhoneNumber,
                            newLocked,
                            newLockedReason,
                            newCardId,
                            newCardSecret,
                            listOf()
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `given user when deleting user then user no longer exists`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val userDto = givenUser()

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/user/${userDto.id}").apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }

        Unit
    }

    @Test
    fun `given user with qualification when deleting user then error message`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val userDto = givenUser()

            val qualificationDto = givenQualification()

            givenUserHasQualification(userDto.id, qualificationDto.id)

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/user/${userDto.id}").apply {
                // then
                assertThat(response.status())
                    .isNotNull()
                    .isNotSuccess()
                assertThat(response.content)
                    .isNotNull()
                    .contains("FK_USERQUALIFICATIONS_USER_ID")
            }
        }

        Unit
    }
}
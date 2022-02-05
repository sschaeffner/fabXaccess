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
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import isNotSuccess
import isOK
import org.junit.Test

@InternalAPI
@KtorExperimentalAPI
class UserTest : CommonTest() {

    @Test
    fun `given no users when getting users then returns empty list`() = testApp {
        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/user").apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content).isEqualTo("[]")
        }
    }

    @Test
    fun `when creating user then user is created`() = testApp {
        // given
        val firstName = "New User 1"
        val lastName = "New User 1 LastName"
        val wikiName = "newUserWikiName"
        val phoneNumber = "+49123456"

        // when
        handleRequestAsAdmin(HttpMethod.Post, "/api/v1/user") {
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

    @Test
    fun `given phone number in wrong format when creating user then UnprocessableEntity`() = testApp {
        // given
        val firstName = "New User 1"
        val lastName = "New User 1 LastName"
        val wikiName = "newUserWikiName"
        val phoneNumber = "123"

        // when
        handleRequestAsAdmin(HttpMethod.Post, "/api/v1/user") {
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
            assertThat(response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(response.content)
                .isNotNull()
                .isEqualTo("Phone number has to be entered with + prefix.")
        }
    }

    @Test
    fun `given existing user when getting user then ok`() = testApp {
        // given
        val firstName = "New User 1"
        val lastName = "New User 1 LastName"
        val wikiName = "newUserWikiName"
        val phoneNumber = "+49123456"
        val userDto = givenUser(
            firstName,
            lastName,
            wikiName,
            phoneNumber
        )

        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
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

    @Test
    fun `given user when editing single parameter then parameter is changed`() = testApp {
        // given
        val userDto = givenUser(
            "New User 1",
            "New User 1 LastName",
            "newUserWikiName",
            "+123456"
        )
        val newFirstName = "Edit Username 1"

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/user/${userDto.id}") {
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
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<UserDto>()
                .isEqualTo(userDto.copy(firstName = newFirstName))
        }
    }

    @Test
    fun `given user when editing all parameters then parameters are changed`() = testApp {
        // given
        val userDto = givenUser(
            "New User 1",
            "New User 1 LastName",
            "newUserWikiName",
            "+123456"
        )

        val newFirstName = "Edit Username 1"
        val newLastName = "New User 1 LastName"
        val newWikiName = "editWikiName"
        val newPhoneNumber = "+54321"
        val newLocked = true
        val newLockedReason = "edit locked reason"
        val newCardId = "bbaabbaa"
        val newCardSecret = "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/user/${userDto.id}") {
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
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
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


    @Test
    fun `given empty phone number when editing user then sets to null`() = testApp {
        // given
        val userDto = givenUser(
            "New User 1",
            "New User 1 LastName",
            "newUserWikiName",
            "+123456"
        )

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/user/${userDto.id}") {
            setBody(
                mapper.writeValueAsString(
                    EditUserDto(
                        null,
                        null,
                        null,
                        "",
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
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<UserDto>()
                .isEqualTo(userDto.copy(phoneNumber = ""))
        }
    }

    @Test
    fun `given invalid phone number when editing user then UnprocessableEntity`() = testApp {
        // given
        val userDto = givenUser(
            "New User 1",
            "New User 1 LastName",
            "newUserWikiName",
            "+123456"
        )

        val newPhoneNumber = "4321"

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/user/${userDto.id}") {
            setBody(
                mapper.writeValueAsString(
                    EditUserDto(
                        null,
                        null,
                        null,
                        newPhoneNumber,
                        null,
                        null,
                        null,
                        null
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }.apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(response.content)
                .isNotNull()
                .isEqualTo("Phone number has to be entered with + prefix.")
        }
    }

    @Test
    fun `given invalid user id when editing user then NotFound`() = testApp {
        // given
        val invalidUserId = 42

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/user/$invalidUserId") {
            setBody(
                mapper.writeValueAsString(
                    EditUserDto(
                        null,
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
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            assertThat(response.content).isEqualTo("User does not exist")
        }
    }

    @Test
    fun `given user when deleting user then user no longer exists`() = testApp {
        // given
        val userDto = givenUser()

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/user/${userDto.id}").apply {
            assertThat(response.status()).isOK()
        }

        // then
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/user/${userDto.id}").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `given user with qualification when deleting user then error message`() = testApp {
        // given
        val userDto = givenUser()

        val qualificationDto = givenQualification()

        givenUserHasQualification(userDto.id, qualificationDto.id)

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/user/${userDto.id}").apply {
            // then
            assertThat(response.status())
                .isNotNull()
                .isNotSuccess()
            assertThat(response.content)
                .isNotNull()
                .contains("FK_USERQUALIFICATIONS_USER__ID")
        }
    }

    @Test
    fun `given invalid user id when deleting user then NotFound`() = testApp {
        // given
        val invalidUserId = 42

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/user/$invalidUserId").apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            assertThat(response.content).isEqualTo("User does not exist")
        }
    }
}
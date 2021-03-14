package cloud.fabx

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabx.dto.EditQualificationDto
import cloud.fabx.dto.NewQualificationDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.UserDto
import cloud.fabx.dto.UserQualificationDto
import com.fasterxml.jackson.module.kotlin.readValue
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
class QualificationTest : CommonTest() {

    @Test
    fun `given no qualifications when getting all qualifications then return empty list`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // when
            handleRequest(HttpMethod.Get, "/api/v1/qualification").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .isEqualTo("[]")
            }
        }

        Unit
    }

    @Test
    fun `when creating qualification then OK`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given 
            val name = "New Qualification 1"
            val description = "A Qualification"
            val colour = "#000000"
            val orderNr = 1

            val newQualificationDto = NewQualificationDto(
                name,
                description,
                colour,
                orderNr
            )

            // when
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(
                    mapper.writeValueAsString(
                        newQualificationDto
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .transform { mapper.readValue<QualificationDto>(it) }
                    .isEqualTo(
                        QualificationDto(
                            1,
                            name,
                            description,
                            colour,
                            orderNr
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `given qualification when getting qualification then return qualification`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val name = "New Qualification 1"
            val description = "A Qualification"
            val colour = "#000000"
            val orderNr = 1

            val qualificationDto = givenQualification(
                name,
                description,
                colour,
                orderNr
            )

            //when
            handleRequest(HttpMethod.Get, "/api/v1/qualification/${qualificationDto.id}").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .transform { mapper.readValue<QualificationDto>(it) }
                    .isEqualTo(
                        QualificationDto(
                            qualificationDto.id,
                            name,
                            description,
                            colour,
                            orderNr
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `when editing single parameter of qualification then qualification is changed`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val qualificationDto = givenQualification(
                "New Qualification 1",
                "A Qualification",
                "#000000",
                1
            )

            val newName = "Edited Qualification Name 1"

            // when
            handleRequest(HttpMethod.Patch, "/api/v1/qualification/${qualificationDto.id}") {
                setBody(
                    mapper.writeValueAsString(
                        EditQualificationDto(
                            newName,
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
            handleRequest(HttpMethod.Get, "/api/v1/qualification/${qualificationDto.id}").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .transform { mapper.readValue<QualificationDto>(it) }
                    .isEqualTo(qualificationDto.copy(name = newName))
            }
        }

        Unit
    }

    @Test
    fun `when editing all parameters of qualification then qualification is changed`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val createdQualificationDto = givenQualification(
                "New Qualification 1",
                "A Qualification",
                "#000000",
                1
            )

            val newName = "Edited Qualification Name 1"
            val newDescription = "Edited Qualification Description"
            val newColour = "#FFFFFF"
            val newOrderNr = 42

            // when
            handleRequest(HttpMethod.Patch, "/api/v1/qualification/${createdQualificationDto.id}") {
                setBody(
                    mapper.writeValueAsString(
                        EditQualificationDto(
                            newName,
                            newDescription,
                            newColour,
                            newOrderNr
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/qualification/${createdQualificationDto.id}").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .transform { mapper.readValue<QualificationDto>(it) }
                    .isEqualTo(QualificationDto(
                        createdQualificationDto.id,
                        newName,
                        newDescription,
                        newColour,
                        newOrderNr
                    ))
            }
        }

        Unit
    }

    @Test
    fun `when deleting qualification then it no longer exists`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val qualificationDto = givenQualification()

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/qualification/${qualificationDto.id}").apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/qualification/1").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }

        Unit
    }


    @Test
    fun `when adding qualification to user then it is added`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val userDto = givenUser()
            val qualificationDto = givenQualification()

            // when
            handleRequest(HttpMethod.Post, "/api/v1/user/${userDto.id}/qualifications") {
                setBody(
                    mapper.writeValueAsString(
                        UserQualificationDto(
                            userDto.id,
                            qualificationDto.id
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
                    .transform { mapper.readValue<UserDto>(it) }
                    .all {
                        transform { it.id }.isEqualTo(userDto.id)
                        transform { it.qualifications }.extracting { it.id }.containsExactly(qualificationDto.id)
                    }
            }
        }

        Unit
    }

    @Test
    fun `given user has qualification when removing it then it is no longer there`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val userDto = givenUser()
            val qualificationDto = givenQualification()
            givenUserHasQualification(userDto.id, qualificationDto.id)

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/user/${userDto.id}/qualifications/${qualificationDto.id}").apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .transform { mapper.readValue<UserDto>(it) }
                    .all {
                        transform { it.id }.isEqualTo(userDto.id)
                        transform { it.qualifications }.isEmpty()
                    }
            }
        }

        Unit
    }

    @Test
    fun `given qualification for tool when deleting qualification then error message`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val qualificationDto = givenQualification()
            val deviceDto = givenDevice()
            givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/qualification/${qualificationDto.id}").apply {
                // then
                assertThat(response.status())
                    .isNotNull()
                    .isNotSuccess()
                assertThat(response.content)
                    .isNotNull()
                    .contains("FK_TOOLQUALIFICATIONS_QUALIFICATION_ID")
            }
        }

        Unit
    }

    @Test
    fun `given qualification for user when deleting qualification then error message`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val qualificationDto = givenQualification()
            val userDto = givenUser()
            givenUserHasQualification(userDto.id, qualificationDto.id)

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/qualification/${qualificationDto.id}").apply {
                // then
                assertThat(response.status())
                    .isNotNull()
                    .isNotSuccess()
                assertThat(response.content)
                    .isNotNull()
                    .contains("FK_USERQUALIFICATIONS_QUALIFICATION_ID")
            }
        }

        Unit
    }
}
package cloud.fabx

import assertk.assertThat
import assertk.assertions.*
import cloud.fabx.dto.EditQualificationDto
import cloud.fabx.dto.NewQualificationDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.UserDto
import cloud.fabx.dto.UserQualificationDto
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import isNotSuccess
import isSuccess
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class QualificationTest: CommonTest() {

    @Test
    fun givenNoQualificationsExistWhenGetAllQualificationsThenReturnsEmptyList() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/v1/qualification").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun whenCreateQualificationThenOk() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Post, "/api/v1/qualification") {
                setBody(mapper.writeValueAsString(
                    NewQualificationDto(
                        "New Qualification 1",
                        "A Qualification",
                        "#000000",
                        1
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<QualificationDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("New Qualification 1", dto.name)
                assertEquals("A Qualification", dto.description)
                assertEquals("#000000", dto.colour)
                assertEquals(1, dto.orderNr)
            }
        }

        Unit
    }

    @Test
    fun givenQualificationWhenGetQualificationThenReturnsQualification() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val qualificationDto = givenQualification(
                "New Qualification 1",
                "A Qualification",
                "#000000",
                1
            )
            assertEquals(1, qualificationDto.id)

            handleRequest(HttpMethod.Get, "/api/v1/qualification/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<QualificationDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("New Qualification 1", dto.name)
                assertEquals("A Qualification", dto.description)
                assertEquals("#000000", dto.colour)
                assertEquals(1, dto.orderNr)
            }
        }

        Unit
    }

    @Test
    fun whenEditingSingleParameterOfQualificationThenQualificationIsChanged() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val qualificationDto = givenQualification(
                "New Qualification 1",
                "A Qualification",
                "#000000",
                1
            )
            assertEquals(1, qualificationDto.id)

            handleRequest(HttpMethod.Patch, "/api/v1/qualification/1") {
                setBody(mapper.writeValueAsString(
                    EditQualificationDto(
                        "Edited Qualification Name 1",
                        null,
                        null,
                        null
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/qualification/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<QualificationDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("Edited Qualification Name 1", dto.name)
                assertEquals("A Qualification", dto.description)
                assertEquals("#000000", dto.colour)
                assertEquals(1, dto.orderNr)
            }
        }

        Unit
    }

    @Test
    fun whenEditingAllParametersOfQualificationThenQualificationIsChanged() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val createdQualificationDto = givenQualification(
                "New Qualification 1",
                "A Qualification",
                "#000000",
                1
            )
            assertEquals(1, createdQualificationDto.id)


            handleRequest(HttpMethod.Patch, "/api/v1/qualification/1") {
                setBody(mapper.writeValueAsString(
                    EditQualificationDto(
                        "Edited Qualification Name 1",
                        "Edited Qualification Description",
                        "#FFFFFF",
                        42
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/qualification/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<QualificationDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("Edited Qualification Name 1", dto.name)
                assertEquals("Edited Qualification Description", dto.description)
                assertEquals("#FFFFFF", dto.colour)
                assertEquals(42, dto.orderNr)
            }
        }

        Unit
    }

    @Test
    fun whenDeletingQualificationThenItNoLongerExists() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            handleRequest(HttpMethod.Delete, "/api/v1/qualification/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get,"/api/v1/qualification/1").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        Unit
    }


    @Test
    fun whenAddingQualificationToUserThenItIsAdded() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            val userDto = givenUser()
            assertEquals(1, userDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

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

                val dto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals(1, dto.qualifications.size)
                assertEquals(1, dto.qualifications[0].id)
            }
        }

        Unit
    }

    @Test
    fun testDeleteQualificationForUser() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {

            val userDto = givenUser()
            assertEquals(1, userDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            givenUserHasQualification(1, 1)

            handleRequest(HttpMethod.Delete, "/api/v1/user/1/qualifications/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals(0, dto.qualifications.size)
            }
        }

        Unit
    }

    @Test
    fun givenQualificationForToolWhenDeleteQualificationThenErrorMessage() = runBlocking {
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
    fun givenQualificationForUserWhenDeleteQualificationThenErrorMessage() = runBlocking {
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
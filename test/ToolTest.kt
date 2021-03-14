package cloud.fabx

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabx.dto.EditToolDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
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
class ToolTest : CommonTest() {

    @Test
    fun `given no tools when getting tools then returns empty list`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // when
            handleRequest(HttpMethod.Get, "/api/v1/tool").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content).isEqualTo("[]")
            }
        }

        Unit
    }

    @Test
    fun `when create tool then tool is created`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val deviceDto = givenDevice()
            val qualificationDto = givenQualification()

            val name = "New Tool 1"
            val pin = 0
            val wikiLink = "http://wikiurl"
            val toolType = ToolType.UNLOCK
            val toolState = ToolState.GOOD

            // when
            handleRequest(HttpMethod.Post, "/api/v1/tool") {
                setBody(
                    mapper.writeValueAsString(
                        NewToolDto(
                            deviceDto.id,
                            name,
                            pin,
                            toolType,
                            toolState,
                            wikiLink,
                            listOf(qualificationDto.id)
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<ToolDto>()
                    .isEqualTo(
                        ToolDto(
                            1,
                            deviceDto.id,
                            name,
                            pin,
                            toolType,
                            toolState,
                            wikiLink,
                            listOf(qualificationDto)
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `given tool exists when getting tool then ok`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val deviceDto = givenDevice()
            val qualificationDto = givenQualification()
            val toolDto = givenTool(deviceDto.id)

            // when
            handleRequest(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<ToolDto>()
                    .all {
                        transform { it.deviceId }.isEqualTo(deviceDto.id)
                        transform { it.qualifications }.extracting { it.id }.containsExactly(qualificationDto.id)
                    }
            }
        }

        Unit
    }

    @Test
    fun `given tool when editing single parameter then parameter is changed`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val deviceDto = givenDevice()
            val qualificationDto = givenQualification()
            val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

            val newName = "Edited Tool Name"

            // when
            handleRequest(HttpMethod.Patch, "/api/v1/tool/${toolDto.id}") {
                setBody(
                    mapper.writeValueAsString(
                        EditToolDto(
                            null,
                            newName,
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
            handleRequest(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<ToolDto>()
                    .isEqualTo(toolDto.copy(name = newName))
            }
        }

        Unit
    }

    @Test
    fun `given tool when editing all parameters then parameters are changed`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val deviceDto = givenDevice()

            val deviceDto2 = givenDevice(
                mac = "aaffeeaaffff",
                secret = "newSecret2"
            )

            val qualificationDto = givenQualification()

            val toolDto = givenTool(deviceDto.id)

            val newDeviceId = deviceDto2.id
            val newName = "Edited Tool Name"
            val newPin = 1
            val newToolType = ToolType.KEEP
            val newToolState = ToolState.BAD
            val newWikiLink = "http://newwikiurl"
            val newQualifications = listOf(qualificationDto.id)

            // when
            handleRequest(HttpMethod.Patch, "/api/v1/tool/${toolDto.id}") {
                setBody(
                    mapper.writeValueAsString(
                        EditToolDto(
                            newDeviceId,
                            newName,
                            newPin,
                            newToolType,
                            newToolState,
                            newWikiLink,
                            newQualifications
                        )
                    )
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .readValue<ToolDto>()
                    .isEqualTo(
                        ToolDto(
                            toolDto.id,
                            deviceDto2.id,
                            newName,
                            newPin,
                            newToolType,
                            newToolState,
                            newWikiLink,
                            listOf(qualificationDto)
                        )
                    )
            }
        }

        Unit
    }

    @Test
    fun `given tool when deleting tool then tool no longer exists`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val deviceDto = givenDevice()
            val toolDto = givenTool(deviceDto.id, qualifications = listOf())

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/tool/${toolDto.id}").apply {
                assertThat(response.status()).isOK()
            }

            // then
            handleRequest(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }

        Unit
    }

    @Test
    fun `given tool with qualification when deleting tool then error message`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            // given
            val qualificationDto = givenQualification()
            val deviceDto = givenDevice()
            val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

            // when
            handleRequest(HttpMethod.Delete, "/api/v1/tool/${toolDto.id}").apply {
                // then
                assertThat(response.status())
                    .isNotNull()
                    .isNotSuccess()
                assertThat(response.content)
                    .isNotNull()
                    .contains("FK_TOOLQUALIFICATIONS_TOOL_ID")
            }
        }

        Unit
    }
}
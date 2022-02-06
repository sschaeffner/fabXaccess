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
import cloud.fabx.model.IdleState
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
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
class ToolTest : CommonTest() {

    @Test
    fun `given no tools when getting tools then returns empty list`() = testApp {
        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/tool").apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content).isEqualTo("[]")
        }
    }

    @Test
    fun `when creating tool then tool is created`() = testApp {
        // given
        val deviceDto = givenDevice()
        val qualificationDto = givenQualification()

        val name = "New Tool 1"
        val pin = 0
        val wikiLink = "http://wikiurl"
        val toolType = ToolType.UNLOCK
        val time = 4000
        val idleState = IdleState.IDLE_LOW
        val toolState = ToolState.GOOD

        // when
        handleRequestAsAdmin(HttpMethod.Post, "/api/v1/tool") {
            setBody(
                mapper.writeValueAsString(
                    NewToolDto(
                        deviceDto.id,
                        name,
                        pin,
                        toolType,
                        time,
                        idleState,
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
                        time,
                        idleState,
                        toolState,
                        wikiLink,
                        listOf(qualificationDto)
                    )
                )
        }
    }

    @Test
    fun `given invalid device id when creating tool then BadRequest`() = testApp {
        // given
        val qualificationDto = givenQualification()
        val invalidDeviceId = 42

        // when
        handleRequestAsAdmin(HttpMethod.Post, "/api/v1/tool") {
            setBody(
                mapper.writeValueAsString(
                    NewToolDto(
                        invalidDeviceId,
                        "New Tool 1",
                        0,
                        ToolType.UNLOCK,
                        4200,
                        IdleState.IDLE_LOW,
                        ToolState.GOOD,
                        "http://wikiurl",
                        listOf(qualificationDto.id)
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }.apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.content).isEqualTo("Device with id 42 does not exist")
        }
    }

    @Test
    fun `given tool exists when getting tool then ok`() = testApp {
        // given
        val deviceDto = givenDevice()
        val qualificationDto = givenQualification()
        val toolDto = givenTool(deviceDto.id)

        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
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

    @Test
    fun `given tool when editing single parameter then parameter is changed`() = testApp {
        // given
        val deviceDto = givenDevice()
        val qualificationDto = givenQualification()
        val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        val newName = "Edited Tool Name"

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/tool/${toolDto.id}") {
            setBody(
                mapper.writeValueAsString(
                    EditToolDto(
                        null,
                        newName,
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
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<ToolDto>()
                .isEqualTo(toolDto.copy(name = newName))
        }
    }

    @Test
    fun `given tool when editing all parameters then parameters are changed`() = testApp {
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
        val newTime = 4200
        val newIdleState = IdleState.IDLE_LOW
        val newToolState = ToolState.BAD
        val newWikiLink = "http://newwikiurl"
        val newQualifications = listOf(qualificationDto.id)

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/tool/${toolDto.id}") {
            setBody(
                mapper.writeValueAsString(
                    EditToolDto(
                        newDeviceId,
                        newName,
                        newPin,
                        newToolType,
                        newTime,
                        newIdleState,
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
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
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
                        newTime,
                        newIdleState,
                        newToolState,
                        newWikiLink,
                        listOf(qualificationDto)
                    )
                )
        }
    }

    @Test
    fun `given invalid tool id when editing tool then NotFound`() = testApp {
        // given
        val invalidToolId = 42

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/tool/$invalidToolId") {
            setBody(
                mapper.writeValueAsString(
                    EditToolDto(
                        null,
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
            assertThat(response.content).isEqualTo("Tool does not exist")
        }

    }

    @Test
    fun `given tool when deleting tool then tool no longer exists`() = testApp {
        // given
        val deviceDto = givenDevice()
        val toolDto = givenTool(deviceDto.id, qualifications = listOf())

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/tool/${toolDto.id}").apply {
            assertThat(response.status()).isOK()
        }

        // then
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/tool/${toolDto.id}").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `given tool with qualification when deleting tool then error message`() = testApp {
        // given
        val qualificationDto = givenQualification()
        val deviceDto = givenDevice()
        val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/tool/${toolDto.id}").apply {
            // then
            assertThat(response.status())
                .isNotNull()
                .isNotSuccess()
            assertThat(response.content)
                .isNotNull()
                .contains("FK_TOOLQUALIFICATIONS_TOOL__ID")
        }
    }

    @Test
    fun `given invalid tool id when deleting tool then NotFound`() = testApp {
        // given
        val invalidToolId = 42

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/tool/$invalidToolId").apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            assertThat(response.content).isEqualTo("Tool does not exist")
        }
    }
}
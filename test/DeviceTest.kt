package cloud.fabx

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditDeviceDto
import cloud.fabx.dto.NewDeviceDto
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
class DeviceTest : CommonTest() {

    @Test
    fun `given no devices when getting all devices then return empty list`() = testApp {
        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/device").apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content).isNotNull().isEqualTo("[]")
        }
    }

    @Test
    fun `given valid data when creating device then OK`() = testApp {
        // given
        val name = "New Device 1"
        val mac = "aaffeeaaffee"
        val secret = "newSecret"
        val bgImageUrl = "http://bgurl"
        val backupBackendUrl = "http://fabx.backup"

        val newDeviceDto = NewDeviceDto(
            name,
            mac,
            secret,
            bgImageUrl,
            backupBackendUrl
        )

        // when
        handleRequestAsAdmin(HttpMethod.Post, "/api/v1/device") {
            setBody(
                mapper.writeValueAsString(
                    newDeviceDto
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<DeviceDto>()
                .isEqualTo(
                    DeviceDto(
                        1,
                        name,
                        mac,
                        secret,
                        bgImageUrl,
                        backupBackendUrl,
                        listOf()
                    )
                )
        }
    }

    @Test
    fun `given device when getting device then return device`() = testApp {
        // given
        val deviceDto = givenDevice()

        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/device/${deviceDto.id}").apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<DeviceDto>()
                .isEqualTo(deviceDto)
        }
    }

    @Test
    fun `when editing single parameter of device then device is changed`() = testApp {
        // given
        val deviceDto = givenDevice(
            "New Device 1",
            "aaffeeaaffee",
            "newSecret",
            "http://bgurl",
            "http://fabx.backup"
        )

        val newName = "Edited Devicename 1"

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/device/${deviceDto.id}") {
            setBody(
                mapper.writeValueAsString(
                    EditDeviceDto(
                        newName,
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
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/device/${deviceDto.id}").apply {
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<DeviceDto>()
                .isEqualTo(deviceDto.copy(name = newName))
        }
    }

    @Test
    fun `when editing all parameters of device then device is changed`() = testApp {
        // given
        val deviceDto = givenDevice()

        val newName = "Edited Devicename 1"
        val newMac = "aabbaabbaabb"
        val newSecret = "editedSecret"
        val newBgImageUrl = "http://editedbgurl"
        val newBackupBackendUrl = "http://fabx.other"

        // when
        handleRequestAsAdmin(HttpMethod.Patch, "/api/v1/device/${deviceDto.id}") {
            setBody(
                mapper.writeValueAsString(
                    EditDeviceDto(
                        newName,
                        newMac,
                        newSecret,
                        newBgImageUrl,
                        newBackupBackendUrl
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }.apply {
            assertThat(response.status()).isOK()
        }

        // then
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/device/${deviceDto.id}").apply {
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .readValue<DeviceDto>()
                .isEqualTo(
                    DeviceDto(
                        deviceDto.id,
                        newName,
                        newMac,
                        newSecret,
                        newBgImageUrl,
                        newBackupBackendUrl,
                        listOf()
                    )
                )
        }
    }

    @Test
    fun `when deleting device then device no longer exists`() = testApp {
        // given
        val deviceDto = givenDevice()

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/device/${deviceDto.id}").apply {
            assertThat(response.status()).isOK()
        }

        // then
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/device/${deviceDto.id}").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `given device with tool when deleting device then error message`() = testApp {
        // given
        val qualificationDto = givenQualification()
        val deviceDto = givenDevice()
        givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        // when
        handleRequestAsAdmin(HttpMethod.Delete, "/api/v1/device/${deviceDto.id}").apply {
            // then
            assertThat(response.status())
                .isNotNull()
                .isNotSuccess()
            assertThat(response.content)
                .isNotNull()
                .contains("FK_TOOLS_DEVICE_ID")
        }
    }
}
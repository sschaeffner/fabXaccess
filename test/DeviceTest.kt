package cloud.fabx

import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditDeviceDto
import cloud.fabx.dto.NewDeviceDto
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
class DeviceTest: CommonTest() {

    @Test
    fun givenNoDevicesWhenGetAllDevicesThenReturnsEmptyList() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/v1/device").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun whenCreateDeviceThenOk() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){
            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl",
                        "http://fabx.backup"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, deviceDto.id)
                assertEquals("New Device 1", deviceDto.name)
                assertEquals("aaffeeaaffee", deviceDto.mac)
                assertEquals("newSecret", deviceDto.secret)
                assertEquals("http://bgurl", deviceDto.bgImageUrl)
                assertEquals("http://fabx.backup", deviceDto.backupBackendUrl)
                assertTrue(deviceDto.tools.isEmpty())
            }
        }

        Unit
    }

    @Test
    fun givenDeviceWhenGetDeviceThenReturnsDevice() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){
            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            handleRequest(HttpMethod.Get, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("New Device 1", dto.name)
                assertEquals("aaffeeaaffee", dto.mac )
                assertEquals("newSecret", dto.secret)
                assertEquals("http://bgurl", dto.bgImageUrl)
                assertEquals("http://fabx.backup", dto.backupBackendUrl)
                assertTrue(dto.tools.isEmpty())
            }
        }

        Unit
    }

    @Test
    fun whenEditSingleParameterOfDeviceThenDeviceIsChanged() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){

            val deviceDto = givenDevice(
                "New Device 1",
                "aaffeeaaffee",
                "newSecret",
                "http://bgurl",
                "http://fabx.backup"
            )
            assertEquals(1, deviceDto.id)

            handleRequest(HttpMethod.Patch, "/api/v1/device/1") {
                setBody(mapper.writeValueAsString(
                    EditDeviceDto(
                        "Edited Devicename 1",
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

            handleRequest(HttpMethod.Get, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("Edited Devicename 1", dto.name)
                assertEquals("aaffeeaaffee", dto.mac )
                assertEquals("newSecret", dto.secret)
                assertEquals("http://bgurl", dto.bgImageUrl)
                assertEquals("http://fabx.backup", dto.backupBackendUrl)
                assertTrue(dto.tools.isEmpty())
            }
        }

        Unit
    }

    @Test
    fun whenEditAllParametersOfDeviceThenDeviceIsChanged() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            handleRequest(HttpMethod.Patch, "/api/v1/device/1") {
                setBody(mapper.writeValueAsString(
                    EditDeviceDto(
                        "Edited Devicename 1",
                        "aabbaabbaabb",
                        "editedSecret",
                        "http://editedbgurl",
                        "http://fabx.other"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val dto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, dto.id)
                assertEquals("Edited Devicename 1", dto.name)
                assertEquals("aabbaabbaabb", dto.mac )
                assertEquals("editedSecret", dto.secret)
                assertEquals("http://editedbgurl", dto.bgImageUrl)
                assertEquals("http://fabx.other", dto.backupBackendUrl)
                assertTrue(dto.tools.isEmpty())
            }
        }

        Unit
    }

    @Test
    fun whenDeleteDeviceThenDeviceNoLongerExists() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            handleRequest(HttpMethod.Delete, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        Unit
    }
}
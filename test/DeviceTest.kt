package cloud.fabx

import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditDeviceDto
import cloud.fabx.dto.NewDeviceDto
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
class DeviceTest: CommonTest() {

    @Test
    fun testGetAllDevices() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false) }) {
            handleRequest(HttpMethod.Get, "/api/v1/device").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[]", response.content)
            }
        }

        Unit
    }

    @Test
    fun testCreateAndGetDevice() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){
            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
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
                assertTrue(deviceDto.tools.isEmpty())
            }

            handleRequest(HttpMethod.Get, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, deviceDto.id)
                assertEquals("New Device 1", deviceDto.name)
                assertEquals("aaffeeaaffee", deviceDto.mac )
                assertEquals("newSecret", deviceDto.secret)
                assertEquals("http://bgurl", deviceDto.bgImageUrl)
                assertTrue(deviceDto.tools.isEmpty())
            }
        }

        Unit
    }

    @Test
    fun testEditDeviceSingleParameter() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){
            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Patch, "/api/v1/device/1") {
                setBody(mapper.writeValueAsString(
                    EditDeviceDto(
                        "Edited Devicename 1",
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

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, deviceDto.id)
                assertEquals("Edited Devicename 1", deviceDto.name)
                assertEquals("aaffeeaaffee", deviceDto.mac )
                assertEquals("newSecret", deviceDto.secret)
                assertEquals("http://bgurl", deviceDto.bgImageUrl)
                assertTrue(deviceDto.tools.isEmpty())
            }
        }

        Unit
    }

    @Test
    fun testEditDeviceAllParameters() = runBlocking {
        withTestApplication ({ module(demoContent = false, apiAuthentication = false) }){
            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/v1/device") {
                setBody(mapper.writeValueAsString(
                    NewDeviceDto(
                        "New Device 1",
                        "aaffeeaaffee",
                        "newSecret",
                        "http://bgurl"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Patch, "/api/v1/device/1") {
                setBody(mapper.writeValueAsString(
                    EditDeviceDto(
                        "Edited Devicename 1",
                        "aabbaabbaabb",
                        "editedSecret",
                        "http://editedbgurl"
                    )
                ))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, "/api/v1/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(1, deviceDto.id)
                assertEquals("Edited Devicename 1", deviceDto.name)
                assertEquals("aabbaabbaabb", deviceDto.mac )
                assertEquals("editedSecret", deviceDto.secret)
                assertEquals("http://editedbgurl", deviceDto.bgImageUrl)
                assertTrue(deviceDto.tools.isEmpty())
            }
        }

        Unit
    }
}
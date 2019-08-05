package cloud.fabx

import cloud.fabx.dto.DeviceDto
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
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceTest {
    @Test
    fun testCreateAndGetDevice() {
        withTestApplication ({ module(testing = true) }){
            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/device") {
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

                assertEquals(deviceDto.name, "New Device 1")
                assertEquals(deviceDto.mac, "aaffeeaaffee")
                assertEquals(deviceDto.secret, "newSecret")
                assertEquals(deviceDto.bgImageUrl, "http://bgurl")
                assertTrue(deviceDto.tools.isEmpty())
            }

            handleRequest(HttpMethod.Get, "/api/device/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val deviceDto = mapper.readValue<DeviceDto>(response.content!!)

                assertEquals(deviceDto.name, "New Device 1")
                assertEquals(deviceDto.mac, "aaffeeaaffee")
                assertEquals(deviceDto.secret, "newSecret")
                assertEquals(deviceDto.bgImageUrl, "http://bgurl")
                assertTrue(deviceDto.tools.isEmpty())
            }
        }
    }
}
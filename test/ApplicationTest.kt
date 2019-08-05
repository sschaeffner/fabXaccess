package cloud.fabx

import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest {
    @Test
    fun testCreateAndGetUser() {
        withTestApplication({ module(testing = true) }) {

            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/user") {
                setBody(mapper.writeValueAsString(NewUserDto(
                    "New User 1",
                    "newUserWikiName",
                    "123456",
                    "aabbccdd"
                )))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(userDto.id, 1)
                assertEquals(userDto.name, "New User 1")
                assertEquals(userDto.wikiName, "newUserWikiName")
                assertEquals(userDto.phoneNumer, "123456")
                assertEquals(userDto.locked, false)
                assertTrue(userDto.lockedReason!!.isEmpty())
                assertEquals(userDto.cardId, "aabbccdd")
                assertEquals(userDto.permissions.size, 0)
            }

            handleRequest(HttpMethod.Get, "/api/user/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.isNotEmpty())

                val userDto = mapper.readValue<UserDto>(response.content!!)

                assertEquals(userDto.id, 1)
                assertEquals(userDto.name, "New User 1")
                assertEquals(userDto.wikiName, "newUserWikiName")
                assertEquals(userDto.phoneNumer, "123456")
                assertEquals(userDto.locked, false)
                assertTrue(userDto.lockedReason!!.isEmpty())
                assertEquals(userDto.cardId, "aabbccdd")
                assertEquals(userDto.permissions.size, 0)

            }
        }
    }

    @Test
    fun testCreateAndGetDevice() {
        withTestApplication ({ module(testing = true) }){
            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/device") {
                setBody(mapper.writeValueAsString(NewDeviceDto(
                    "New Device 1",
                    "aaffeeaaffee",
                    "newSecret",
                    "http://bgurl"
                )))
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

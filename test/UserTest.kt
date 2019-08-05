package cloud.fabx

import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.UserDto
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

class UserTest {
    @Test
    fun testCreateAndGetUser() {
        withTestApplication({ module(testing = true) }) {

            val mapper = jacksonObjectMapper()

            handleRequest(HttpMethod.Post, "/api/user") {
                setBody(mapper.writeValueAsString(
                    NewUserDto(
                        "New User 1",
                        "newUserWikiName",
                        "123456",
                        "aabbccdd"
                    )
                ))
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
}
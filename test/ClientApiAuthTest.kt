package cloud.fabx

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

@InternalAPI
@KtorExperimentalAPI
class ClientApiAuthTest: CommonTest() {

    @Test
    fun givenNoAuthenticationWhenGetConfigThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {

            val deviceDto = givenDevice(mac ="aaffeeaaffee")
            assertEquals(1, deviceDto.id)

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/config").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            Unit
        }
    }

    @Test
    fun givenAuthenticationWhenGetConfigThenOk() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {

            val deviceDto = givenDevice(
                mac = "aaffeeaaffee",
                secret = "newSecret"
            )
            assertEquals(1, deviceDto.id)

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/config"){
                addBasicAuth("aaffeeaaffee", "newSecret")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("New Device 1\nhttp://bgurl\nhttp://fabx.backup\n", response.content)
            }

            Unit
        }
    }

    @Test
    fun givenNoAuthenticationWhenGetPermissionsThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {

            val userDto = givenUser()
            assertEquals(1, userDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            givenUserHasQualification(1, 1)

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/permissions/aabbccdd/11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            Unit
        }
    }

    @Test
    fun givenAuthenticationWhenGetPermissionsThenOk() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {

            val userDto = givenUser()
            assertEquals(1, userDto.id)

            givenCardForUser(1,
                "aabbccdd",
                "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF")

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            // CREATE DEVICE
            val deviceDto = givenDevice(
                mac = "aaffeeaaffee",
                secret = "newSecret"
            )
            assertEquals(1, deviceDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            givenUserHasQualification(1, 1)

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/permissions/aabbccdd/11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF") {
                addBasicAuth("aaffeeaaffee", "newSecret")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("1", response.content?.trim())
            }

            Unit
        }
    }

    @Test
    fun whenAuthenticatingWithWrongSecretThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = true) }) {

            givenDevice(
                mac = "aaffeeaaffee",
                secret = "newSecret"
            )

            handleRequest(HttpMethod.Get, "/clientApi/v1/aabbccaabbcc/config") {
                addBasicAuth("aaffeeaaffee", "someothersecret")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            Unit
        }
    }
}
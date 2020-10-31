package cloud.fabx

import cloud.fabx.model.ToolType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class ClientApiTest: CommonTest() {

    @Test
    fun givenUserHasQualificationThenReturnsToolId() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = false) }) {

            val userDto = givenUser()
            assertEquals(1, userDto.id)

            givenCardForUser(1,
                "aabbccdd",
                "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"
            )

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val deviceDto = givenDevice()
            assertEquals(1, deviceDto.id)

            val toolDto = givenTool(1)
            assertEquals(1, toolDto.id)

            givenUserHasQualification(1, 1)

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/permissions/aabbccdd/11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                assertEquals("1", response.content?.trim())
            }
        }

        Unit
    }

    @Test
    fun givenAuthenticationWhenGetClientConfigThenReturnsConfig() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = false, clientApiAuthentication = false) }) {

            val deviceDto = givenDevice(
                mac = "aaffeeaaffee",
                secret = "newSecret"
            )
            assertEquals(1, deviceDto.id)

            val qualificationDto = givenQualification()
            assertEquals(1, qualificationDto.id)

            val toolDto1 = givenTool(
                1,
                "New Tool 1",
                0,
                ToolType.UNLOCK
            )
            assertEquals(1, toolDto1.id)

            val toolDto2 = givenTool(
                1,
                "New Tool 2",
                1,
                ToolType.UNLOCK
            )
            assertEquals(2, toolDto2.id)

            handleRequest(HttpMethod.Get, "/clientApi/v1/aaffeeaaffee/config").apply {
                assertEquals("New Device 1\nhttp://bgurl\nhttp://fabx.backup\n1,0,UNLOCK,New Tool 1\n2,1,UNLOCK,New Tool 2", response.content?.trim())
            }
        }

        Unit
    }
}
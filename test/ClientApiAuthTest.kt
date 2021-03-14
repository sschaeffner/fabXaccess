package cloud.fabx

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import isOK
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

@InternalAPI
@KtorExperimentalAPI
class ClientApiAuthTest : CommonTest() {

    @Test
    fun `given no authentication when getting config then unauthorized`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = true
            )
        }) {
            // given
            val mac = "aaffeeaaffee"
            givenDevice(mac = mac)

            // when
            handleRequest(HttpMethod.Get, "/clientApi/v1/${mac}/config").apply {
                // then
                assertThat(response.status())
                    .isEqualTo(HttpStatusCode.Unauthorized)
            }
        }

        Unit
    }

    @Test
    fun `given valid authentication when getting config then OK`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = true
            )
        }) {
            // given
            val mac = "aaffeeaaffee"
            val secret = "newSecret"
            givenDevice(mac = mac, secret = secret)

            // when
            handleRequest(HttpMethod.Get, "/clientApi/v1/${mac}/config") {
                addBasicAuth(mac, secret)
            }.apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isEqualTo("New Device 1\nhttp://bgurl\nhttp://fabx.backup\n")
            }
        }

        Unit
    }

    @Test
    fun `given no authentication when getting permissions then Unauthorized`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = true
            )
        }) {
            // given
            val mac = "aaffeeaaffee"
            val userDto = givenUser()
            val qualificationDto = givenQualification()
            val deviceDto = givenDevice(mac = mac)
            givenTool(deviceDto.id)
            givenUserHasQualification(userDto.id, qualificationDto.id)

            val invalidCardUid = "aabbccdd"
            val invalidCardSecret = "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"

            // when
            handleRequest(
                HttpMethod.Get,
                "/clientApi/v1/${mac}/permissions/${invalidCardUid}/${invalidCardSecret}"
            ).apply {
                // then
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }

        Unit
    }

    @Test
    fun `given authentication when getting permissions then OK`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = true
            )
        }) {
            // given
            val userDto = givenUser()

            val cardId = "aabbccdd"
            val cardSecret = "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"
            givenCardForUser(
                userDto.id,
                cardId,
                cardSecret
            )

            val qualificationDto = givenQualification()

            val mac = "aaffeeaaffee"
            val secret = "newSecret"
            val deviceDto = givenDevice(
                mac = mac,
                secret = secret
            )

            val toolDto = givenTool(deviceDto.id)

            givenUserHasQualification(userDto.id, qualificationDto.id)

            handleRequest(HttpMethod.Get, "/clientApi/v1/${mac}/permissions/${cardId}/${cardSecret}") {
                addBasicAuth(mac, secret)
            }.apply {
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .contains("${toolDto.id}")
            }
        }

        Unit
    }

    @Test
    fun `given invalid authentication when getting config then Unauthorized`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = true
            )
        }) {

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
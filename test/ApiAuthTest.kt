package cloud.fabx

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpHeaders
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

@KtorExperimentalAPI
class ApiAuthTest : CommonTest() {

    @Test
    fun `given no authentication when getting all users then Unauthorized`() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = true) }) {
            //when
            handleRequest(HttpMethod.Get, "/api/v1/user").apply {
                // then
                assertThat(response.headers[HttpHeaders.WWWAuthenticate])
                    .isEqualTo("Basic realm=\"fabX access API\", charset=UTF-8")
                assertThat(response.status())
                    .isEqualTo(HttpStatusCode.Unauthorized)
            }
        }

        Unit
    }

    @InternalAPI
    @Test
    fun `given valid authentication when getting all users then OK`() = runBlocking {
        withTestApplication({ module(demoContent = true, apiAuthentication = true) }) {
            // when
            handleRequest(HttpMethod.Get, "/api/v1/user") {
                addBasicAuth("admin1", "demopassword")
            }.apply {
                // then
                assertThat(response.status()).isOK()
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        Unit
    }

    @InternalAPI
    @Test
    fun `given invalid authentication when getting all users then Unauthorized`() = runBlocking {
        withTestApplication({ module(demoContent = true, apiAuthentication = true) }) {
            // when
            handleRequest(HttpMethod.Get, "/api/v1/user") {
                addBasicAuth("admin1", "blub")
            }.apply {
                // then
                assertThat(response.status())
                    .isEqualTo(HttpStatusCode.Unauthorized)
            }
        }

        Unit
    }
}
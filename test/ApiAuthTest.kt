package cloud.fabx

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import isOK
import org.junit.Test

@InternalAPI
@KtorExperimentalAPI
class ApiAuthTest : CommonTest() {

    @Test
    fun `given no authentication when getting all users then Unauthorized`() = testApp {
        //when
        handleRequest(HttpMethod.Get, "/api/v1/user").apply {
            // then
            assertThat(response.headers[HttpHeaders.WWWAuthenticate])
                .isEqualTo("Basic realm=\"fabX access API\", charset=UTF-8")
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun `given valid authentication when getting all users then OK`() = testApp {
        // when
        handleRequest(HttpMethod.Get, "/api/v1/user") {
            addTestAdminAuth()
        }.apply {
            // then
            assertThat(response.status()).isOK()
        }
    }

    @Test
    fun `given valid authentication when getting info then OK`() = testApp {
        // when
        handleRequestAsAdmin(HttpMethod.Get, "/api/v1/info").apply {
            // then
            assertThat(response.status()).isOK()
        }
    }

    @Test
    fun `given invalid authentication when getting all users then Unauthorized`() = testApp {
        // when
        handleRequest(HttpMethod.Get, "/api/v1/user") {
            addBasicAuth("admin", "blub")
        }.apply {
            // then
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.Unauthorized)
        }
    }
}
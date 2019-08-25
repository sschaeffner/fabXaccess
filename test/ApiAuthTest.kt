package cloud.fabx

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
class ApiAuthTest: CommonTest() {

    @Test
    fun getAllUsersWithoutAuthenticationThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = true) }) {
            handleRequest(HttpMethod.Get, "/api/user").apply {
                assertEquals("Basic realm=\"fabX access API\", charset=UTF-8", response.headers[HttpHeaders.WWWAuthenticate])
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        Unit
    }

    @InternalAPI
    @Test
    fun getAllUsersWithAuthentication() = runBlocking {
        withTestApplication({ module(demoContent = true, apiAuthentication = true) }) {
            handleRequest(HttpMethod.Get, "/api/user") {
                addBasicAuth("admin1", "demopassword")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        Unit
    }

    @InternalAPI
    @Test
    fun getAllUsersWithInvalidAuthenticationThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = true, apiAuthentication = true) }) {
            handleRequest(HttpMethod.Get, "/api/user") {
                addBasicAuth("admin1", "blub")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        Unit
    }

    @InternalAPI
    private fun TestApplicationRequest.addBasicAuth(user: String, password: String) {
        val encoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()
        addHeader(HttpHeaders.Authorization, "Basic $encoded")
    }
}
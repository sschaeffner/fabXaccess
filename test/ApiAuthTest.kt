package cloud.fabx

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class ApiAuthTest: CommonTest() {

    @Test
    fun givenNoAuthenticationWhenGetAllUsersThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = false, apiAuthentication = true) }) {
            handleRequest(HttpMethod.Get, "/api/v1/user").apply {
                assertEquals("Basic realm=\"fabX access API\", charset=UTF-8", response.headers[HttpHeaders.WWWAuthenticate])
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        Unit
    }

    @InternalAPI
    @Test
    fun givenValidAuthenticationWhenGetAllUsersThenOk() = runBlocking {
        withTestApplication({ module(demoContent = true, apiAuthentication = true) }) {
            handleRequest(HttpMethod.Get, "/api/v1/user") {
                addBasicAuth("admin1", "demopassword")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        Unit
    }

    @InternalAPI
    @Test
    fun givenInvalidAuthenticationWhenGetAllUsersThenUnauthorized() = runBlocking {
        withTestApplication({ module(demoContent = true, apiAuthentication = true) }) {
            handleRequest(HttpMethod.Get, "/api/v1/user") {
                addBasicAuth("admin1", "blub")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        Unit
    }
}
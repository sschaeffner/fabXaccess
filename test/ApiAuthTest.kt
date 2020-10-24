package cloud.fabx

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
class ApiAuthTest: CommonTest() {

    @Test
    fun getAllUsersWithoutAuthenticationThenUnauthorized() = runBlocking {
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
    fun getAllUsersWithAuthentication() = runBlocking {
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
    fun getAllUsersWithInvalidAuthenticationThenUnauthorized() = runBlocking {
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
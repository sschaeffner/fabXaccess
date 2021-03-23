package cloud.fabx

import assertk.Assert
import assertk.assertThat
import cloud.fabx.db.DbHandler
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditUserDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.dto.NewQualificationDto
import cloud.fabx.dto.NewToolDto
import cloud.fabx.dto.NewUserDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.dto.UserDto
import cloud.fabx.dto.UserQualificationDto
import cloud.fabx.model.Admins
import cloud.fabx.model.Devices
import cloud.fabx.model.Qualifications
import cloud.fabx.model.ToolQualifications
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
import cloud.fabx.model.Tools
import cloud.fabx.model.UserQualifications
import cloud.fabx.model.Users
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.encodeBase64
import isOK
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before

@InternalAPI
@KtorExperimentalAPI
open class CommonTest {
    protected val mapper = jacksonObjectMapper()

    @Before
    fun before() = transaction(DbHandler.db) {
        println("BeforeEach")

        SchemaUtils.drop(Devices, Tools, Users, Admins, Qualifications, UserQualifications, ToolQualifications)
        SchemaUtils.create(Devices, Tools, Users, Admins, Qualifications, UserQualifications, ToolQualifications)
    }

    @InternalAPI
    protected fun TestApplicationRequest.addBasicAuth(user: String, password: String) {
        val encoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()
        addHeader(HttpHeaders.Authorization, "Basic $encoded")
    }

    protected fun TestApplicationRequest.addTestAdminAuth() = addBasicAuth("admin", "password")

    protected fun testApp(test: TestApplicationEngine.() -> Unit) {
        return withTestApplication({ module(testAdmin = true) }) {
            test()
        }
    }

    protected fun TestApplicationEngine.handleRequestAsAdmin(
        method: HttpMethod,
        uri: String,
        setup: TestApplicationRequest.() -> Unit = {}
    ): TestApplicationCall {
        return handleRequest(method, uri) {
            addTestAdminAuth()
            setup()
        }
    }

    protected inline fun <reified T> Assert<String>.readValue(): Assert<T> =
        transform { mapper.readValue(it) }

    protected fun TestApplicationEngine.givenUser(
        firstName: String = "New User 1",
        lastName: String = "New User 1 LastName",
        wikiName: String = "newUserWikiName",
        phoneNumber: String = "123456"
    ): UserDto {
        handleRequest(HttpMethod.Post, "/api/v1/user") {
            setBody(
                mapper.writeValueAsString(
                    NewUserDto(firstName, lastName, wikiName, phoneNumber)
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
            return mapper.readValue(response.content!!)
        }
    }

    protected fun TestApplicationEngine.givenCardForUser(
        userId: Int,
        cardId: String,
        cardSecret: String
    ) {
        handleRequest(HttpMethod.Patch, "/api/v1/user/$userId") {
            setBody(
                mapper.writeValueAsString(
                    EditUserDto(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        cardId,
                        cardSecret
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
        }
    }

    protected fun TestApplicationEngine.givenLockStateForUser(
        userId: Int,
        locked: Boolean,
        lockedReason: String = ""
    ) {
        handleRequest(HttpMethod.Patch, "/api/v1/user/$userId") {
            setBody(
                mapper.writeValueAsString(
                    EditUserDto(
                        null,
                        null,
                        null,
                        null,
                        locked,
                        lockedReason,
                        null,
                        null
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
        }
    }

    protected fun TestApplicationEngine.givenQualification(
        name: String = "New Qualification 1",
        description: String = "A Qualification",
        colour: String = "#000000",
        orderNr: Int = 1
    ): QualificationDto {
        handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            setBody(
                mapper.writeValueAsString(
                    NewQualificationDto(name, description, colour, orderNr)
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
            return mapper.readValue(response.content!!)
        }
    }

    protected fun TestApplicationEngine.givenDevice(
        name: String = "New Device 1",
        mac: String = "aaffeeaaffee",
        secret: String = "newSecret",
        bgImageUrl: String = "http://bgurl",
        backupBackendUrl: String = "http://fabx.backup"
    ): DeviceDto {
        handleRequest(HttpMethod.Post, "/api/v1/device") {
            setBody(
                mapper.writeValueAsString(
                    NewDeviceDto(name, mac, secret, bgImageUrl, backupBackendUrl)
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
            return mapper.readValue(response.content!!)
        }
    }

    protected fun TestApplicationEngine.givenTool(
        deviceId: Int,
        name: String = "New Tool 1",
        pin: Int = 0,
        type: ToolType = ToolType.UNLOCK,
        state: ToolState = ToolState.GOOD,
        wikiLink: String = "http://wikiurl",
        qualifications: List<Int> = listOf(1)
    ): ToolDto {
        handleRequest(HttpMethod.Post, "/api/v1/tool") {
            setBody(
                mapper.writeValueAsString(
                    NewToolDto(
                        deviceId,
                        name,
                        pin,
                        type,
                        state,
                        wikiLink,
                        qualifications
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
            return mapper.readValue(response.content!!)
        }
    }

    protected fun TestApplicationEngine.givenUserHasQualification(
        userId: Int,
        qualificationId: Int
    ) {
        handleRequest(HttpMethod.Post, "/api/v1/user/$userId/qualifications") {
            setBody(
                mapper.writeValueAsString(
                    UserQualificationDto(
                        userId,
                        qualificationId
                    )
                )
            )
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addTestAdminAuth()
        }.apply {
            assertThat(response.status()).isOK()
        }
    }
}
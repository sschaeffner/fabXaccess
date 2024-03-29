package cloud.fabx

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabx.model.IdleState
import cloud.fabx.model.ToolState
import cloud.fabx.model.ToolType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import isOK
import org.junit.Test

@InternalAPI
@KtorExperimentalAPI
class ClientApiV2Test : CommonTest() {

    @Test
    fun `given valid authentication when getting client config then return config`() = testApp {
        // given
        val mac = "aaffeeaaffee"
        val secret = "newSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        val qualificationDto = givenQualification()

        val toolDto1 = givenTool(
            deviceDto.id,
            "New Tool 1",
            0,
            ToolType.UNLOCK,
            4200,
            IdleState.IDLE_LOW,
            qualifications = listOf(qualificationDto.id)
        )

        val toolDto2 = givenTool(
            deviceDto.id,
            "New Tool 2",
            1,
            ToolType.UNLOCK,
            8910,
            IdleState.IDLE_HIGH,
            qualifications = listOf(qualificationDto.id)
        )

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/config") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .isEqualTo(
                    "New Device 1\nhttp://bgurl\nhttp://fabx.backup\n"
                            + "${toolDto1.id},0,UNLOCK,4200,IDLE_LOW,New Tool 1\n"
                            + "${toolDto2.id},1,UNLOCK,8910,IDLE_HIGH,New Tool 2\n"
                )
        }
    }

    @Test
    fun `given no attached tools when getting config then Unauthorized`() = testApp {
        // given
        val mac = "aaffeeaaffee"
        val secret = "newSecret"
        givenDevice(
            mac = mac,
            secret = secret
        )

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v1/${mac}/config") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.content).isNull()
        }
    }

    @Test
    fun `given new device when getting config then create device and return config`() = testApp {
        // given
        val mac = "aaffeeaaffee"
        val secret = "aSecret42"

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/config") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .isEqualTo(
                    "new device ${mac}\n\n\n"
                )
        }
    }

    @Test
    fun `given no authentication when getting config then unauthorized`() = testApp {
        // given
        val mac = "aaffeeaaffee"
        givenDevice(mac = mac)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/config").apply {
            // then
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun `given invalid authentication when getting config then Unauthorized`() = testApp {
        // given
        val mac = "aaffeeaaffee"
        val secret = "newSecret"
        givenDevice(
            mac = mac,
            secret = secret
        )

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/config") {
            addBasicAuth(mac, "someothersecret")
        }.apply {
            // then
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun `given authentication for other device when getting then Forbidden`() = testApp {
        // given
        val mac = "aaffeeaaffee"
        val secret = "newSecret"
        givenDevice(mac = mac, secret = secret)

        val otherMac = "aabbccddeeff"
        val otherSecret = "otherSecret"
        val otherDevice = givenDevice(mac = otherMac, secret = otherSecret)
        givenTool(
            otherDevice.id,
            qualifications = listOf()
        )

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/config") {
            addBasicAuth(otherMac, otherSecret)
        }.apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            assertThat(response.content).isEqualTo("Given mac has to match authentication")
        }
    }

    @Test
    fun `given user has qualification when getting permissions then return toolId`() = testApp {
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
        val secret = "someSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        givenUserHasQualification(userDto.id, qualificationDto.id)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/permissions/${cardId}/${cardSecret}") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .contains("${toolDto.id}")
        }
    }

    @Test
    fun `given locked user when getting permissions then return no tool ids`() = testApp {
        // given
        val userDto = givenUser()
        givenLockStateForUser(userDto.id, true)

        val cardId = "aabbccdd"
        val cardSecret = "11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF"
        givenCardForUser(
            userDto.id,
            cardId,
            cardSecret
        )

        val qualificationDto = givenQualification()

        val mac = "aaffeeaaffee"
        val secret = "someSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        givenUserHasQualification(userDto.id, qualificationDto.id)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/permissions/${cardId}/${cardSecret}") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .isEmpty()
        }
    }

    @Test
    fun `given disabled tool when getting permissions then not return disabled tool id`() = testApp {
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
        givenUserHasQualification(userDto.id, qualificationDto.id)

        val mac = "aaffeeaaffee"
        val secret = "someSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        val enabledToolDto =
            givenTool(deviceDto.id, name = "enabled tool", pin = 0, qualifications = listOf(qualificationDto.id))
        val disabledToolDto =
            givenTool(deviceDto.id, name = "disabled tool", pin = 1, qualifications = listOf(qualificationDto.id))
        givenStateForTool(disabledToolDto.id, ToolState.DISABLED)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/permissions/${cardId}/${cardSecret}") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .all {
                    contains("${enabledToolDto.id}")
                    doesNotContain("${disabledToolDto.id}")
                }
        }
    }

    @Test
    fun `given user has qualification when getting permissions via phone number then return toolId`() = testApp {
        // given
        val phoneNumber = "+4912345"
        val userDto = givenUser(phoneNumber = phoneNumber)

        val qualificationDto = givenQualification()

        val mac = "aaffeeaaffee"
        val secret = "someSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        givenUserHasQualification(userDto.id, qualificationDto.id)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/permissions/${phoneNumber}") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .contains("${toolDto.id}")
        }
    }

    @Test
    fun `given locked user when getting permissions via phone number then return no tool ids`() = testApp {
        // given
        val phoneNumber = "+4298765"
        val userDto = givenUser(phoneNumber = phoneNumber)
        givenLockStateForUser(userDto.id, true)

        val qualificationDto = givenQualification()

        val mac = "aaffeeaaffee"
        val secret = "someSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

        givenUserHasQualification(userDto.id, qualificationDto.id)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/permissions/${phoneNumber}") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .isEmpty()
        }
    }

    @Test
    fun `given disabled tool when getting permissions via phone number then not return disabled tool id`() = testApp {
        // given
        val phoneNumber = "+4298765432"
        val userDto = givenUser(phoneNumber = phoneNumber)

        val qualificationDto = givenQualification()
        givenUserHasQualification(userDto.id, qualificationDto.id)

        val mac = "aaffeeaaffee"
        val secret = "someSecret"
        val deviceDto = givenDevice(
            mac = mac,
            secret = secret
        )

        val enabledToolDto =
            givenTool(deviceDto.id, name = "enabled tool", pin = 0, qualifications = listOf(qualificationDto.id))
        val disabledToolDto =
            givenTool(deviceDto.id, name = "disabled tool", pin = 1, qualifications = listOf(qualificationDto.id))
        givenStateForTool(disabledToolDto.id, ToolState.DISABLED)

        // when
        handleRequest(HttpMethod.Get, "/clientApi/v2/${mac}/permissions/${phoneNumber}") {
            addBasicAuth(mac, secret)
        }.apply {
            // then
            assertThat(response.status()).isOK()
            assertThat(response.content)
                .isNotNull()
                .all {
                    contains("${enabledToolDto.id}")
                    doesNotContain("${disabledToolDto.id}")
                }
        }
    }

    @Test
    fun `given no authentication when getting permissions then Unauthorized`() = testApp {
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
            "/clientApi/v2/${mac}/permissions/${invalidCardUid}/${invalidCardSecret}"
        ).apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun `given no attached tools when getting permissions then Unauthorized`() = testApp {
        // given
        val userDto = givenUser()

        val mac = "aaffeeaaffee"
        val secret = "newSecret"
        givenDevice(
            mac = mac,
            secret = secret
        )

        // when
        handleRequest(
            HttpMethod.Get,
            "/clientApi/v1/${mac}/permissions/${userDto.cardId}/${userDto.cardSecret}"
        ).apply {
            // then
            assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }
}
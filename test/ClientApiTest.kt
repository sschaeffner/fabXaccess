package cloud.fabx

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabx.model.ToolType
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import isOK
import kotlinx.coroutines.runBlocking
import org.junit.Test

@KtorExperimentalAPI
class ClientApiTest : CommonTest() {

    @Test
    fun `given user has qualification when getting permissions then return toolId`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = false
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
            val deviceDto = givenDevice(
                mac = mac
            )

            val toolDto = givenTool(deviceDto.id, qualifications = listOf(qualificationDto.id))

            givenUserHasQualification(userDto.id, qualificationDto.id)

            // when
            handleRequest(HttpMethod.Get, "/clientApi/v1/${mac}/permissions/${cardId}/${cardSecret}").apply {
                // then
                assertThat(response.status()).isOK()
                assertThat(response.content)
                    .isNotNull()
                    .contains("${toolDto.id}")
            }
        }

        Unit
    }

    @Test
    fun `given authentication when getting client config then return config`() = runBlocking {
        withTestApplication({
            module(
                demoContent = false,
                apiAuthentication = false,
                clientApiAuthentication = false
            )
        }) {
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
                qualifications = listOf(qualificationDto.id)
            )

            val toolDto2 = givenTool(
                deviceDto.id,
                "New Tool 2",
                1,
                ToolType.UNLOCK,
                qualifications = listOf(qualificationDto.id)
            )

            // when
            handleRequest(HttpMethod.Get, "/clientApi/v1/${mac}/config").apply {
                // then
                assertThat(response.content)
                    .isNotNull()
                    .isEqualTo(
                        "New Device 1\nhttp://bgurl\nhttp://fabx.backup\n"
                                + "${toolDto1.id},0,UNLOCK,New Tool 1\n"
                                + "${toolDto2.id},1,UNLOCK,New Tool 2\n"
                    )
            }
        }

        Unit
    }
}
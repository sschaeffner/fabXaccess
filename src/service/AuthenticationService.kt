package cloud.fabx.service

import cloud.fabx.application.AdminPrincipal
import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.model.Admin
import cloud.fabx.model.Admins
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getDigestFunction
import java.util.Base64
import org.jetbrains.exposed.sql.and

@KtorExperimentalAPI
class AuthenticationService {

    private val digestFunction = getDigestFunction("SHA-256") { "fabXfabXfabX${it.length}" }

    suspend fun checkAdminCredentials(username: String, password: String): AdminPrincipal? {
        val hash = digestFunction.invoke(password)
        val encodedHash = Base64.getEncoder().encodeToString(hash)

        return findAdmin(username, encodedHash)?.let {
            AdminPrincipal(it.name)
        }
    }

    private suspend fun findAdmin(username: String, encodedHash: String): Admin? = dbQuery {
        Admin.find {
            (Admins.name eq username) and
            (Admins.passwordHash eq encodedHash)
        }.firstOrNull()
    }
}
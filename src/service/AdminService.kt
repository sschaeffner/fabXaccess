package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.model.Admin
import cloud.fabx.model.Admins
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getDigestFunction
import org.jetbrains.exposed.sql.and
import java.util.*

@KtorExperimentalAPI
class AdminService {

    private val digestFunction = getDigestFunction("SHA-256") { "fabXfabXfabX${it.length}" }

    suspend fun checkAdminCredentials(username: String, password: String): Boolean = dbQuery{
        val hash = digestFunction.invoke(password)
        val encodedHash = Base64.getEncoder().encodeToString(hash)

        val admin = Admin.find {
            (Admins.name eq username) and
            (Admins.passwordHash eq encodedHash)
        }.firstOrNull()

        admin != null
    }
}
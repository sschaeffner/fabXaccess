package cloud.fabx.db

import cloud.fabx.model.Admins
import cloud.fabx.model.Devices
import cloud.fabx.model.Qualifications
import cloud.fabx.model.ToolQualifications
import cloud.fabx.model.Tools
import cloud.fabx.model.UserQualifications
import cloud.fabx.model.Users
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


object DbHandler {
    private var dbUrl: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    private var dbDriver: String = "org.h2.Driver"
    private var dbUser: String? = null
    private var dbPassword: String? = null

    fun configure(herokuDbUrl: String) {
        this.dbDriver = "org.postgresql.Driver"

        val dbUri = URI(herokuDbUrl)

        this.dbUser = dbUri.userInfo.split(":")[0]
        this.dbPassword = dbUri.userInfo.split(":")[1]
        this.dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
    }

    fun configure(jdbcUrl: String, dbUser: String, dbPassword: String) {
        this.dbDriver = "org.postgresql.Driver"

        this.dbUrl = jdbcUrl
        this.dbUser = dbUser
        this.dbPassword = dbPassword
    }

    val db by lazy {
        val db =
            if (dbUser != null && dbPassword != null) {
                Database.connect(this.dbUrl, driver = this.dbDriver, user = dbUser!!, password = dbPassword!!)
            } else {
                Database.connect(this.dbUrl, driver = this.dbDriver)
            }

        transaction {
            SchemaUtils.create(
                Users,
                Devices,
                Tools,
                Admins,
                Qualifications,
                UserQualifications,
                ToolQualifications
            )
        }

        db
    }

    suspend fun <T> dbQuery(block: () -> T) = withContext(Dispatchers.IO) {
        transaction(db) {
            block()
        }
    }
}

package cloud.fabx.db

import cloud.fabx.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI


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
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Users)
            SchemaUtils.create(Devices)
            SchemaUtils.create(Tools)
            SchemaUtils.create(Admins)
            SchemaUtils.create(Qualifications)
            SchemaUtils.create(UserQualifications)
            SchemaUtils.create(ToolQualifications)
        }

        db
    }

    suspend fun <T> dbQuery(block: () -> T) = withContext(Dispatchers.IO) {
            transaction(db) {
                block()
            }
        }
}
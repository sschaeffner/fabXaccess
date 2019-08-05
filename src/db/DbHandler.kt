package cloud.fabx.db

import cloud.fabx.model.Devices
import cloud.fabx.model.Tools
import cloud.fabx.model.UserPermissions
import cloud.fabx.model.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DbHandler {
    val db by lazy {
        val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Users)
            SchemaUtils.create(Devices)
            SchemaUtils.create(Tools)
            SchemaUtils.create(UserPermissions)
        }

        db
    }

    suspend fun <T> dbQuery(block: () -> T) = withContext(Dispatchers.IO) {
            transaction(db) {
                block()
            }
        }
}
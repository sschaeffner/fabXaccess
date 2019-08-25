package cloud.fabx

import cloud.fabx.db.DbHandler
import cloud.fabx.model.Admins
import cloud.fabx.model.Devices
import cloud.fabx.model.Tools
import cloud.fabx.model.Users
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before

@KtorExperimentalAPI
open class CommonTest {
    @Before
    fun before() = transaction(DbHandler.db) {
        println("BeforeEach")

        SchemaUtils.drop(Devices)
        SchemaUtils.drop(Tools)
        SchemaUtils.drop(Users)
        SchemaUtils.drop(Admins)

        SchemaUtils.create(Devices)
        SchemaUtils.create(Tools)
        SchemaUtils.create(Users)
        SchemaUtils.create(Admins)

        Unit
    }
}
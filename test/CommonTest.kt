package cloud.fabx

import cloud.fabx.db.DbHandler
import cloud.fabx.model.Devices
import cloud.fabx.model.Tools
import cloud.fabx.model.Users
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before

open class CommonTest {
    @Before
    fun before() = transaction(DbHandler.db) {
        println("BeforeEach")

        SchemaUtils.drop(Devices)
        SchemaUtils.drop(Tools)
        SchemaUtils.drop(Users)

        SchemaUtils.create(Devices)
        SchemaUtils.create(Tools)
        SchemaUtils.create(Users)

        Unit
    }
}
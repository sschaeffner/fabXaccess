package cloud.fabx.model

import org.jetbrains.exposed.sql.Table

object UserPermissions: Table() {
    val user = reference("user", Users).primaryKey(0)
    val tool = reference("tool", Tools).primaryKey(1)
}
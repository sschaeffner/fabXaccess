package cloud.fabx.model

import org.jetbrains.exposed.sql.Table

object UserQualifications: Table() {
    val user = reference("user", Users).primaryKey(0)
    val qualification = reference("qualification", Qualifications).primaryKey(1)
}
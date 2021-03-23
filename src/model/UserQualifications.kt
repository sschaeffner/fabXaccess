package cloud.fabx.model

import org.jetbrains.exposed.sql.Table

object UserQualifications : Table() {
    val user = reference("user", Users)
    val qualification = reference("qualification", Qualifications)
    override val primaryKey = PrimaryKey(user, qualification)
}
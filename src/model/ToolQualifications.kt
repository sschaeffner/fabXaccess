package cloud.fabx.model

import org.jetbrains.exposed.sql.Table

object ToolQualifications: Table() {
    val tool = reference("tool", Tools).primaryKey(0)
    val qualification = reference("qualification", Qualifications).primaryKey(1)
}
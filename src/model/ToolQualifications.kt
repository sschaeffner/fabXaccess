package cloud.fabx.model

import org.jetbrains.exposed.sql.Table

object ToolQualifications: Table() {
    val tool = reference("tool", Tools)
    val qualification = reference("qualification", Qualifications)
    override val primaryKey = PrimaryKey(tool, qualification)
}
package net.nemerosa.ontrack.repository

import net.nemerosa.ontrack.repository.support.AbstractJdbcRepository
import org.springframework.stereotype.Repository
import javax.sql.DataSource

@Repository
class ProjectLabelJdbcRepository(
        dataSource: DataSource
) : AbstractJdbcRepository(dataSource), ProjectLabelRepository {

    override fun getLabelsForProject(project: Int): List<Int> =
            namedParameterJdbcTemplate.queryForList(
                    """
                        SELECT PL.LABEL_ID
                        FROM PROJECT_LABEL PL
                        INNER JOIN LABEL L ON L.ID = PL.label_id
                        WHERE PROJECT_ID = :project
                        ORDER BY L.category, L.name
                       """,
                    params("project", project),
                    Integer::class.java
            ).map { it.toInt() }

    override fun getProjectsForLabel(label: Int): List<Int> =
            namedParameterJdbcTemplate.queryForList(
                    """
                        SELECT PL.project_id
                        FROM PROJECT_LABEL PL
                        INNER JOIN PROJECTS p on PL.project_id = p.id
                        WHERE label_id = :label
                        ORDER BY p.name
                       """,
                    params("label", label),
                    Integer::class.java
            ).map { it.toInt() }

    override fun associateProjectToLabel(project: Int, label: Int) {
        val params = params("project", project).addValue("label", label)
        val existing = namedParameterJdbcTemplate.queryForList(
                """
                    SELECT *
                    FROM PROJECT_LABEL
                    WHERE PROJECT_ID = :project
                    AND LABEL_ID = :label
                """,
                params
        )
        if (existing.isEmpty()) {
            namedParameterJdbcTemplate.update(
                    """
                        INSERT INTO project_label(PROJECT_ID, LABEL_ID)
                        VALUES (:project, :label)
                    """,
                    params
            )
        }
    }

    override fun unassociateProjectToLabel(project: Int, label: Int) {
        namedParameterJdbcTemplate.update(
                """
                    DELETE FROM PROJECT_LABEL
                    WHERE PROJECT_ID = :project
                    AND LABEL_ID = :label
                """,
                params("project", project).addValue("label", label)
        )
    }

}

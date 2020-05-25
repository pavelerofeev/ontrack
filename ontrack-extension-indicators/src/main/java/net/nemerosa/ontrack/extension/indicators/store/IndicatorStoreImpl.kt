package net.nemerosa.ontrack.extension.indicators.store

import com.fasterxml.jackson.databind.JsonNode
import net.nemerosa.ontrack.common.Time
import net.nemerosa.ontrack.extension.indicators.model.Indicator
import net.nemerosa.ontrack.json.asJson
import net.nemerosa.ontrack.json.parse
import net.nemerosa.ontrack.model.Ack
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.repository.support.store.EntityDataStore
import net.nemerosa.ontrack.repository.support.store.EntityDataStoreFilter
import net.nemerosa.ontrack.repository.support.store.EntityDataStoreRecord
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class IndicatorStoreImpl(
        private val entityDataStore: EntityDataStore,
        private val securityService: SecurityService
) : IndicatorStore {

    override fun loadIndicator(project: Project, type: String, previous: Duration?): StoredIndicator? {
        return entityDataStore.getByFilter(
                EntityDataStoreFilter(project)
                        .withCategory(STORE_CATEGORY)
                        .withName(type)
                        .withCount(1)
                        .withBeforeTime(previous?.let { Time.now() - it })
        )
                .firstOrNull()
                ?.let { record ->
                    toStoredIndicator(record)
                }
    }

    override fun loadIndicatorHistory(project: Project, type: String): List<StoredIndicator> {
        return entityDataStore.getByFilter(
                EntityDataStoreFilter(project)
                        .withCategory(STORE_CATEGORY)
                        .withName(type)
        ).mapNotNull { record -> toStoredIndicator(record) }
    }

    private fun toStoredIndicator(record: EntityDataStoreRecord): StoredIndicator? {
        val rep = record.data.parse<StoredIndicatorRepresentation>()
        return if (rep.value != null) {
            StoredIndicator(
                    value = rep.value,
                    comment = rep.comment,
                    signature = record.signature
            )
        } else {
            null
        }
    }

    override fun loadPreviousIndicator(project: Project, typeId: String): StoredIndicator? =
            entityDataStore.getByFilter(
                    EntityDataStoreFilter(project)
                            .withCategory(STORE_CATEGORY)
                            .withName(typeId)
                            .withCount(2)
            )
                    .drop(1)
                    .firstOrNull()
                    ?.let {
                        toStoredIndicator(it)
                    }

    override fun storeIndicator(project: Project, type: String, indicator: StoredIndicator) {
        // Gets the last version of the indicator
        val last = loadIndicator(project, type)
        if (last == null || last.value != indicator.value || last.comment != indicator.comment) {
            entityDataStore.add(
                    project,
                    STORE_CATEGORY,
                    type,
                    indicator.signature,
                    null,
                    StoredIndicatorRepresentation(
                            value = indicator.value,
                            comment = indicator.comment
                    ).asJson()
            )
        }
    }

    override fun deleteIndicator(project: Project, typeId: String): Ack {
        val existing = loadIndicator(project, typeId)
        if (existing != null) {
            entityDataStore.add(
                    project,
                    STORE_CATEGORY,
                    typeId,
                    securityService.currentSignature,
                    null,
                    StoredIndicatorRepresentation(
                            value = null,
                            comment = null
                    ).asJson()
            )
        }
        return Ack.validate(existing != null)
    }

    override fun deleteIndicatorByType(typeId: String) {
        entityDataStore.deleteByFilter(
                EntityDataStoreFilter(null)
                        .withCategory(STORE_CATEGORY)
                        .withName(typeId)
                        .withCount(Int.MAX_VALUE)
        )
    }

    companion object {
        private val STORE_CATEGORY = Indicator::class.java.name
    }

    private class StoredIndicatorRepresentation(
            val value: JsonNode?,
            val comment: String?
    )

}
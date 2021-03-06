package net.nemerosa.ontrack.extension.general

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import net.nemerosa.ontrack.extension.api.SearchExtension
import net.nemerosa.ontrack.extension.support.AbstractExtension
import net.nemerosa.ontrack.json.parseOrNull
import net.nemerosa.ontrack.model.security.ProjectView
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.security.callAsAdmin
import net.nemerosa.ontrack.model.structure.*
import net.nemerosa.ontrack.ui.controller.URIBuilder
import net.nemerosa.ontrack.ui.support.AbstractSearchProvider
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.util.function.BiFunction
import java.util.function.Predicate

@Component
class MetaInfoSearchExtension(
        extensionFeature: GeneralExtensionFeature,
        private val uriBuilder: URIBuilder,
        private val propertyService: PropertyService,
        private val structureService: StructureService,
        private val securityService: SecurityService
) : AbstractExtension(extensionFeature), SearchExtension, SearchIndexer<MetaInfoSearchItem> {

    override val searchResultType = SearchResultType(
            extensionFeature.featureDescription,
            "build-meta-info",
            "Build with Meta Info",
            "Meta information pair using format name:[value] or value"
    )

    override fun getSearchProvider(): SearchProvider {
        return object : AbstractSearchProvider(uriBuilder) {
            override fun isTokenSearchable(token: String): Boolean {
                return this@MetaInfoSearchExtension.isTokenSearchable(token)
            }

            override fun search(token: String): Collection<SearchResult> {
                return this@MetaInfoSearchExtension.search(token)
            }
        }
    }

    fun isTokenSearchable(token: String): Boolean = StringUtils.indexOf(token, META_INFO_SEPARATOR) > 0

    protected fun search(token: String): Collection<SearchResult> {
        return if (isTokenSearchable(token)) {
            val name = StringUtils.substringBefore(token, META_INFO_SEPARATOR)
            val value = StringUtils.substringAfter(token, META_INFO_SEPARATOR)
            // Searchs for all entities with the value
            val entities = securityService.callAsAdmin {
                propertyService.searchWithPropertyValue(
                        MetaInfoPropertyType::class.java,
                        BiFunction { entityType, id -> entityType.getEntityFn(structureService).apply(id) },
                        Predicate { metaInfoProperty -> metaInfoProperty.matchNameValue(name, value) }
                )
            }.filter { entity ->
                securityService.isProjectFunctionGranted(entity, ProjectView::class.java)
            }
            // Returns search results
            entities.mapNotNull { entity -> toSearchResult(entity, name) }
        } else {
            emptyList()
        }
    }

    protected fun toSearchResult(entity: ProjectEntity, name: String): SearchResult? {
        // Gets the property value for the meta info name (required)
        val value = propertyService.getProperty(entity, MetaInfoPropertyType::class.java).value?.getValue(name)
        // OK
        return value?.run {
            SearchResult(
                    entity.entityDisplayName,
                    String.format("%s -> %s", name, this),
                    uriBuilder.getEntityURI(entity),
                    uriBuilder.getEntityPage(entity),
                    100.0,
                    searchResultType
            )
        }
    }

    override val indexerName: String = "Meta info properties"

    override val indexName: String = META_INFO_SEARCH_INDEX

    override val indexMapping: SearchIndexMapping? = indexMappings<MetaInfoSearchItem> {
        +MetaInfoSearchItem::entityId to id { index = false }
        +MetaInfoSearchItem::entityType to keyword { index = false }
        +MetaInfoSearchItem::items to nested()
        +MetaInfoSearchItem::keys to keyword { scoreBoost = 3.0 } to text()
    }

    override fun indexAll(processor: (MetaInfoSearchItem) -> Unit) {
        propertyService.forEachEntityWithProperty<MetaInfoPropertyType, MetaInfoProperty> { entityId, property ->
            processor(
                    MetaInfoSearchItem(
                            entityId = entityId,
                            property = property
                    )
            )
        }
    }

    override fun toSearchResult(id: String, score: Double, source: JsonNode): SearchResult? {
        // Parsing
        val item = source.parseOrNull<MetaInfoSearchItem>()
        // Conversion
        return item?.let { toSearchResult(it, score) }
    }

    private fun toSearchResult(item: MetaInfoSearchItem, score: Double): SearchResult? {
        // Loads the entity
        val entity: ProjectEntity? = item.entityType.getFindEntityFn(structureService).apply(ID.of(item.entityId))
        // Conversion (using legacy code)
        return entity?.let {
            SearchResult(
                    title = entity.entityDisplayName,
                    description = item.items.map { (name, value) -> "$name -> $value" }.sorted().joinToString(", "),
                    uri = uriBuilder.getEntityURI(entity),
                    page = uriBuilder.getEntityPage(entity),
                    accuracy = score,
                    type = searchResultType
            )
        }
    }

}

/**
 * Separator between name and value when looking for meta information.
 */
const val META_INFO_SEPARATOR = ":"

/**
 * Index name for the meta info search
 */
const val META_INFO_SEARCH_INDEX = "meta-info-properties"

@JsonIgnoreProperties(ignoreUnknown = true)
class MetaInfoSearchItem(
        val items: Map<String, String>,
        val entityType: ProjectEntityType,
        val entityId: Int
) : SearchItem {

    constructor(entity: ProjectEntity, property: MetaInfoProperty) : this(
            entityId = ProjectEntityID(entity.projectEntityType, entity.id()),
            property = property
    )

    constructor(entityId: ProjectEntityID, property: MetaInfoProperty) : this(
            items = property.items.map {
                it.name to (it.value ?: "")
            }.associate { it },
            entityType = entityId.type,
            entityId = entityId.id
    )

    val keys = items.map { (name, value) -> "$name$META_INFO_SEPARATOR$value" }

    override val id: String = "$entityType::$entityId"

    override val fields: Map<String, Any?> = mapOf(
            "keys" to items.map { (name, value) -> "$name$META_INFO_SEPARATOR$value" },
            "items" to items,
            "entityType" to entityType,
            "entityId" to entityId
    )

}
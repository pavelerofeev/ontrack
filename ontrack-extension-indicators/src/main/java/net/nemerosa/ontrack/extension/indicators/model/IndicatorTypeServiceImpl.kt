package net.nemerosa.ontrack.extension.indicators.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import net.nemerosa.ontrack.common.getOrNull
import net.nemerosa.ontrack.extension.indicators.acl.IndicatorTypeManagement
import net.nemerosa.ontrack.model.Ack
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.support.StorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IndicatorTypeServiceImpl(
        private val indicatorCategoryService: IndicatorCategoryService,
        private val indicatorValueTypeService: IndicatorValueTypeService,
        private val storageService: StorageService,
        private val securityService: SecurityService
) : IndicatorTypeService, IndicatorCategoryListener {

    init {
        indicatorCategoryService.registerCategoryListener(this)
    }

    override fun onCategoryDeleted(category: IndicatorCategory) {
        findByCategory(category).forEach {
            deleteType(it.id)
        }
    }

    private val listeners = mutableListOf<IndicatorTypeListener>()

    override fun registerTypeListener(listener: IndicatorTypeListener) {
        listeners += listener
    }

    override fun findAll(): List<IndicatorType<*, *>> {
        return storageService.getKeys(STORE).mapNotNull { key ->
            storageService.retrieve(STORE, key, StoredIndicatorType::class.java).getOrNull()
        }.mapNotNull {
            fromStorage<Any, Any>(it)
        }.sortedWith(
                compareBy(
                        { it.category.name },
                        { it.name }
                )
        )
    }

    override fun findTypeById(typeId: String): IndicatorType<*, *>? =
            storageService.retrieve(STORE, typeId, StoredIndicatorType::class.java)
                    .getOrNull()
                    ?.let { fromStorage<Any, Any>(it) }

    override fun getTypeById(typeId: String): IndicatorType<*, *> =
            findTypeById(typeId) ?: throw IndicatorTypeNotFoundException(typeId)

    override fun findByCategory(category: IndicatorCategory): List<IndicatorType<*, *>> {
        return findAll().filter {
            it.category.id == category.id
        }.sortedWith(
                compareBy(
                        { it.category.name },
                        { it.name }
                )
        )
    }

    private fun <T, C> fromStorage(stored: StoredIndicatorType): IndicatorType<T, C>? {
        val category = indicatorCategoryService.findCategoryById(stored.category)
        val valueType = indicatorValueTypeService.findValueTypeById<T, C>(stored.valueType)
        return if (category != null && valueType != null) {
            val valueConfig = valueType.fromConfigStoredJson(stored.valueConfig)
            IndicatorType(
                    id = stored.id,
                    category = category,
                    name = stored.name,
                    link = stored.link,
                    valueType = valueType,
                    valueConfig = valueConfig,
                    source = stored.source,
                    computed = stored.computed
            )
        } else {
            null
        }
    }

    override fun createType(input: CreateTypeForm): IndicatorType<*, *> {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val type = findTypeById(input.id)
        if (type != null) {
            throw IndicatorTypeIdAlreadyExistsException(input.id)
        } else {
            return updateType(input)
        }
    }

    override fun <T, C> createType(
            id: String,
            category: IndicatorCategory,
            name: String,
            link: String?,
            valueType: IndicatorValueType<T, C>,
            valueConfig: C,
            source: IndicatorSource?,
            computed: Boolean
    ): IndicatorType<T, C> {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val type = findTypeById(id)
        if (type != null) {
            throw IndicatorTypeIdAlreadyExistsException(id)
        } else {
            return updateType(
                    id,
                    category,
                    name,
                    link,
                    valueType,
                    valueConfig,
                    source,
                    computed
            )
        }
    }

    override fun deleteType(id: String): Ack {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val type = findTypeById(id)
        return if (type != null) {
            listeners.forEach { it.onTypeDeleted(type) }
            storageService.delete(STORE, id)
            Ack.OK
        } else {
            Ack.NOK
        }
    }

    override fun updateType(input: CreateTypeForm): IndicatorType<*, *> {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val category = indicatorCategoryService.getCategory(input.category)
        val valueType = indicatorValueTypeService.getValueType<Any, Any>(input.valueType.id)
        val valueConfig = valueType.toConfigStoredJson(
                valueType.fromConfigForm(input.valueType.data ?: NullNode.instance)
        )
        val stored = StoredIndicatorType(
                id = input.id,
                category = category.id,
                name = input.name,
                link = input.link,
                valueType = valueType.id,
                valueConfig = valueConfig,
                source = null,
                computed = false
        )
        storageService.store(
                STORE,
                input.id,
                stored
        )
        return getTypeById(input.id)
    }

    override fun <T, C> updateType(
            id: String,
            category: IndicatorCategory,
            name: String,
            link: String?,
            valueType: IndicatorValueType<T, C>,
            valueConfig: C,
            source: IndicatorSource?,
            computed: Boolean
    ): IndicatorType<T, C> {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val stored = StoredIndicatorType(
                id = id,
                category = category.id,
                name = name,
                link = link,
                valueType = valueType.id,
                valueConfig = valueType.toConfigStoredJson(valueConfig),
                source = source,
                computed = computed
        )
        storageService.store(
                STORE,
                id,
                stored
        )
        @Suppress("UNCHECKED_CAST")
        return getTypeById(id) as IndicatorType<T, C>
    }

    private class StoredIndicatorType(
            val id: String,
            val category: String,
            val name: String,
            val link: String?,
            val valueType: String,
            val valueConfig: JsonNode,
            val source: IndicatorSource?,
            val computed: Boolean
    )

    companion object {
        private val STORE: String get() = IndicatorType::class.java.name
    }

}
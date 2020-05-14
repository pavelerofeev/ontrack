package net.nemerosa.ontrack.extension.indicators.model

import net.nemerosa.ontrack.common.getOrNull
import net.nemerosa.ontrack.extension.indicators.acl.IndicatorTypeManagement
import net.nemerosa.ontrack.model.Ack
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.support.StorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IndicatorCategoryServiceImpl(
        private val securityService: SecurityService,
        private val storageService: StorageService
) : IndicatorCategoryService {

    private val listeners = mutableListOf<IndicatorCategoryListener>()

    override fun registerCategoryListener(listener: IndicatorCategoryListener) {
        listeners += listener
    }

    override fun createCategory(input: IndicatorForm, source: IndicatorSource?): IndicatorCategory {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val type = findCategoryById(input.id)
        if (type != null) {
            throw IndicatorCategoryIdAlreadyExistsException(input.id)
        } else {
            return updateCategory(input, source)
        }
    }

    override fun updateCategory(input: IndicatorForm, source: IndicatorSource?): IndicatorCategory {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val stored = StoredIndicatorCategory(
                id = input.id,
                name = input.name,
                source = source
        )
        storageService.store(
                STORE,
                input.id,
                stored
        )
        return getCategory(input.id)
    }

    override fun deleteCategory(id: String): Ack {
        securityService.checkGlobalFunction(IndicatorTypeManagement::class.java)
        val category = findCategoryById(id)
        return if (category != null) {
            listeners.forEach { it.onCategoryDeleted(category) }
            storageService.delete(STORE, id)
            Ack.OK
        } else {
            Ack.NOK
        }
    }

    override fun findCategoryById(id: String): IndicatorCategory? =
            storageService.retrieve(STORE, id, StoredIndicatorCategory::class.java)
                    .getOrNull()
                    ?.let { fromStorage(it) }

    override fun getCategory(id: String): IndicatorCategory {
        return findCategoryById(id) ?: throw IndicatorCategoryNotFoundException(id)
    }

    override fun findAll(): List<IndicatorCategory> {
        return storageService.getKeys(STORE).mapNotNull { key ->
            storageService.retrieve(STORE, key, StoredIndicatorCategory::class.java).getOrNull()
        }.mapNotNull {
            fromStorage(it)
        }.sortedBy { it.name }
    }

    private fun fromStorage(stored: StoredIndicatorCategory): IndicatorCategory? =
            IndicatorCategory(
                    id = stored.id,
                    name = stored.name,
                    source = stored.source
            )

    private class StoredIndicatorCategory(
            val id: String,
            val name: String,
            val source: IndicatorSource?
    )

    companion object {
        private val STORE: String = IndicatorCategory::class.java.name
    }

}
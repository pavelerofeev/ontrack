package net.nemerosa.ontrack.extension.oidc.settings

import net.nemerosa.ontrack.model.security.EncryptionService
import net.nemerosa.ontrack.model.security.GlobalSettings
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.support.StorageService
import net.nemerosa.ontrack.model.support.retrieve
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OIDCSettingsServiceImpl(
        private val securityService: SecurityService,
        private val storageService: StorageService,
        private val encryptionService: EncryptionService
) : OIDCSettingsService {

    override val providers: List<OntrackOIDCProvider>
        get() {
            securityService.checkGlobalFunction(GlobalSettings::class.java)
            return storageService.getData(OIDC_PROVIDERS_STORE, StoredOntrackOIDCProvider::class.java)
                    .values
                    .sortedBy { it.id }
                    .map { decrypt(it) }
        }

    override fun createProvider(input: OntrackOIDCProvider): OntrackOIDCProvider {
        securityService.checkGlobalFunction(GlobalSettings::class.java)
        val existing = storageService.retrieve<OntrackOIDCProvider>(OIDC_PROVIDERS_STORE, input.id)
        if (existing != null) {
            throw OntrackOIDCProviderIDAlreadyExistsException(input.id)
        } else {
            storageService.store(OIDC_PROVIDERS_STORE, input.id, encrypt(input))
            return input
        }
    }

    companion object {
        /**
         * Name of the store
         */
        private val OIDC_PROVIDERS_STORE = OntrackOIDCProvider::class.java.name
    }

    /**
     * Stored object
     */
    private data class StoredOntrackOIDCProvider(
            val id: String,
            val name: String,
            val description: String,
            val issuerId: String,
            val clientId: String,
            val clientEncryptedSecret: String
    )

    private fun decrypt(stored: StoredOntrackOIDCProvider) = OntrackOIDCProvider(
            id = stored.id,
            name = stored.name,
            description = stored.description,
            issuerId = stored.issuerId,
            clientId = stored.clientId,
            clientSecret = encryptionService.decrypt(stored.clientEncryptedSecret) ?: ""
    )

    private fun encrypt(input: OntrackOIDCProvider) = StoredOntrackOIDCProvider(
            id = input.id,
            name = input.name,
            description = input.description,
            issuerId = input.issuerId,
            clientId = input.clientId,
            clientEncryptedSecret = (encryptionService.encrypt(input.clientSecret)
                    ?: throw OntrackOIDCProviderCannotEncryptSecretException())
    )
}
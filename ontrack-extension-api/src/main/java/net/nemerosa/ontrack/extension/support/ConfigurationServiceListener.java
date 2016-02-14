package net.nemerosa.ontrack.extension.support;

import net.nemerosa.ontrack.model.support.UserPasswordConfiguration;

public interface ConfigurationServiceListener<T extends UserPasswordConfiguration> {

    default void onNewConfiguration(T configuration) {
    }

    default void onUpdatedConfiguration(T configuration) {
    }

    default void onDeletedConfiguration(T configuration) {
    }
}

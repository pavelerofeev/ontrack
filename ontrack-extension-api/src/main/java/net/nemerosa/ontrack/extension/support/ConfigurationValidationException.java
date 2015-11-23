package net.nemerosa.ontrack.extension.support;

import net.nemerosa.ontrack.model.exceptions.InputException;
import net.nemerosa.ontrack.model.support.Configuration;
import net.nemerosa.ontrack.model.support.UserPasswordConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * Thrown when a validation error occurs when creating or updating a configuration.
 *
 * @see AbstractConfigurationService#validateAndCheck(UserPasswordConfiguration)
 */
public class ConfigurationValidationException extends InputException {
    public ConfigurationValidationException(Configuration<?> configuration, String message) {
        super(extractMessage(configuration, message));
    }

    private static String extractMessage(Configuration<?> configuration, String message) {
        if (StringUtils.isNotBlank(message)) {
            return message;
        } else {
            return String.format("Cannot connect to %s", configuration.getName());
        }
    }
}

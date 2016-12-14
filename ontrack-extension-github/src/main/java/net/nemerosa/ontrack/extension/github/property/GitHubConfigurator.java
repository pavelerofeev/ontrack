package net.nemerosa.ontrack.extension.github.property;

import net.nemerosa.ontrack.extension.git.model.GitConfiguration;
import net.nemerosa.ontrack.extension.git.model.GitConfigurator;
import net.nemerosa.ontrack.extension.github.GitHubIssueServiceExtension;
import net.nemerosa.ontrack.extension.issues.IssueServiceRegistry;
import net.nemerosa.ontrack.extension.issues.model.ConfiguredIssueService;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationIdentifier;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.nemerosa.ontrack.extension.github.property.GitHubGitConfiguration.CONFIGURATION_REPOSITORY_SEPARATOR;

@Component
public class GitHubConfigurator implements GitConfigurator {

    private final PropertyService propertyService;
    private final IssueServiceRegistry issueServiceRegistry;

    @Autowired
    public GitHubConfigurator(
            PropertyService propertyService,
            IssueServiceRegistry issueServiceRegistry
    ) {
        this.propertyService = propertyService;
        this.issueServiceRegistry = issueServiceRegistry;
    }

    @Override
    public Optional<GitConfiguration> getConfiguration(Project project) {
        return propertyService.getProperty(project, GitHubProjectConfigurationPropertyType.class)
                .option()
                .map(this::getGitConfiguration);
    }

    private GitConfiguration getGitConfiguration(GitHubProjectConfigurationProperty property) {
        return new GitHubGitConfiguration(
                property,
                getConfiguredIssueService(property)
        );
    }

    private ConfiguredIssueService getConfiguredIssueService(GitHubProjectConfigurationProperty property) {
        // TODO #473 String identifier = property.getIssueServiceConfigurationIdentifier();
        // TODO #473 if (IssueServiceConfigurationRepresentation.isSelf(identifier)) {
//            return new ConfiguredIssueService(
//                    issueServiceExtension,
//                    new GitLabIssueServiceConfiguration(
//                            property.getConfiguration(),
//                            property.getRepository()
//                    )
//            );
//        } else {
        return issueServiceRegistry.getConfiguredIssueService(
                new IssueServiceConfigurationIdentifier(
                        GitHubIssueServiceExtension.GITHUB_SERVICE_ID,
                        property.getConfiguration().getName()
                                + CONFIGURATION_REPOSITORY_SEPARATOR
                                + property.getRepository()
                ).format()
        );
//        }
    }
}

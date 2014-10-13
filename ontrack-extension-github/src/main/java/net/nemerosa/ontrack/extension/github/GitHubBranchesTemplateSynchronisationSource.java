package net.nemerosa.ontrack.extension.github;

import net.nemerosa.ontrack.extension.api.ExtensionManager;
import net.nemerosa.ontrack.extension.git.GitBranchesTemplateSynchronisationSourceConfig;
import net.nemerosa.ontrack.extension.github.property.GitHubProjectConfigurationPropertyType;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.Memo;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.PropertyService;
import net.nemerosa.ontrack.model.support.AbstractTemplateSynchronisationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class GitHubBranchesTemplateSynchronisationSource extends AbstractTemplateSynchronisationSource<GitBranchesTemplateSynchronisationSourceConfig> {

    private final GitHubExtensionFeature gitHubExtensionFeature;
    private final ExtensionManager extensionManager;
    private final PropertyService propertyService;

    @Autowired
    public GitHubBranchesTemplateSynchronisationSource(GitHubExtensionFeature gitHubExtensionFeature, ExtensionManager extensionManager, PropertyService propertyService) {
        super(GitBranchesTemplateSynchronisationSourceConfig.class);
        this.gitHubExtensionFeature = gitHubExtensionFeature;
        this.extensionManager = extensionManager;
        this.propertyService = propertyService;
    }

    @Override
    public String getId() {
        return "github-branches";
    }

    @Override
    public String getName() {
        return "GitHub branches";
    }

    @Override
    public boolean isApplicable(Project project) {
        return extensionManager.isExtensionFeatureEnabled(gitHubExtensionFeature)
                && propertyService.hasProperty(project, GitHubProjectConfigurationPropertyType.class);
    }

    @Override
    public Form getForm(Project project) {
        return Form.create()
                .with(
                        Memo.of("includes")
                                .label("Includes")
                                .optional()
                                .help("List of branches to include - one pattern per line, where " +
                                        "* can be used as a wildcard.")
                )
                .with(
                        Memo.of("excludes")
                                .label("Excludes")
                                .optional()
                                .help("List of branches to exclude - one pattern per line, where " +
                                        "* can be used as a wildcard.")
                )
                ;
    }

    @Override
    public List<String> getBranchNames(Project project, GitBranchesTemplateSynchronisationSourceConfig config) {
        // FIXME Method net.nemerosa.ontrack.extension.git.GitBranchesTemplateSynchronisationSource.getBranchNames
        return Collections.emptyList();
    }

    @Override
    public GitBranchesTemplateSynchronisationSourceConfig getDefaultConfig(Project project) {
        return new GitBranchesTemplateSynchronisationSourceConfig(
                "",
                ""
        );
    }
}

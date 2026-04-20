package gitflow;

import consulo.annotation.component.ExtensionImpl;
import consulo.component.PropertiesComponent;
import consulo.configurable.ConfigurationException;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.project.ProjectPropertiesComponent;
import consulo.ui.annotation.RequiredUIAccess;
import gitflow.ui.GitflowOptionsForm;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andreas Vogler (Andreas.Vogler@geneon.de)
 * @author Opher Vishnia (opherv@gmail.com)
 */
@ExtensionImpl
public class GitflowConfigurable implements ProjectConfigurable {
    final Project project;
    GitflowOptionsForm gitflowOptionsForm;
    final PropertiesComponent propertiesComponent;
    final Map<Enum<GitflowOptionsFactory.TYPE>, List<Map<String, String>>> gitflowOptions;
    final Map<String, String> optionDefaults;

    @Inject
    public GitflowConfigurable(Project project, ProjectPropertiesComponent projectPropertiesComponent) {
        gitflowOptions = GitflowOptionsFactory.getOptions();
        propertiesComponent = projectPropertiesComponent;
        optionDefaults = new HashMap<>();
        this.project = project;
    }

    @Nonnull
    @Override
    public String getId() {
        return "vcs.gitflow";
    }

    @Nullable
    @Override
    public String getParentId() {
        return StandardConfigurableIds.VCS_GROUP;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Gitflow");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    @RequiredUIAccess
    public JComponent createComponent() {
        gitflowOptionsForm = new GitflowOptionsForm();
        return gitflowOptionsForm.getContentPane();
    }

    public static boolean isOptionActive(Project project, String optionId) {
        return ProjectPropertiesComponent.getInstance(project).getBoolean(optionId + "_active");
    }

    public static String getOptionTextString(Project project, String optionId) {
        String retValue = ProjectPropertiesComponent.getInstance(project).getValue(optionId + "_text");
        if (retValue == null) {
            retValue = DefaultOptions.getOption(optionId);
        }
        return retValue;
    }

    @Override
    @RequiredUIAccess
    public boolean isModified() {
        // iterate over branch types (feature/release/hotfix/bugfix)
        for (GitflowOptionsFactory.TYPE type : GitflowOptionsFactory.TYPE.values()) {
            for (Map<String, String> optionMap : gitflowOptions.get(type)) {
                String optionId = GitflowOptionsFactory.getOptionId(type, optionMap.get("key"));

                boolean isOptionActiveInForm = gitflowOptionsForm.isOptionActive(optionId);
                boolean savedOptionIsActive = propertiesComponent.getBoolean(optionId + "_active");

                if (isOptionActiveInForm != savedOptionIsActive) {
                    return true;
                }

                // option has text value
                if (optionMap.get("inputText") != null) {
                    String textInForm = gitflowOptionsForm.getOptionText(optionId);
                    String savedOptionText = propertiesComponent.getValue(optionId + "text");
                    if (!textInForm.equals(savedOptionText)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @RequiredUIAccess
    public void apply() throws ConfigurationException {
        // iterate over branch types (feature/release/hotfix/bugfix)
        for (GitflowOptionsFactory.TYPE type : GitflowOptionsFactory.TYPE.values()) {
            for (Map<String, String> optionMap : gitflowOptions.get(type)) {
                String optionId = GitflowOptionsFactory.getOptionId(type, optionMap.get("key"));

                // set isActive value
                propertiesComponent.setValue(optionId + "_active", gitflowOptionsForm.isOptionActive(optionId));

                // set text value, if relevant
                if (optionMap.get("inputText") != null) {
                    propertiesComponent.setValue(optionId + "_text", gitflowOptionsForm.getOptionText(optionId));
                }
            }
        }
    }

    @Override
    @RequiredUIAccess
    public void reset() {
        // iterate over branch types (feature/release/hotfix/bugfix)
        for (GitflowOptionsFactory.TYPE type : GitflowOptionsFactory.TYPE.values()) {
            for (Map<String, String> optionMap : gitflowOptions.get(type)) {
                String optionId = GitflowOptionsFactory.getOptionId(type, optionMap.get("key"));
                boolean savedOptionIsActive = propertiesComponent.getBoolean(optionId + "_active");
                gitflowOptionsForm.setOptionActive(optionId, savedOptionIsActive);

                // option has text value
                if (optionMap.get("inputText") != null) {
                    String savedOptionText = propertiesComponent.getValue(optionId + "text");
                    if (savedOptionText == null) {
                        gitflowOptionsForm.setOptionText(optionId, optionMap.get("inputText"));
                    }
                    else {
                        gitflowOptionsForm.setOptionText(optionId, savedOptionText);
                    }
                }
            }
        }
        gitflowOptionsForm.updateFormDisabledStatus();
    }

    @Override
    @RequiredUIAccess
    public void disposeUIResources() {
        gitflowOptionsForm = null;
    }
}

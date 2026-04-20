package gitflow;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.project.Project;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandlerListener;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */
@ServiceAPI(ComponentScope.APPLICATION)
public interface Gitflow {
    GitCommandResult initRepo(
        @Nonnull GitRepository repository,
        GitflowInitOptions initOptions,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult reInitRepo(
        @Nonnull GitRepository repository,
        GitflowInitOptions initOptions,
        @Nullable GitLineHandlerListener... listeners
    );

    // feature

    GitCommandResult startFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nullable String baseBranch,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult finishFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult publishFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult pullFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult trackFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    );

    //release

    GitCommandResult startRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult finishRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nonnull String tagMessage,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult publishRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult trackRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nullable GitLineHandlerListener... listeners
    );

    //hotfix

    GitCommandResult startHotfix(
        @Nonnull GitRepository repository,
        @Nonnull String hotfixName,
        @Nullable String baseBranch,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult finishHotfix(
        @Nonnull GitRepository repository,
        @Nonnull String hotfixName,
        @Nonnull String tagMessage,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult publishHotfix(
        @Nonnull GitRepository repository,
        @Nonnull String hotfixName,
        @Nullable GitLineHandlerListener... listeners
    );

    // Bugfix

    GitCommandResult startBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nullable String baseBranch,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult finishBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult publishBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult pullBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult trackBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    );

    GitCommandResult version(@Nonnull Project project, GitLineHandlerListener... listeners);
}

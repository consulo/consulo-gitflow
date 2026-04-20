package gitflow;

import consulo.annotation.component.ServiceImpl;
import consulo.project.Project;
import git4idea.commands.*;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */
@ServiceImpl
@Singleton
public class GitflowImpl implements Gitflow {
    private final Git myGit;

    @Inject
    public GitflowImpl(Git git) {
        myGit = git;
    }

    private GitCommand flowCommand() {
        return GitCommand.write("flow");
    }

    private void addOptionsCommand(GitLineHandler h, Project project, String optionId) {
        Map<String, String> optionMap = GitflowOptionsFactory.getOptionById(optionId);
        if (GitflowConfigurable.isOptionActive(project, optionMap.get("id"))) {
            h.addParameters(optionMap.get("flag"));
        }
    }

    @Override
    public GitCommandResult initRepo(
        @Nonnull GitRepository repository,
        GitflowInitOptions initOptions,
        @Nullable GitLineHandlerListener... listeners
    ) {
        return callInit(repository, initOptions, false, listeners);
    }

    @Override
    public GitCommandResult reInitRepo(
        @Nonnull GitRepository repository,
        GitflowInitOptions initOptions,
        @Nullable GitLineHandlerListener... listeners
    ) {
        return callInit(repository, initOptions, true, listeners);
    }

    private GitCommandResult callInit(
        @Nonnull GitRepository repository,
        GitflowInitOptions initOptions, boolean reInit,
        @Nullable GitLineHandlerListener[] listeners
    ) {
        GitLineHandler h;
        if (initOptions.isUseDefaults()) {
            h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        }
        else {
            h = new GitInitLineHandler(initOptions, repository.getProject(), repository.getRoot(), flowCommand());
        }

        h.setSilent(false);
        h.setStdoutSuppressed(false);
        h.setStderrSuppressed(false);

        h.addParameters("init");

        if (initOptions.isUseDefaults()) {
            h.addParameters("-d");
        }
        else {
            for (GitLineHandlerListener listener : listeners) {
                h.addLineListener(listener);
            }
        }

        if (reInit) {
            h.addParameters("-f");
        }

        return myGit.runCommand(h);
    }


    //feature

    @Override
    public GitCommandResult startFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nullable String baseBranch,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("feature");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(), "FEATURE_fetchFromOrigin");

        h.addParameters(featureName);

        if (baseBranch != null) {
            h.addParameters(baseBranch);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult finishFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());

        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("feature");
        h.addParameters("finish");


        addOptionsCommand(h, repository.getProject(), "FEATURE_keepRemote");
        addOptionsCommand(h, repository.getProject(), "FEATURE_keepLocal");
        addOptionsCommand(h, repository.getProject(), "FEATURE_keepBranch");
        addOptionsCommand(h, repository.getProject(), "FEATURE_fetchFromOrigin");
        addOptionsCommand(h, repository.getProject(), "FEATURE_pushOnFinish");
        addOptionsCommand(h, repository.getProject(), "FEATURE_noFastForward");
//        addOptionsCommand(h, repository.getProject(),"FEATURE_squash");

        h.addParameters(featureName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult publishFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("feature");
        h.addParameters("publish");
        h.addParameters(featureName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }


    // feature pull seems to be kind of useless. see
    // http://stackoverflow.com/questions/18412750/why-doesnt-git-flow-feature-pull-track
    @Override
    public GitCommandResult pullFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);
        h.addParameters("feature");
        h.addParameters("pull");
        h.addParameters(remote.getName());
        h.addParameters(featureName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult trackFeature(
        @Nonnull GitRepository repository,
        @Nonnull String featureName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);
        h.addParameters("feature");
        h.addParameters("track");
        h.addParameters(featureName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    //release

    @Override
    public GitCommandResult startRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("release");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(), "RELEASE_fetchFromOrigin");

        h.addParameters(releaseName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult finishRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nonnull String tagMessage,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("release");
        h.addParameters("finish");

        addOptionsCommand(h, repository.getProject(), "RELEASE_fetchFromOrigin");
        addOptionsCommand(h, repository.getProject(), "RELEASE_pushOnFinish");
        addOptionsCommand(h, repository.getProject(), "RELEASE_keepRemote");
        addOptionsCommand(h, repository.getProject(), "RELEASE_keepLocal");
        addOptionsCommand(h, repository.getProject(), "RELEASE_keepBranch");
//        addOptionsCommand(h, repository.getProject(),"RELEASE_squash");

        h.addParameters(releaseName);

        Map<String, String> dontTag = GitflowOptionsFactory.getOptionById("RELEASE_dontTag");
        if (GitflowConfigurable.isOptionActive(repository.getProject(), dontTag.get("id"))) {
            h.addParameters(dontTag.get("flag"));
        }
        else {
            h.addParameters("-m");
            h.addParameters(tagMessage);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult publishRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);

        h.setSilent(false);

        h.addParameters("release");
        h.addParameters("publish");
        h.addParameters(releaseName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult trackRelease(
        @Nonnull GitRepository repository,
        @Nonnull String releaseName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("release");
        h.addParameters("track");
        h.addParameters(releaseName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    //hotfix

    @Override
    public GitCommandResult startHotfix(
        @Nonnull GitRepository repository,
        @Nonnull String hotfixName,
        @Nullable String baseBranch,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("hotfix");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(), "HOTFIX_fetchFromOrigin");

        h.addParameters(hotfixName);

        if (baseBranch != null) {
            h.addParameters(baseBranch);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult finishHotfix(
        @Nonnull GitRepository repository,
        @Nonnull String hotfixName,
        @Nonnull String tagMessage,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("hotfix");
        h.addParameters("finish");

        addOptionsCommand(h, repository.getProject(), "HOTFIX_keepBranch");
        addOptionsCommand(h, repository.getProject(), "HOTFIX_fetchFromOrigin");
        addOptionsCommand(h, repository.getProject(), "HOTFIX_pushOnFinish");

        h.addParameters(hotfixName);

        Map<String, String> dontTag = GitflowOptionsFactory.getOptionById("HOTFIX_dontTag");
        if (GitflowConfigurable.isOptionActive(repository.getProject(), dontTag.get("id"))) {
            h.addParameters(dontTag.get("flag"));
        }
        else {
            h.addParameters("-m");
            h.addParameters(tagMessage);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult publishHotfix(
        @Nonnull GitRepository repository,
        @Nonnull String hotfixName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);

        h.setSilent(false);

        h.addParameters("hotfix");
        h.addParameters("publish");
        h.addParameters(hotfixName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    private void setUrl(GitLineHandler h, GitRepository repository) {
        List<GitRemote> remotes = new ArrayList<>(repository.getRemotes());

        //make sure a remote repository is available
        if (!remotes.isEmpty()) {
            h.setUrl(remotes.iterator().next().getFirstUrl());
        }
    }

    // Bugfix

    @Override
    public GitCommandResult startBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nullable String baseBranch,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("bugfix");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(), "BUGFIX_fetchFromOrigin");

        h.addParameters(bugfixName);

        if (baseBranch != null) {
            h.addParameters(baseBranch);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult finishBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());

        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("bugfix");
        h.addParameters("finish");


        addOptionsCommand(h, repository.getProject(), "BUGFIX_keepRemote");
        addOptionsCommand(h, repository.getProject(), "BUGFIX_keepLocal");
        addOptionsCommand(h, repository.getProject(), "BUGFIX_keepBranch");
        addOptionsCommand(h, repository.getProject(), "BUGFIX_fetchFromOrigin");
//        addOptionsCommand(h, repository.getProject(),"BUGFIX_squash");

        h.addParameters(bugfixName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult publishBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("bugfix");
        h.addParameters("publish");
        h.addParameters(bugfixName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    // feature/bugfix pull seems to be kind of useless. see
    // http://stackoverflow.com/questions/18412750/why-doesnt-git-flow-feature-pull-track
    @Override
    public GitCommandResult pullBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);
        h.addParameters("bugfix");
        h.addParameters("pull");
        h.addParameters(remote.getName());
        h.addParameters(bugfixName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult trackBugfix(
        @Nonnull GitRepository repository,
        @Nonnull String bugfixName,
        @Nonnull GitRemote remote,
        @Nullable GitLineHandlerListener... listeners
    ) {
        GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);
        h.addParameters("bugfix");
        h.addParameters("track");
        h.addParameters(bugfixName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    @Override
    public GitCommandResult version(@Nonnull Project project, GitLineHandlerListener... listeners) {
        GitLineHandler h = new GitLineHandler(project, new File(project.getBasePath()), flowCommand());

        h.addParameters("version");

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }
}

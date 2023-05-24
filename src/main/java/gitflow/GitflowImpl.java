package gitflow;

import consulo.annotation.component.ServiceImpl;
import consulo.project.Project;
import git4idea.commands.*;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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

    private void addOptionsCommand(GitLineHandler h, Project project, String optionId){
        HashMap<String,String> optionMap = GitflowOptionsFactory.getOptionById(optionId);
        if (GitflowConfigurable.isOptionActive(project, optionMap.get("id"))){
            h.addParameters(optionMap.get("flag"));
        }
    }

    public GitCommandResult initRepo(@NotNull GitRepository repository,
                                     GitflowInitOptions initOptions, @Nullable GitLineHandlerListener... listeners) {
        return callInit(repository, initOptions, false, listeners);
    }

    public GitCommandResult reInitRepo(@NotNull GitRepository repository,
            GitflowInitOptions initOptions,
            @Nullable GitLineHandlerListener... listeners) {
        return callInit(repository, initOptions, true, listeners);
    }

    private GitCommandResult callInit(@NotNull GitRepository repository,
            GitflowInitOptions initOptions, boolean reInit,
            @Nullable GitLineHandlerListener[] listeners) {
        GitLineHandler h;
        if (initOptions.isUseDefaults()) {
            h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        } else {
            h = new GitInitLineHandler(initOptions, repository.getProject(),
                    repository.getRoot(), flowCommand());
        }

        h.setSilent(false);
        h.setStdoutSuppressed(false);
        h.setStderrSuppressed(false);

        h.addParameters("init");

        if (initOptions.isUseDefaults()) {
            h.addParameters("-d");
        } else {
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

    public GitCommandResult startFeature(@NotNull GitRepository repository,
                                         @NotNull String featureName,
                                         @Nullable String baseBranch,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("feature");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(),"FEATURE_fetchFromOrigin");

        h.addParameters(featureName);

        if (baseBranch != null) {
            h.addParameters(baseBranch);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    public GitCommandResult finishFeature(@NotNull GitRepository repository,
                                          @NotNull String featureName,
                                          @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());

        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("feature");
        h.addParameters("finish");


        addOptionsCommand(h, repository.getProject(),"FEATURE_keepRemote");
        addOptionsCommand(h, repository.getProject(),"FEATURE_keepLocal");
        addOptionsCommand(h, repository.getProject(),"FEATURE_keepBranch");
        addOptionsCommand(h, repository.getProject(),"FEATURE_fetchFromOrigin");
        addOptionsCommand(h, repository.getProject(),"FEATURE_pushOnFinish");
        addOptionsCommand(h, repository.getProject(),"FEATURE_noFastForward");
//        addOptionsCommand(h, repository.getProject(),"FEATURE_squash");

        h.addParameters(featureName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }


    public GitCommandResult publishFeature(@NotNull GitRepository repository,
                                           @NotNull String featureName,
                                           @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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
    public GitCommandResult pullFeature(@NotNull GitRepository repository,
                                        @NotNull String featureName,
                                        @NotNull GitRemote remote,
                                        @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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

    public GitCommandResult trackFeature(@NotNull GitRepository repository,
                                         @NotNull String featureName,
                                         @NotNull GitRemote remote,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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

    public GitCommandResult startRelease(@NotNull GitRepository repository,
                                         @NotNull String releaseName,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("release");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(),"RELEASE_fetchFromOrigin");

        h.addParameters(releaseName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    public GitCommandResult finishRelease(@NotNull GitRepository repository,
                                          @NotNull String releaseName,
                                          @NotNull String tagMessage,
                                          @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("release");
        h.addParameters("finish");

        addOptionsCommand(h, repository.getProject(),"RELEASE_fetchFromOrigin");
        addOptionsCommand(h, repository.getProject(),"RELEASE_pushOnFinish");
        addOptionsCommand(h, repository.getProject(),"RELEASE_keepRemote");
        addOptionsCommand(h, repository.getProject(),"RELEASE_keepLocal");
        addOptionsCommand(h, repository.getProject(),"RELEASE_keepBranch");
//        addOptionsCommand(h, repository.getProject(),"RELEASE_squash");

        h.addParameters(releaseName);

        HashMap<String,String> dontTag = GitflowOptionsFactory.getOptionById("RELEASE_dontTag");
        if (GitflowConfigurable.isOptionActive(repository.getProject(), dontTag.get("id"))){
            h.addParameters(dontTag.get("flag"));
        }
        else{
            h.addParameters("-m");
            h.addParameters(tagMessage);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }


    public GitCommandResult publishRelease(@NotNull GitRepository repository,
                                           @NotNull String releaseName,
                                           @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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

    public GitCommandResult trackRelease(@NotNull GitRepository repository,
                                         @NotNull String releaseName,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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

    public GitCommandResult startHotfix(@NotNull GitRepository repository,
                                        @NotNull String hotfixName,
                                        @Nullable String baseBranch,
                                        @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("hotfix");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(),"HOTFIX_fetchFromOrigin");

        h.addParameters(hotfixName);

        if (baseBranch != null) {
            h.addParameters(baseBranch);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    public GitCommandResult finishHotfix(@NotNull GitRepository repository,
                                         @NotNull String hotfixName,
                                         @NotNull String tagMessage,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("hotfix");
        h.addParameters("finish");

        addOptionsCommand(h, repository.getProject(),"HOTFIX_keepBranch");
        addOptionsCommand(h, repository.getProject(),"HOTFIX_fetchFromOrigin");
        addOptionsCommand(h, repository.getProject(),"HOTFIX_pushOnFinish");

        h.addParameters(hotfixName);

        HashMap<String,String> dontTag = GitflowOptionsFactory.getOptionById("HOTFIX_dontTag");
        if (GitflowConfigurable.isOptionActive(repository.getProject(), dontTag.get("id"))){
            h.addParameters(dontTag.get("flag"));
        }
        else{
            h.addParameters("-m");
            h.addParameters(tagMessage);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    public GitCommandResult publishHotfix(@NotNull GitRepository repository,
                                          @NotNull String hotfixName,
                                          @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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
        ArrayList<GitRemote> remotes = new ArrayList<GitRemote>(repository.getRemotes());

        //make sure a remote repository is available
        if (!remotes.isEmpty()) {
            h.setUrl(remotes.iterator().next().getFirstUrl());
        }
    }

    // Bugfix

    public GitCommandResult startBugfix(@NotNull GitRepository repository,
                                         @NotNull String bugfixName,
                                         @Nullable String baseBranch,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
        h.setSilent(false);

        h.addParameters("bugfix");
        h.addParameters("start");

        addOptionsCommand(h, repository.getProject(),"BUGFIX_fetchFromOrigin");

        h.addParameters(bugfixName);

        if (baseBranch != null) {
            h.addParameters(baseBranch);
        }

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }

    public GitCommandResult finishBugfix(@NotNull GitRepository repository,
                                          @NotNull String bugfixName,
                                          @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());

        setUrl(h, repository);
        h.setSilent(false);

        h.addParameters("bugfix");
        h.addParameters("finish");


        addOptionsCommand(h, repository.getProject(),"BUGFIX_keepRemote");
        addOptionsCommand(h, repository.getProject(),"BUGFIX_keepLocal");
        addOptionsCommand(h, repository.getProject(),"BUGFIX_keepBranch");
        addOptionsCommand(h, repository.getProject(),"BUGFIX_fetchFromOrigin");
//        addOptionsCommand(h, repository.getProject(),"BUGFIX_squash");

        h.addParameters(bugfixName);

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }


    public GitCommandResult publishBugfix(@NotNull GitRepository repository,
                                           @NotNull String bugfixName,
                                           @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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
    public GitCommandResult pullBugfix(@NotNull GitRepository repository,
                                        @NotNull String bugfixName,
                                        @NotNull GitRemote remote,
                                        @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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

    public GitCommandResult trackBugfix(@NotNull GitRepository repository,
                                         @NotNull String bugfixName,
                                         @NotNull GitRemote remote,
                                         @Nullable GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(), flowCommand());
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
    public GitCommandResult version(@NotNull Project project, GitLineHandlerListener... listeners) {
        final GitLineHandler h = new GitLineHandler(project, new File(project.getBasePath()), flowCommand());

        h.addParameters("version");

        for (GitLineHandlerListener listener : listeners) {
            h.addLineListener(listener);
        }
        return myGit.runCommand(h);
    }
}

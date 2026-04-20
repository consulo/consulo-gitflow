package gitflow;

import consulo.application.Application;
import consulo.disposer.Disposer;
import consulo.project.Project;
import consulo.versionControlSystem.VcsException;
import consulo.virtualFileSystem.VirtualFile;
import git4idea.config.GitConfigUtil;
import git4idea.repo.GitRepository;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */
public class GitflowConfigUtil {
    public static final String BRANCH_MASTER = "gitflow.branch.master";
    public static final String BRANCH_DEVELOP = "gitflow.branch.develop";
    public static final String PREFIX_FEATURE = "gitflow.prefix.feature";
    public static final String PREFIX_RELEASE = "gitflow.prefix.release";
    public static final String PREFIX_HOTFIX = "gitflow.prefix.hotfix";
    public static final String PREFIX_BUGFIX = "gitflow.prefix.bugfix";
    public static final String PREFIX_SUPPORT = "gitflow.prefix.support";
    public static final String PREFIX_VERSIONTAG = "gitflow.prefix.versiontag";

    private static final Map<Project, Map<String, GitflowConfigUtil>> gitflowConfigUtilMap = new HashMap<>();

    @Nonnull
    final Project myProject;
    @Nonnull
    final GitRepository myRepo;
    public String masterBranch;
    public String developBranch;
    public String featurePrefix;
    public String releasePrefix;
    public String hotfixPrefix;
    public String bugfixPrefix;
    public String supportPrefix;
    public String versiontagPrefix;

    public static GitflowConfigUtil getInstance(@Nonnull Project project, @Nonnull GitRepository repo) {
        GitflowConfigUtil instance;
        if (gitflowConfigUtilMap.containsKey(project) && gitflowConfigUtilMap.get(project).containsKey(repo.getPresentableUrl())) {
            instance = gitflowConfigUtilMap.get(project).get(repo.getPresentableUrl());
        }
        else {
            Map<String, GitflowConfigUtil> innerMap = new HashMap<>();
            instance = new GitflowConfigUtil(project, repo);

            gitflowConfigUtilMap.put(project, innerMap);
            innerMap.put(repo.getPresentableUrl(), instance);

            //cleanup
            Disposer.register(repo, () -> innerMap.remove(repo));
            Disposer.register(project, () -> gitflowConfigUtilMap.remove(project));
        }

        return instance;
    }

    GitflowConfigUtil(@Nonnull Project project, @Nonnull GitRepository repo) {
        myProject = project;
        myRepo = repo;

        update();
    }

    public void update() {
        VirtualFile root = myRepo.getRoot();

        try {
            Future<Void> f = (Future<Void>) Application.get().executeOnPooledThread((Runnable) () -> {
                try {
                    masterBranch = GitConfigUtil.getValue(myProject, root, BRANCH_MASTER);
                    developBranch = GitConfigUtil.getValue(myProject, root, BRANCH_DEVELOP);
                    featurePrefix = GitConfigUtil.getValue(myProject, root, PREFIX_FEATURE);
                    releasePrefix = GitConfigUtil.getValue(myProject, root, PREFIX_RELEASE);
                    hotfixPrefix = GitConfigUtil.getValue(myProject, root, PREFIX_HOTFIX);
                    bugfixPrefix = GitConfigUtil.getValue(myProject, root, PREFIX_BUGFIX);
                    supportPrefix = GitConfigUtil.getValue(myProject, root, PREFIX_SUPPORT);
                    versiontagPrefix = GitConfigUtil.getValue(myProject, root, PREFIX_VERSIONTAG);
                }
                catch (VcsException e) {
                    NotifyUtil.notifyError(myProject, "Config error", e);
                }
            });
            f.get();

        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    public String getFeatureNameFromBranch(String branchName) {
        return branchName.substring(branchName.indexOf(featurePrefix) + featurePrefix.length(), branchName.length());
    }

    public String getReleaseNameFromBranch(String branchName) {
        return branchName.substring(branchName.indexOf(releasePrefix) + releasePrefix.length(), branchName.length());
    }

    public String getHotfixNameFromBranch(String branchName) {
        return branchName.substring(branchName.indexOf(hotfixPrefix) + hotfixPrefix.length(), branchName.length());
    }

    public String getBugfixNameFromBranch(String branchName) {
        return branchName.substring(branchName.indexOf(bugfixPrefix) + bugfixPrefix.length(), branchName.length());
    }

    public String getRemoteNameFromBranch(String branchName) {
        return branchName.substring(0, branchName.indexOf("/"));
    }

    public void setMasterBranch(String branchName) {
        masterBranch = branchName;
        VirtualFile root = myRepo.getRoot();
        try {
            GitConfigUtil.setValue(myProject, root, BRANCH_MASTER, branchName);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setDevelopBranch(String branchName) {
        developBranch = branchName;
        VirtualFile root = myRepo.getRoot();
        try {
            GitConfigUtil.setValue(myProject, root, BRANCH_DEVELOP, branchName);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setReleasePrefix(String prefix) {
        releasePrefix = prefix;
        VirtualFile root = myRepo.getRoot();

        try {
            GitConfigUtil.setValue(myProject, root, PREFIX_RELEASE, prefix);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setFeaturePrefix(String prefix) {
        featurePrefix = prefix;
        VirtualFile root = myRepo.getRoot();

        try {
            GitConfigUtil.setValue(myProject, root, PREFIX_FEATURE, prefix);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setHotfixPrefix(String prefix) {
        hotfixPrefix = prefix;
        VirtualFile root = myRepo.getRoot();

        try {
            GitConfigUtil.setValue(myProject, root, PREFIX_HOTFIX, prefix);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setBugfixPrefix(String prefix) {
        bugfixPrefix = prefix;
        VirtualFile root = myRepo.getRoot();

        try {
            GitConfigUtil.setValue(myProject, root, PREFIX_BUGFIX, prefix);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setSupportPrefix(String prefix) {
        supportPrefix = prefix;
        VirtualFile root = myRepo.getRoot();

        try {
            GitConfigUtil.setValue(myProject, root, PREFIX_SUPPORT, prefix);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    public void setVersionPrefix(String prefix) {
        versiontagPrefix = prefix;
        VirtualFile root = myRepo.getRoot();

        try {
            GitConfigUtil.setValue(myProject, root, PREFIX_VERSIONTAG, prefix);
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }
    }

    // this still reads from config because it always runs from an action and never from the EDT
    public String getBaseBranch(String branchName) {

        VirtualFile root = myRepo.getRoot();

        String baseBranch = null;
        try {
            baseBranch = GitConfigUtil.getValue(myProject, root, "gitflow.branch." + branchName + ".base");
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(myProject, "Config error", e);
        }

        return baseBranch;
    }
}

package gitflow.actions;

import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.versionControlSystem.VcsException;
import consulo.virtualFileSystem.VirtualFileManager;
import git4idea.branch.GitBranchUtil;
import git4idea.merge.GitMerger;
import git4idea.repo.GitRepository;
import gitflow.Gitflow;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.IDEAUtils;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class GitflowAction extends DumbAwareAction {
    private static final long VFM_REFRESH_DELAY = 750L;

    Project myProject;
    final Gitflow myGitflow = ServiceManager.getService(Gitflow.class);
    final List<GitRepository> myRepos = new ArrayList<>();
    GitRepository myRepo;
    GitflowBranchUtil branchUtil;

    VirtualFileManager virtualFileManager;

    GitflowAction(String actionName) {
        super(actionName);
    }

    GitflowAction(GitRepository repo, String actionName) {
        super(actionName);
        myRepo = repo;

        branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        Project project = IDEAUtils.getActiveProject();

        // if repo isn't set explicitly, such as in the case of starting from keyboard shortcut, infer it
        if (myRepo == null) {
            myRepo = GitBranchUtil.getCurrentRepository(project);
        }
        setup(project);
    }

    private void setup(Project project) {
        myProject = project;
        virtualFileManager = VirtualFileManager.getInstance();
        myRepos.add(myRepo);

        branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
    }

    public void runAction(
        Project project,
        String baseBranchName,
        String branchName,
        @Nullable Runnable callInAwtLater
    ) {
        setup(project);
    }

    //returns true if merge successful, false otherwise
    @RequiredUIAccess
    public boolean handleMerge(Project project) {
        // FIXME As of 201.0 the async version of this method still doesn't make use of the callback, else we'd
        //  simply use a FutureTask (which is a Runnable) and its get() method prior to launching the merge tool.
        virtualFileManager.syncRefresh();

        try {
            long start, end = System.currentTimeMillis();
            do {
                start = end;
                // Hence this ugly hack, to let the time to intellij to catch up with the external changes made by the
                // CLI before being able to run the merge tool. Else the tool won't have the right state, won't display,
                // and the merge success Y/N dialog will appear directly! Anyway, in v193 500ms was sufficient,
                // but in v201 the right value seems to be in the [700-750]ms range (on the committer's machine).
                Thread.sleep(VFM_REFRESH_DELAY);

                GitflowActions.runMergeTool(project); // The window is modal, so we can measure how long it's opened.
                end = System.currentTimeMillis();
            }
            while (end - start < 1000L); // Additional hack: a window open <1s obviously didn't open, let's try again.

            myRepo.update();

            // And refreshing again so an onscreen file doesn't show in a conflicted state when the Y/N dialog show up.
            virtualFileManager.syncRefresh();
            Thread.sleep(VFM_REFRESH_DELAY);

            return askUserForMergeSuccess(project);
        }
        catch (InterruptedException ignored) {
            return false;
        }
    }

    @RequiredUIAccess
    private static boolean askUserForMergeSuccess(Project myProject) {
        //if merge was completed successfully, finish the action
        //note that if it wasn't intellij is left in the "merging state", and git4idea provides no UI way to resolve it
        //merging can be done via intellij itself or any other util
        int answer = Messages.showYesNoDialog(myProject, "Was the merge completed successfully?", "Merge", UIUtil.getQuestionIcon());
        if (answer == 0) {
            GitMerger gitMerger = new GitMerger(myProject);

            try {
                gitMerger.mergeCommit(gitMerger.getMergingRoots());
            }
            catch (VcsException e1) {
                NotifyUtil.notifyError(myProject, "Error", "Error committing merge result");
                e1.printStackTrace();
            }

            return true;
        }
        else {
            NotifyUtil.notifyInfo(
                myProject,
                "Merge incomplete",
                "To manually complete the merge choose VCS > Git > Resolve Conflicts.\n" +
                    "Once done, commit the merged files.\n"
            );
            return false;
        }


    }
}

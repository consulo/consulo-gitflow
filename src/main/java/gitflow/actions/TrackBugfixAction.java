package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.GitflowBranchChooseDialog;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

public class TrackBugfixAction extends AbstractTrackAction {
    TrackBugfixAction() {
        super("Track Bugfix", BranchType.Bugfix);
    }

    TrackBugfixAction(GitRepository repo) {
        super(repo, "Track Bugfix", BranchType.Bugfix);
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        super.actionPerformed(e);

        List<String> remoteBranches = branchUtil.getRemoteBranchNames();
        List<String> remoteBugfixBranches = new ArrayList<>();

        //get only the branches with the proper prefix
        for (String item : remoteBranches) {
            if (item.contains(branchUtil.getPrefixBugfix())) {
                remoteBugfixBranches.add(item);
            }
        }

        if (remoteBranches.size() > 0) {
            GitflowBranchChooseDialog branchChoose = new GitflowBranchChooseDialog(myProject, remoteBugfixBranches);

            branchChoose.show();
            if (branchChoose.isOK()) {
                String branchName = branchChoose.getSelectedBranchName();

                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
                final String bugfixName = gitflowConfigUtil.getBugfixNameFromBranch(branchName);
                final GitRemote remote = branchUtil.getRemoteByBranch(branchName);
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

                new Task.Backgroundable(myProject, "Tracking bugfix " + bugfixName, false) {
                    @Override
                    public void run(@Nonnull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.trackBugfix(myRepo, bugfixName, remote, errorLineHandler);
                        if (result.success()) {
                            String trackedBugfixMessage =
                                String.format("A new branch '%s%s' was created", branchUtil.getPrefixBugfix(), bugfixName);
                            NotifyUtil.notifySuccess(myProject, bugfixName, trackedBugfixMessage);
                        }
                        else {
                            NotifyUtil.notifyError(
                                myProject,
                                "Error",
                                "Please have a look at the Version Control console for more details"
                            );
                        }
                        myRepo.update();
                    }
                }.queue();
            }
        }
        else {
            NotifyUtil.notifyError(myProject, "Error", "No remote branches");
        }
    }
}
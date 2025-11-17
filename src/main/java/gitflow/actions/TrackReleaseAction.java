package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.GitflowBranchChooseDialog;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

public class TrackReleaseAction extends AbstractTrackAction {
    TrackReleaseAction() {
        super("Track Release", BranchType.Release);
    }

    TrackReleaseAction(GitRepository repo) {
        super(repo, "Track Release", BranchType.Release);
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        super.actionPerformed(e);

        List<String> remoteBranches = branchUtil.getRemoteBranchNames();
        List<String> remoteReleaseBranches = new ArrayList<>();

        //get only the branches with the proper prefix
        for (String item : remoteBranches) {
            if (item.contains(branchUtil.getPrefixRelease())) {
                remoteReleaseBranches.add(item);
            }
        }

        if (remoteBranches.size() > 0) {
            GitflowBranchChooseDialog branchChoose = new GitflowBranchChooseDialog(myProject, remoteReleaseBranches);

            branchChoose.show();
            if (branchChoose.isOK()) {
                String branchName = branchChoose.getSelectedBranchName();
                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
                final String releaseName = gitflowConfigUtil.getReleaseNameFromBranch(branchName);
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

                new Task.Backgroundable(myProject, "Tracking release " + releaseName, false) {
                    @Override
                    public void run(@Nonnull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.trackRelease(myRepo, releaseName, errorLineHandler);

                        if (result.success()) {
                            String trackedReleaseMessage = String.format(
                                " A new remote tracking branch '%s%s' was created",
                                branchUtil.getPrefixRelease(),
                                releaseName
                            );
                            NotifyUtil.notifySuccess(myProject, releaseName, trackedReleaseMessage);
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
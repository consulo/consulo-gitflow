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

public class TrackFeatureAction extends AbstractTrackAction {
    TrackFeatureAction() {
        super("Track Feature", BranchType.Feature);
    }

    TrackFeatureAction(GitRepository repo) {
        super(repo, "Track Feature", BranchType.Feature);
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        super.actionPerformed(e);

        List<String> remoteBranches = branchUtil.getRemoteBranchNames();
        List<String> remoteFeatureBranches = new ArrayList<>();

        //get only the branches with the proper prefix
        for (String item : remoteBranches) {
            if (item.contains(branchUtil.getPrefixFeature())) {
                remoteFeatureBranches.add(item);
            }
        }

        if (remoteBranches.size() > 0) {
            GitflowBranchChooseDialog branchChoose = new GitflowBranchChooseDialog(myProject, remoteFeatureBranches);

            branchChoose.show();
            if (branchChoose.isOK()) {
                String branchName = branchChoose.getSelectedBranchName();
                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
                final String featureName = gitflowConfigUtil.getFeatureNameFromBranch(branchName);
                final GitRemote remote = branchUtil.getRemoteByBranch(branchName);
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

                new Task.Backgroundable(myProject, "Tracking feature " + featureName, false) {
                    @Override
                    public void run(@Nonnull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.trackFeature(myRepo, featureName, remote, errorLineHandler);

                        if (result.success()) {
                            String trackedFeatureMessage =
                                String.format("A new branch '%s%s' was created", branchUtil.getPrefixFeature(), featureName);
                            NotifyUtil.notifySuccess(myProject, featureName, trackedFeatureMessage);
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
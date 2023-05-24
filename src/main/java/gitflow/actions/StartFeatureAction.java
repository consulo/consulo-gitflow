package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.DialogWrapper;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.ui.GitflowStartFeatureDialog;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartFeatureAction extends AbstractStartAction {

    public StartFeatureAction() {
        super("Start Feature");
    }
    public StartFeatureAction(GitRepository repo) {
        super(repo, "Start Feature");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        GitflowStartFeatureDialog dialog = new GitflowStartFeatureDialog(myProject, myRepo);
        dialog.show();

        if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

        final String featureName = dialog.getNewBranchName();
        final String baseBranchName = dialog.getBaseBranchName();

        this.runAction(e.getData(Project.KEY), baseBranchName, featureName, null);
    }

    public void runAction(final Project project, final String baseBranchName, final String featureName, @Nullable final Runnable callInAwtLater){
        super.runAction(project, baseBranchName, featureName, callInAwtLater);

        new Task.Backgroundable(myProject, "Starting feature " + featureName, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                final GitCommandResult commandResult = createFeatureBranch(baseBranchName, featureName);
                if (callInAwtLater != null && commandResult.success()) {
                    callInAwtLater.run();
                }
            }
        }.queue();
    }

    private GitCommandResult createFeatureBranch(String baseBranchName, String featureName) {
        GitflowErrorsListener errorListener = new GitflowErrorsListener(myProject);
        GitCommandResult result = myGitflow.startFeature(myRepo, featureName, baseBranchName, errorListener);

        if (result.success()) {
            String startedFeatureMessage = String.format("A new branch '%s%s' was created, based on '%s'", branchUtil.getPrefixFeature(), featureName, baseBranchName);
            NotifyUtil.notifySuccess(myProject, featureName, startedFeatureMessage);
        } else {
            NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
        }

        myRepo.update();
        virtualFileMananger.asyncRefresh(null); //update editors
        return result;
    }
}
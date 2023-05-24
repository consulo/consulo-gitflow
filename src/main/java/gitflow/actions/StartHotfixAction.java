package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.DialogWrapper;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.ui.GitflowStartHotfixDialog;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StartHotfixAction extends AbstractStartAction {

    public StartHotfixAction(GitRepository repo) {
        super(repo, "Start Hotfix");
    }

    public StartHotfixAction() {
        super("Start Hotfix");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        GitflowStartHotfixDialog dialog = new GitflowStartHotfixDialog(myProject, myRepo);
        dialog.show();

        if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

        final String hotfixName = dialog.getNewBranchName();
        final String baseBranchName = dialog.getBaseBranchName();

        this.runAction(e.getData(Project.KEY), baseBranchName, hotfixName, null);
    }

    public void runAction(final Project project, final String baseBranchName, final String hotfixName, @Nullable final Runnable callInAwtLater){
        super.runAction(project, baseBranchName, hotfixName, callInAwtLater);

        new Task.Backgroundable(myProject, "Starting hotfix " + hotfixName, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                final GitCommandResult commandResult = createHotfixBranch(baseBranchName, hotfixName);
                if (callInAwtLater != null && commandResult.success()) {
                    callInAwtLater.run();
                }
            }
        }.queue();
    }

    private GitCommandResult createHotfixBranch(String baseBranchName, String hotfixBranchName) {
        GitflowErrorsListener errorListener = new GitflowErrorsListener(myProject);
        GitCommandResult result = myGitflow.startHotfix(myRepo, hotfixBranchName, baseBranchName, errorListener);

        if (result.success()) {
            String startedHotfixMessage = String.format("A new hotfix '%s%s' was created, based on '%s'",
                    branchUtil.getPrefixHotfix(), hotfixBranchName, baseBranchName);
            NotifyUtil.notifySuccess(myProject, hotfixBranchName, startedHotfixMessage);
        } else {
            NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
        }

        myRepo.update();

        return result;
    }
}
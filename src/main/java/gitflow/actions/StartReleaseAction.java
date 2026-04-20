package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import git4idea.validators.GitNewBranchNameValidator;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;

public class StartReleaseAction extends AbstractStartAction {
    StartReleaseAction() {
        super("Start Release");
    }

    StartReleaseAction(GitRepository repo) {
        super(repo, "Start Release");
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        super.actionPerformed(e);

        final String releaseName = Messages.showInputDialog(
            myProject,
            "Enter the name of new release:",
            "New Release",
            UIUtil.getQuestionIcon(),
            "",
            GitNewBranchNameValidator.newInstance(myRepos)
        );
        final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

        if (releaseName == null) {
            // user clicked cancel
        }
        else if (releaseName.isEmpty()) {
            Messages.showWarningDialog(myProject, "You must provide a name for the release", "Whoops");
        }
        else {
            new Task.Backgroundable(myProject, "Starting release " + releaseName, false) {
                @Override
                public void run(@Nonnull ProgressIndicator indicator) {
                    GitCommandResult result = myGitflow.startRelease(myRepo, releaseName, errorLineHandler);

                    if (result.success()) {
                        String startedReleaseMessage = String.format(
                            "A new release '%s%s' was created, based on '%s'",
                            branchUtil.getPrefixRelease(),
                            releaseName,
                            branchUtil.getBranchnameDevelop()
                        );
                        NotifyUtil.notifySuccess(myProject, releaseName, startedReleaseMessage);
                    }
                    else {
                        NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
                    }

                    myRepo.update();

                }
            }.queue();
        }
    }
}
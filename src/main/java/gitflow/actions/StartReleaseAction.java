package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.localize.LocalizeValue;
import consulo.ui.InputBoxBuilder;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.Messages;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import git4idea.validators.GitNewBranchNameValidator;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

public class StartReleaseAction extends AbstractStartAction {

    StartReleaseAction() {
        super("Start Release");
    }

    StartReleaseAction(GitRepository repo) {
        super(repo,"Start Release");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        InputBoxBuilder.text()
            .asQuestion()
            .title(LocalizeValue.localizeTODO("New Release"))
            .text(LocalizeValue.localizeTODO("Enter the name of new release:"))
            .value("")
            .validator(GitNewBranchNameValidator.newInstance(repos))
            .showAsync(myProject)
            .whenCompleteAsync((releaseName, error) -> {
                if (releaseName != null) {
                    startRelease(releaseName);
                }
            }, UIAccess.current());
    }

    @RequiredUIAccess
    private void startRelease(String releaseName) {
        final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

        if (releaseName.isEmpty()){
            Messages.showWarningDialog(myProject, "You must provide a name for the release", "Whoops");
        }
        else {
            new Task.Backgroundable(myProject,"Starting release "+releaseName,false){
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    GitCommandResult result=  myGitflow.startRelease(myRepo, releaseName, errorLineHandler);

                    if (result.success()) {
                        String startedReleaseMessage = String.format("A new release '%s%s' was created, based on '%s'", branchUtil.getPrefixRelease(), releaseName, branchUtil.getBranchnameDevelop());
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
package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;

public class PublishBugfixAction extends AbstractPublishAction {
    PublishBugfixAction() {
        super("Publish Bugfix", BranchType.Bugfix);
    }

    PublishBugfixAction(GitRepository repo) {
        super(repo, "Publish Bugfix", BranchType.Bugfix);
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        super.actionPerformed(e);

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
        final String bugfixName = gitflowConfigUtil.getBugfixNameFromBranch(branchUtil.getCurrentBranchName());

        new Task.Backgroundable(myProject, "Publishing bugfix " + bugfixName, false) {
            @Override
            public void run(@Nonnull ProgressIndicator indicator) {
                GitCommandResult result = myGitflow.publishBugfix(myRepo, bugfixName, new GitflowErrorsListener(myProject));
                if (result.success()) {
                    String publishedBugfixMessage =
                        String.format("A new remote branch '%s%s' was created", branchUtil.getPrefixBugfix(), bugfixName);
                    NotifyUtil.notifySuccess(myProject, bugfixName, publishedBugfixMessage);
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
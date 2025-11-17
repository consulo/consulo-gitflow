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

public class PublishHotfixAction extends AbstractPublishAction {
    PublishHotfixAction() {
        super("Publish Hotfix", BranchType.Hotfix);
    }

    PublishHotfixAction(GitRepository repo) {
        super(repo, "Publish Hotfix", BranchType.Hotfix);
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        super.actionPerformed(e);

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
        final String hotfixName = gitflowConfigUtil.getHotfixNameFromBranch(branchUtil.getCurrentBranchName());
        final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

        new Task.Backgroundable(myProject, "Publishing hotfix " + hotfixName, false) {
            @Override
            public void run(@Nonnull ProgressIndicator indicator) {
                GitCommandResult result = myGitflow.publishHotfix(myRepo, hotfixName, errorLineHandler);

                if (result.success()) {
                    String publishedHotfixMessage = String.format("A new remote branch '%s%s' was created", branchUtil.getPrefixHotfix(), hotfixName);
                    NotifyUtil.notifySuccess(myProject, hotfixName, publishedHotfixMessage);
                } else {
                    NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
                }

                myRepo.update();
            }
        }.queue();
    }
}
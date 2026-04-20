package gitflow.actions;

import consulo.ui.ex.action.AnActionEvent;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowInitOptions;
import jakarta.annotation.Nonnull;

public class ReInitRepoAction extends InitRepoAction {
    ReInitRepoAction() {
        super("Re-init Repo");
    }

    ReInitRepoAction(GitRepository repo) {
        super(repo, "Re-init Repo");
    }

    @Override
    public void update(@Nonnull AnActionEvent e) {
        gitflow.GitflowBranchUtil branchUtil = gitflow.GitflowBranchUtilManager.getBranchUtil(myRepo);

        // Only show when gitflow is setup
        e.getPresentation().setEnabledAndVisible(branchUtil != null && branchUtil.hasGitflow());
    }

    @Override
    protected String getSuccessMessage() {
        return "Re-initialized gitflow in repo " + myRepo.getRoot().getPresentableName();
    }

    @Override
    protected GitCommandResult executeCommand(
        GitflowInitOptions initOptions,
        GitflowErrorsListener errorLineHandler,
        GitflowLineHandler localLineHandler
    ) {
        return myGitflow.reInitRepo(myRepo, initOptions, errorLineHandler, localLineHandler);
    }

    @Override
    protected String getTitle() {
        return "Re-initializing Repo";
    }

    @Override
    protected GitflowLineHandler getLineHandler() {
        return new GitflowErrorsListener(myProject);
    }
}

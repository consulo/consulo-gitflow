package gitflow.actions;

import consulo.ui.ex.action.AnActionEvent;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import jakarta.annotation.Nonnull;

public abstract class AbstractStartAction extends GitflowAction {
    AbstractStartAction(String actionName) {
        super(actionName);
    }

    AbstractStartAction(GitRepository repo, String actionName) {
        super(repo, actionName);
    }

    @Override
    public void update(@Nonnull AnActionEvent e) {
        GitflowBranchUtil branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
        if (branchUtil != null) {
            //Disable and hide when gitflow has not been setup
            e.getPresentation().setEnabledAndVisible(branchUtil.hasGitflow());
        }
    }
}

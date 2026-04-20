package gitflow.actions;

import consulo.ui.ex.action.AnActionEvent;
import git4idea.repo.GitRepository;
import jakarta.annotation.Nonnull;

public abstract class AbstractBranchAction extends GitflowAction {
    enum BranchType {
        Feature,
        Release,
        Bugfix,
        Hotfix
    }

    final BranchType type;

    AbstractBranchAction(String actionName, BranchType type) {
        super(actionName);
        this.type = type;
    }

    AbstractBranchAction(GitRepository repo, String actionName, BranchType type) {
        super(repo, actionName);
        this.type = type;
    }

    @Override
    public void update(@Nonnull AnActionEvent e) {
        if (branchUtil == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        //Disable and hide when gitflow has not been setup
        if (!branchUtil.hasGitflow()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        //Disable and hide when the branch-type is incorrect
        e.getPresentation().setEnabledAndVisible(isActionAllowedForBranch());
    }

    protected boolean isActionAllowedForBranch() {
        return switch (type) {
            case Feature -> branchUtil.isCurrentBranchFeature();
            case Release -> branchUtil.isCurrentBranchRelease();
            case Bugfix -> branchUtil.isCurrentBranchBugfix();
            case Hotfix -> branchUtil.isCurrentBranchHotfix();
            default -> false;
        };
    }
}

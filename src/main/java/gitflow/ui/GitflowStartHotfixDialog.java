package gitflow.ui;

import consulo.project.Project;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;

public class GitflowStartHotfixDialog extends AbstractBranchStartDialog {

    public GitflowStartHotfixDialog(Project project, GitRepository repo) {
        super(project, repo);
    }

    protected boolean showBranchFromCombo(){
        return false;
    }

    @Override
    protected String getLabel() {
        return "hotfix";
    }

    @Override
    protected String getDefaultBranch() {
        return GitflowConfigUtil.getInstance(getProject(), myRepo).masterBranch;
    }
}

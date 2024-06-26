package gitflow.ui;

import consulo.project.Project;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;

public class GitflowStartBugfixDialog extends AbstractBranchStartDialog {

    public GitflowStartBugfixDialog(Project project, GitRepository repo) {
        super(project, repo);
    }

    @Override
    protected String getLabel() {
        return "bugfix";
    }

    @Override
    protected String getDefaultBranch() {
        return GitflowConfigUtil.getInstance(getProject(), myRepo).developBranch;
    }
}

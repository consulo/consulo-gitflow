package gitflow.ui;

import consulo.project.Project;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;

public class GitflowStartFeatureDialog extends AbstractBranchStartDialog {

    public GitflowStartFeatureDialog(Project project, GitRepository repo) {
        super(project, repo);
    }

    @Override
    protected String getLabel() {
        return "feature";
    }

    @Override
    protected String getDefaultBranch() {
        return GitflowConfigUtil.getInstance(getProject(), myRepo).developBranch;
    }
}

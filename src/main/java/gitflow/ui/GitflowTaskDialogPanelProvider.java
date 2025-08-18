package gitflow.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.task.LocalTask;
import consulo.task.Task;
import consulo.task.TaskManager;
import consulo.task.ui.TaskDialogPanel;
import consulo.task.ui.TaskDialogPanelProvider;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class GitflowTaskDialogPanelProvider extends TaskDialogPanelProvider {

    @Nullable
    @Override
    public TaskDialogPanel getOpenTaskPanel(@NotNull Project project, @NotNull Task task) {
        GitRepository currentRepo = GitBranchUtil.getCurrentRepository(project);
        GitflowBranchUtil branchUtil = GitflowBranchUtilManager.getBranchUtil(currentRepo);
        if (branchUtil.hasGitflow()) {
            return TaskManager.getManager(project).isVcsEnabled() ? new GitflowOpenTaskPanel(project, task, currentRepo) : null;
        }
        else{
            return null;
        }
    }

    @Nullable
    @Override
    public TaskDialogPanel getCloseTaskPanel(@NotNull Project project, @NotNull LocalTask task) {
        GitRepository currentRepo = GitBranchUtil.getCurrentRepository(project);
        GitflowBranchUtil branchUtil = GitflowBranchUtilManager.getBranchUtil(currentRepo);

        if (branchUtil.hasGitflow()) {
            return TaskManager.getManager(project).isVcsEnabled() ? new GitflowCloseTaskPanel(project, task, currentRepo) : null;
        }
        else{
            return null;
        }
    }

}

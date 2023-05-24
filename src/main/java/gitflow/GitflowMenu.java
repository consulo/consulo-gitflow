package gitflow;

import consulo.project.Project;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import gitflow.actions.GitflowPopupGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitflowMenu extends ActionGroup {
    public GitflowMenu() {
        super("Gitflow", true);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        if (e == null) {
            return new AnAction[0];
        }

        Project project = e.getData(Project.KEY);
        if (project == null) {
            return new AnAction[0];
        }

        GitflowPopupGroup popupGroup = new GitflowPopupGroup(project, true);

        return popupGroup.getActionGroup().getChildren(e);
    }
}

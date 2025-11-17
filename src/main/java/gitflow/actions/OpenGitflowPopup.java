package gitflow.actions;

import consulo.project.Project;
import consulo.project.ui.wm.WindowManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import gitflow.ui.GitflowWidget;
import jakarta.annotation.Nonnull;

public class OpenGitflowPopup extends GitflowAction {
    OpenGitflowPopup() {
        super("Gitflow Operations Popup...");
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        // TODO calling super will cause a NPE, if no repo is set up. Since we only need the project, we take it directly from the event
        // super.actionPerformed(e);
        Project currentProject = e.getData(Project.KEY);

        GitflowWidget widget = GitflowWidget.findWidgetInstance(currentProject);
        if (widget != null) {
            widget.showPopupInCenterOf(WindowManager.getInstance().getFrame(currentProject));
        }
    }
}

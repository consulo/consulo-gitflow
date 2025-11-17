package gitflow.actions;

import consulo.dataContext.DataContext;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionPlaces;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.util.dataholder.Key;
import git4idea.actions.GitResolveConflictsAction;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * All actions associated with Gitflow
 *
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */
public class GitflowActions {
    @RequiredUIAccess
    public static void runMergeTool(Project project) {
        GitResolveConflictsAction resolveAction = new GitResolveConflictsAction();
        AnActionEvent e = new AnActionEvent(
            null,
            new ProjectDataContext(project),
            ActionPlaces.UNKNOWN,
            new Presentation(""),
            ActionManager.getInstance(),
            0
        );
        resolveAction.actionPerformed(e);
    }


    /**
     * Simple wrapper containing just enough to let the conflicts resolver to launch
     * We could have transferred the DataContext or wrapped a HackyDataContext from the previous action,
     * but that would make the semantics terrible
     */
    private final static class ProjectDataContext implements DataContext {
        private final Project myProject;

        private ProjectDataContext(Project project) {
            this.myProject = project;
        }

        @Nullable
        @Override
        public Object getData(@Nonnull Key dataId) {
            if (Project.KEY == dataId) {
                return myProject;
            }
            else {
                throw new UnsupportedOperationException(dataId.toString());
            }
        }
    }
}

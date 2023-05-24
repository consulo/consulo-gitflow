package gitflow.actions;

import consulo.dataContext.DataContext;
import consulo.language.editor.CommonDataKeys;
import consulo.project.Project;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionPlaces;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.util.dataholder.Key;
import git4idea.actions.GitResolveConflictsAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All actions associated with Gitflow
 *
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */
public class GitflowActions {

    public static void runMergeTool(Project project){
        GitResolveConflictsAction resolveAction = new GitResolveConflictsAction();
        AnActionEvent e = new AnActionEvent(null, new ProjectDataContext(project), ActionPlaces.UNKNOWN, new Presentation(""), ActionManager.getInstance(), 0);
        resolveAction.actionPerformed(e);
    }


    /**
     * Simple wrapper containing just enough to let the conflicts resolver to launch
     * We could have transferred the DataContext or wrapped a HackyDataContext from the previous action,
     * but that would make the semantics terrible
     */
    private final static class ProjectDataContext implements DataContext {
        private Project project;

        private ProjectDataContext(Project project) {
            this.project = project;
        }

        @Nullable
        @Override
        public Object getData(@NotNull Key dataId) {
            if(CommonDataKeys.PROJECT == dataId) {
                return project;
            } else {
                throw new UnsupportedOperationException(dataId.toString());
            }
        }
    }
}

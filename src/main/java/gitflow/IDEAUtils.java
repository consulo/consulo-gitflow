package gitflow;

import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.project.ui.wm.IdeFrame;
import consulo.project.ui.wm.WindowManager;

public class IDEAUtils {
    public static Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
            if (ideFrame != null && ideFrame.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }
}
